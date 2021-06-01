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
					ZipEntry zipEntry = patchedZip.getEntry(patchMetadata.targetName);
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
		if(!memberMappedJar.toFile().isFile()) throw new IllegalArgumentException(new FileNotFoundException(memberMappedJar.toString()));
		try {
			patchedJar.toFile().getParentFile().mkdirs();
			final ThreadLocal<ZipFile> memberMappedZip = ThreadLocal.withInitial(() -> {
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
								memberMappedZip.get().close();
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
						return getPatchedBytes(memberMappedZip.get(), digest.get(), metadata);
					} catch (IOException | CompressorException | InvalidHeaderException e) {
						throw new RuntimeException(e);
					}
				}, executorService), metadata)).collect(Collectors.toSet());
				try (ZipOutputStream patchedZip = new ZipOutputStream(new FileOutputStream(patchedJar.toFile()))) {
					patchedZip.setMethod(ZipOutputStream.DEFLATED);
					patchedZip.setLevel(Deflater.BEST_SPEED);
					Set<String> processed = new HashSet<>();
					for (PatchData patchData : patchDataSet) {
						putNextEntrySafe(patchedZip, patchData.metadata.targetName);
						final byte[] patchedBytes = patchData.patchedBytesFuture.join();
						patchedZip.write(patchedBytes);
						patchedZip.closeEntry();
						processed.add(patchData.metadata.targetName);
					}

					((Iterator<ZipEntry>) memberMappedZip.get().entries()).forEachRemaining(zipEntry -> {
						if (zipEntry.isDirectory() || processed.contains(patchesMetadata.relocationMapping.getOrDefault(zipEntry.getName(), zipEntry.getName())) || patchesMetadata.copyExcludes.contains(zipEntry.getName()))
							return;
						try {
							InputStream in = memberMappedZip.get().getInputStream(zipEntry);
							putNextEntrySafe(patchedZip, patchesMetadata.relocationMapping.getOrDefault(zipEntry.getName(), zipEntry.getName()));
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

	private static byte[] getPatchedBytes(ZipFile memberMappedZip, MessageDigest digest, PatchesMetadata.PatchMetadata patchMetadata) throws IOException, CompressorException, InvalidHeaderException {
		final byte[] originalBytes;
		final ZipEntry originalEntry = memberMappedZip.getEntry(patchMetadata.originalName);
		if (originalEntry != null)
			try (final InputStream in = memberMappedZip.getInputStream(originalEntry)) {
				originalBytes = IOUtils.toByteArray(in);
			}
		else originalBytes = new byte[0];
		final byte[] patchBytes;
		try (final InputStream in = YatoclipPatcher.class.getClassLoader().getResourceAsStream("patches/" + patchMetadata.targetName + ".patch")) {
			if (in == null)
				throw new FileNotFoundException("patches/" + patchMetadata.targetName + ".patch");
			patchBytes = IOUtils.toByteArray(in);
		}
		if (!patchMetadata.originalHash.equals(ServerSetup.toHex(digest.digest(originalBytes))))
			throw new FileNotFoundException(String.format("Hash do not match: original file: %s: expected %s but got %s", patchMetadata.originalName, patchMetadata.originalHash, ServerSetup.toHex(digest.digest(originalBytes))));

		if (!patchMetadata.patchHash.equals(ServerSetup.toHex(digest.digest(patchBytes))))
			throw new FileNotFoundException(String.format("Hash do not match: patch file: %s: expected %s but got %s", patchMetadata.targetName + ".patch", patchMetadata.patchHash, ServerSetup.toHex(digest.digest(patchBytes))));

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Patch.patch(originalBytes, patchBytes, byteOut);
		final byte[] patchedBytes = byteOut.toByteArray();
		if (!patchMetadata.targetHash.equals(ServerSetup.toHex(digest.digest(patchedBytes))))
			throw new FileNotFoundException(String.format("Hash do not match: target file: %s: expected %s but got %s", patchMetadata.targetName, patchMetadata.targetHash, ServerSetup.toHex(digest.digest(patchedBytes))));
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
