package org.yatopiamc.yatoclip;

import com.google.gson.Gson;
import io.sigpipe.jbsdiff.InvalidHeaderException;
import io.sigpipe.jbsdiff.Patch;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;

public class YatoclipPatcher {

	private static final PatchesMetadata patchesMetadata;

	static {
		try (
				final InputStream in = YatoclipPatcher.class.getClassLoader().getResourceAsStream("patches/metadata.json");
				final InputStreamReader reader = new InputStreamReader(in);
		) {
			patchesMetadata = new Gson().fromJson(reader, PatchesMetadata.class);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	static boolean isJarUpToDate(Path patchedJar) {
		requireNonNull(patchedJar);
		if (!patchedJar.toFile().isFile()) return false;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			try (ZipFile patchedZip = new ZipFile(patchedJar.toFile())) {
				for (PatchesMetadata.PatchMetadata patchMetadata : patchesMetadata.patches) {
					ZipEntry zipEntry = patchedZip.getEntry(patchMetadata.name);
					if (zipEntry == null || !patchMetadata.targetHash.equals(ServerSetup.toHex(digest.digest(IOUtils.toByteArray(patchedZip.getInputStream(zipEntry))))))
						return false;
				}
			}
			return true;
		} catch (Throwable t) {
			System.out.println(t.toString());
			return false;
		}
	}

	static void patchJar(Path memberMappedJar, Path patchedJar) {
		requireNonNull(memberMappedJar);
		requireNonNull(patchedJar);
		if(!memberMappedJar.toFile().isFile()) throw new IllegalArgumentException(new FileNotFoundException());
		try {
			patchedJar.toFile().getParentFile().mkdirs();
			final ThreadLocal<ZipFile> classMappedZip = ThreadLocal.withInitial(() -> {
				try {
					return new ZipFile(memberMappedJar.toFile());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			final ThreadLocal<MessageDigest> digest = ThreadLocal.withInitial(() -> {
				try {
					return MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
			});
			ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
				private AtomicInteger serial = new AtomicInteger(0);

				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(() -> {
						try {
							r.run();
						} finally {
							try {
								classMappedZip.get().close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
					thread.setName("YatoClip Worker #" + serial.incrementAndGet());
					thread.setDaemon(true);
					return thread;
				}
			});
			try {
				final Set<PatchData> patchDataSet = patchesMetadata.patches.stream().map((PatchesMetadata.PatchMetadata metadata) -> new PatchData(CompletableFuture.supplyAsync(() -> {
					try {
						return getPatchedBytes(classMappedZip.get(), digest.get(), metadata);
					} catch (IOException | CompressorException | InvalidHeaderException e) {
						throw new RuntimeException(e);
					}
				}, executorService), metadata)).collect(Collectors.toSet());
				try (ZipOutputStream patchedZip = new ZipOutputStream(new FileOutputStream(patchedJar.toFile()))) {
					patchedZip.setMethod(ZipOutputStream.DEFLATED);
					patchedZip.setLevel(Deflater.BEST_SPEED);
					Set<String> processed = new HashSet<>();
					for (PatchData patchData : patchDataSet) {
						putNextEntrySafe(patchedZip, patchData.metadata.name);
						final byte[] patchedBytes = patchData.patchedBytesFuture.join();
						patchedZip.write(patchedBytes);
						patchedZip.closeEntry();
						processed.add(patchData.metadata.name);
					}

					((Iterator<ZipEntry>) classMappedZip.get().entries()).forEachRemaining(zipEntry -> {
						if (zipEntry.isDirectory() || processed.contains(applyRelocations(zipEntry.getName())) || patchesMetadata.copyExcludes.contains(zipEntry.getName()))
							return;
						try {
							InputStream in = classMappedZip.get().getInputStream(zipEntry);
							putNextEntrySafe(patchedZip, zipEntry.getName());
							patchedZip.write(IOUtils.toByteArray(in));
							patchedZip.closeEntry();
						} catch (Throwable t) {
							throw new RuntimeException(t);
						}
					});
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			executorService.shutdown();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private static byte[] getPatchedBytes(ZipFile classMappedZip, MessageDigest digest, PatchesMetadata.PatchMetadata patchMetadata) throws IOException, CompressorException, InvalidHeaderException {
		final byte[] originalBytes;
		final ZipEntry originalEntry = classMappedZip.getEntry(applyRelocationsReverse(patchMetadata.name));
		if (originalEntry != null)
			try (final InputStream in = classMappedZip.getInputStream(originalEntry)) {
				originalBytes = IOUtils.toByteArray(in);
			}
		else originalBytes = new byte[0];
		final byte[] patchBytes;
		try (final InputStream in = YatoclipPatcher.class.getClassLoader().getResourceAsStream("patches/" + patchMetadata.name + ".patch")) {
			if (in == null)
				throw new FileNotFoundException();
			patchBytes = IOUtils.toByteArray(in);
		}
		if (!patchMetadata.originalHash.equals(ServerSetup.toHex(digest.digest(originalBytes))) || !patchMetadata.patchHash.equals(ServerSetup.toHex(digest.digest(patchBytes))))
			throw new FileNotFoundException("Hash do not match");

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Patch.patch(originalBytes, patchBytes, byteOut);
		final byte[] patchedBytes = byteOut.toByteArray();
		if (!patchMetadata.targetHash.equals(ServerSetup.toHex(digest.digest(patchedBytes))))
			throw new FileNotFoundException("Hash do not match");
		return patchedBytes;
	}

	private static void putNextEntrySafe(ZipOutputStream patchedZip, String name) throws IOException {
		String[] split = name.split("/");
		split = Arrays.copyOfRange(split, 0, split.length - 1);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(s).append("/");
			try {
				patchedZip.putNextEntry(new ZipEntry(sb.toString()));
			} catch (ZipException e) {
				if (e.getMessage().startsWith("duplicate entry"))
					continue;
				throw e;
			}
		}
		final ZipEntry entry = new ZipEntry(name);
		patchedZip.putNextEntry(entry);
	}

	private static String applyRelocations(String name) {
		if (!name.endsWith(".class")) return name;
		if (name.indexOf('/') == -1)
			name = "/" + name;
		for (PatchesMetadata.Relocation relocation : patchesMetadata.relocations) {
			if (name.startsWith(relocation.from) && (relocation.includeSubPackages || name.split("/").length == name.split("/").length - 1)) {
				return relocation.to + name.substring(relocation.from.length());
			}
		}
		return name;
	}

	private static String applyRelocationsReverse(String name) {
		if (!name.endsWith(".class")) return name;
		if (name.indexOf('/') == -1)
			name = "/" + name;
		for (PatchesMetadata.Relocation relocation : patchesMetadata.relocations) {
			if (name.startsWith(relocation.to) && (relocation.includeSubPackages || name.split("/").length == name.split("/").length - 1)) {
				return relocation.from + name.substring(relocation.to.length());
			}
		}
		return name;
	}

	private static class PatchData {

		public final CompletableFuture<byte[]> patchedBytesFuture;
		public final PatchesMetadata.PatchMetadata metadata;


		private PatchData(CompletableFuture<byte[]> patchedBytesFuture, PatchesMetadata.PatchMetadata metadata) {
			Objects.requireNonNull(patchedBytesFuture);
			Objects.requireNonNull(metadata);
			this.patchedBytesFuture = patchedBytesFuture.thenApply(Objects::requireNonNull);
			this.metadata = metadata;
		}
	}

}
