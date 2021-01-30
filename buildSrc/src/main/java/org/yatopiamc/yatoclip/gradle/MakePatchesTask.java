package org.yatopiamc.yatoclip.gradle;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import io.sigpipe.jbsdiff.Diff;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.work.Incremental;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MakePatchesTask extends DefaultTask {

    @OutputDirectory
    private final File outputDir = ((Copy) getProject().getTasks().getByPath("processResources")).getDestinationDir().toPath().resolve("patches").toFile();

    @InputFile
    @Incremental
    public File originalJar = null;
    @InputFile
    @Incremental
    public File targetJar = null;

    public Set<PatchesMetadata.Relocation> getRelocations() {
        return relocations;
    }

    public void setRelocations(Set<PatchesMetadata.Relocation> relocations) {
        this.relocations = relocations;
    }

    @Input
    public Set<PatchesMetadata.Relocation> relocations;

    public File getOriginalJar() {
        return originalJar;
    }

    public void setOriginalJar(File originalJar) {
        this.originalJar = originalJar;
    }

    public File getTargetJar() {
        return targetJar;
    }

    public void setTargetJar(File targetJar) {
        this.targetJar = targetJar;
    }

    public File getOutputDir() {
        return outputDir;
    }

    private ProgressLoggerFactory getProgressLoggerFactory() {
        return ((ProjectInternal) getProject()).getServices().get(ProgressLoggerFactory.class);
    }

    @Inject
    public WorkerExecutor getWorkerExecutor() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void genPatches() throws IOException, InterruptedException {
        Preconditions.checkNotNull(originalJar);
        Preconditions.checkNotNull(targetJar);
        getLogger().lifecycle("Generating patches for " + originalJar + " -> " + targetJar);

        final ProgressLogger genPatches = getProgressLoggerFactory().newOperation(getClass()).setDescription("Generate patches");
        genPatches.started();

        genPatches.progress("Cleanup");
        outputDir.mkdirs();
        FileUtils.cleanDirectory(outputDir);

        genPatches.progress("Reading files");
        ThreadLocal<ZipFile> originalZip = ThreadLocal.withInitial(() -> {
            try {
                return new ZipFile(originalJar);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ThreadLocal<ZipFile> targetZip = ThreadLocal.withInitial(() -> {
            try {
                return new ZipFile(targetJar);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Set<PatchesMetadata.PatchMetadata> patchMetadata = Sets.newConcurrentHashSet();
        ThreadLocal<MessageDigest> digestThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        ThreadLocal<ProgressLogger> progressLoggerThreadLocal = ThreadLocal.withInitial(() -> {
            final ProgressLogger progressLogger = getProgressLoggerFactory().newOperation(this.getClass());
            progressLogger.setDescription("Patch worker");
            progressLogger.started("Idle");
            return progressLogger;
        });
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("MakePatches-%d").setThreadFactory(r -> new Thread(() -> {
                    boolean isExceptionOccurred = false;
                    try {
                        r.run();
                    } catch (Throwable t) {
                        isExceptionOccurred = true;
                        progressLoggerThreadLocal.get().completed(t.toString(), true);
                        throw t;
                    } finally {
                        digestThreadLocal.remove();
                        if (!isExceptionOccurred)
                            progressLoggerThreadLocal.get().completed();
                        progressLoggerThreadLocal.remove();
                        try {
                            originalZip.get().close();
                            targetZip.get().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })).build());
        AtomicInteger current = new AtomicInteger(0);
        final int size = targetZip.get().size();
        ((Iterator<ZipEntry>) targetZip.get().entries()).forEachRemaining(zipEntryT -> {
            genPatches.progress("Submitting tasks (" + current.incrementAndGet() + "/" + size + ")");
            if (zipEntryT.isDirectory()) return;
            executorService.execute(() -> {
                ZipEntry zipEntry = targetZip.get().getEntry(zipEntryT.getName());
                final String child = zipEntry.getName();
                progressLoggerThreadLocal.get().progress("Reading " + zipEntry.getName());
                File outputFile = new File(outputDir, child + ".patch");
                outputFile.getParentFile().mkdirs();
                final byte[] originalBytes;
                final byte[] targetBytes;
                final ZipEntry oEntry = originalZip.get().getEntry(applyRelocationsReverse(child));
                try (
                        final InputStream oin = oEntry != null ? originalZip.get().getInputStream(oEntry) : null;
                        final InputStream tin = targetZip.get().getInputStream(zipEntry);
                ) {
                    originalBytes = oin != null ? IOUtils.toByteArray(oin) : new byte[0];
                    targetBytes = IOUtils.toByteArray(tin);
                } catch (Throwable e) {
                    Throwables.throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
                if (Arrays.equals(originalBytes, targetBytes)) return;

                progressLoggerThreadLocal.get().progress("GenPatch " + zipEntry.getName());
                try (final OutputStream out = new FileOutputStream(outputFile)) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    Diff.diff(originalBytes, targetBytes, byteArrayOutputStream);
                    patchMetadata.add(new PatchesMetadata.PatchMetadata(child, toHex(digestThreadLocal.get().digest(originalBytes)), toHex(digestThreadLocal.get().digest(targetBytes)), toHex(digestThreadLocal.get().digest(byteArrayOutputStream.toByteArray()))));
                    out.write(byteArrayOutputStream.toByteArray());
                } catch (Throwable t) {
                    Throwables.throwIfUnchecked(t);
                    throw new RuntimeException(t);
                }

                progressLoggerThreadLocal.get().progress("Idle");
            });
        });
        genPatches.progress("Calculating exclusions");
        Set<String> copyExcludes = new HashSet<>();
        ((Iterator<ZipEntry>) originalZip.get().entries()).forEachRemaining(zipEntry -> {
            if(targetZip.get().getEntry(applyRelocations(zipEntry.getName())) == null)
                copyExcludes.add(zipEntry.getName());
        });
        originalZip.get().close();
        targetZip.get().close();

        genPatches.progress("Waiting for patching to finish");
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) ;
        digestThreadLocal.remove();

        genPatches.progress("Writing patches metadata");
        try (final OutputStream out = new FileOutputStream(new File(outputDir, "metadata.json"));
             final Writer writer = new OutputStreamWriter(out)) {
            new Gson().toJson(new PatchesMetadata(patchMetadata, relocations, copyExcludes), writer);
        }

        /*
        genPatches.progress("Reading jar files into memory");
        byte[] origin = Files.readAllBytes(originalJar.toPath());
        byte[] target = Files.readAllBytes(targetJar.toPath());

        genPatches.progress("Generating patch");
        try(final OutputStream out = new BufferedOutputStream(new FileOutputStream(output))){
            Diff.diff(origin, target, out);
        }
         */

        genPatches.completed();

    }

    private String applyRelocations(String name) {
        if(!name.endsWith(".class")) return name;
        if (name.indexOf('/') == -1)
            name = "/" + name;
        for (PatchesMetadata.Relocation relocation : relocations) {
            if (name.startsWith(relocation.from) && (relocation.includeSubPackages || name.split("/").length == name.split("/").length - 1)) {
                return relocation.to + name.substring(relocation.from.length());
            }
        }
        return name;
    }

    private String applyRelocationsReverse(String name) {
        if(!name.endsWith(".class")) return name;
        if (name.indexOf('/') == -1)
            name = "/" + name;
        for (PatchesMetadata.Relocation relocation : relocations) {
            if (name.startsWith(relocation.to) && (relocation.includeSubPackages || name.split("/").length == name.split("/").length - 1)) {
                return relocation.from + name.substring(relocation.to.length());
            }
        }
        return name;
    }

    public static String toHex(final byte[] hash) {
        final StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte aHash : hash) {
            sb.append(String.format("%02X", aHash & 0xFF));
        }
        return sb.toString();
    }

}
