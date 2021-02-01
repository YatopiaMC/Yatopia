package org.yatopiamc.yatoclip;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class ServerSetup {

	private static final String minecraftVersion;
	private static final Path cacheDirectory;
	private static final Gson gson = new Gson();

	private static VersionInfo versionInfo = null;
	private static BuildDataInfo buildDataInfo = null;

	static {
		Properties prop = new Properties();
		try (InputStream inputStream = ServerSetup.class.getClassLoader().getResourceAsStream("yatoclip-launch.properties")) {
			prop.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		minecraftVersion = prop.getProperty("minecraftVersion");
		cacheDirectory = Paths.get("cache", minecraftVersion);
		cacheDirectory.toFile().mkdirs();
	}

	public static Path setup() throws IOException {
		long startTime = System.nanoTime();
		checkBuildData();
		applyMappingsAndPatches();
		System.err.println(String.format("Yatoclip server setup completed in %.2fms", (System.nanoTime() - startTime) / 1_000_000.0));
		return cacheDirectory.resolve("Minecraft").resolve(minecraftVersion + "-patched.jar");
	}

	private static void applyMappingsAndPatches() throws IOException {
		final Path minecraftDir = cacheDirectory.resolve("Minecraft");
		minecraftDir.toFile().mkdirs();
		final Path vanillaJar = minecraftDir.resolve(minecraftVersion + ".jar");
		if (!isValidZip(vanillaJar)) {
			System.err.println("Downloading vanilla jar...");
			download(new URL(buildDataInfo.serverUrl), vanillaJar);
			if (!isValidZip(vanillaJar)) throw new RuntimeException("Invalid vanilla jar");
		}
		final Path classMappedJar = minecraftDir.resolve(minecraftVersion + "-cl.jar");
		final Path memberMappedJar = minecraftDir.resolve(minecraftVersion + "-m.jar");
		final Path patchedJar = minecraftDir.resolve(minecraftVersion + "-patched.jar");
		if (!isValidZip(classMappedJar) || !isValidZip(memberMappedJar)) {
			SpecialSourceLauncher.resetSpecialSourceClassloader();
			final Path buildData = cacheDirectory.resolve("BuildData");
			SpecialSourceLauncher.setSpecialSourceJar(buildData.resolve("bin").resolve("SpecialSource-2.jar").toFile());
			System.err.println("Applying class mapping...");
			SpecialSourceLauncher.runProcess(
					"map", "--only", ".", "--only", "net/minecraft", "--auto-lvt", "BASIC", "--auto-member", "SYNTHETIC",
					"-i", vanillaJar.toAbsolutePath().toString(),
					"-m", buildData.resolve("mappings").resolve(buildDataInfo.classMappings).toAbsolutePath().toString(),
					"-o", classMappedJar.toAbsolutePath().toString()
			);
			System.err.println("Applying member mapping...");
			SpecialSourceLauncher.runProcess(
					"map", "--only", ".", "--only", "net/minecraft", "--auto-member", "LOGGER", "--auto-member", "TOKENS",
					"-i", classMappedJar.toAbsolutePath().toString(),
					"-m", buildData.resolve("mappings").resolve(buildDataInfo.memberMappings).toAbsolutePath().toString(),
					"-o", memberMappedJar.toAbsolutePath().toString()
			);
			SpecialSourceLauncher.resetSpecialSourceClassloader();
			if (!isValidZip(classMappedJar) || !isValidZip(memberMappedJar))
				throw new RuntimeException("Unable to apply mappings");
		}

		if (!YatoclipPatcher.isJarUpToDate(patchedJar)){
			System.err.println("Applying patches...");
			YatoclipPatcher.patchJar(memberMappedJar, patchedJar);
			if(!YatoclipPatcher.isJarUpToDate(patchedJar))
				throw new RuntimeException("Unable to apply patches");
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isValidZip(Path zipPath) {
		try {
			ZipFile zipFile = new ZipFile(zipPath.toFile());
			zipFile.close();
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	private static void checkBuildData() throws IOException {
		final Path buildDataDir = cacheDirectory.resolve("BuildData");
		buildDataDir.toFile().mkdirs();
		final Path versionInfoFile = buildDataDir.resolve("version.json");
		if (!tryParseVersionInfo(versionInfoFile)) {
			System.err.println("Downloading version.json...");
			final URL versionInfoURI = new URL("https://hub.spigotmc.org/versions/" + minecraftVersion + ".json");
			download(versionInfoURI, versionInfoFile);
			if (!tryParseVersionInfo(versionInfoFile)) throw new RuntimeException("Unable to parse versionInfo");
		}
		final Path buildDataArchive = buildDataDir.resolve("BuildData.zip");
		if (!tryParseBuildData(buildDataArchive)) {
			System.err.println("Downloading BuildData...");
			final URL buildDataURL = new URL("https://hub.spigotmc.org/stash/rest/api/latest/projects/SPIGOT/repos/builddata/archive?at=" + ServerSetup.versionInfo.refs.buildData + "&format=zip");
			download(buildDataURL, buildDataArchive);
			if (!tryParseBuildData(buildDataArchive)) throw new RuntimeException("Unable to parse BuildData");
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean tryParseBuildData(Path buildData) {
		try {
			ZipFile zipFile = new ZipFile(buildData.toFile());
			((Iterator<ZipEntry>) zipFile.entries()).forEachRemaining(zipEntry -> {
				if (zipEntry.isDirectory()) return;
				buildData.getParent().resolve(zipEntry.getName()).getParent().toFile().mkdirs();
				try (
						final ReadableByteChannel source = Channels.newChannel(zipFile.getInputStream(zipEntry));
						final FileChannel fileChannel = FileChannel.open(buildData.getParent().resolve(zipEntry.getName()), CREATE, WRITE, TRUNCATE_EXISTING)
				) {
					fileChannel.transferFrom(source, 0, Long.MAX_VALUE);
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			});
			zipFile.close();
			try (Reader reader = Files.newBufferedReader(buildData.getParent().resolve("info.json"))){
				ServerSetup.buildDataInfo = gson.fromJson(reader, BuildDataInfo.class);
			}
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean tryParseVersionInfo(Path versionInfo) {
		try (Reader reader = Files.newBufferedReader(versionInfo)) {
			ServerSetup.versionInfo = gson.fromJson(reader, VersionInfo.class);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	private static void download(URL url, Path downloadTo) throws IOException {
		try (
				final ReadableByteChannel source = Channels.newChannel(url.openStream());
				final FileChannel fileChannel = FileChannel.open(downloadTo, CREATE, WRITE, TRUNCATE_EXISTING)
		) {
			downloadTo.getParent().toFile().mkdirs();
			fileChannel.transferFrom(source, 0, Long.MAX_VALUE);
		}
	}

	static String toHex(final byte[] hash) {
		final StringBuilder sb = new StringBuilder(hash.length * 2);
		for (byte aHash : hash) {
			sb.append(String.format("%02X", aHash & 0xFF));
		}
		return sb.toString();
	}

	public static class VersionInfo {

		@SerializedName("refs")
		private Refs refs;

		@SerializedName("name")
		private String name;

		@SerializedName("description")
		private String description;

		@SerializedName("toolsVersion")
		private int toolsVersion;

		@SerializedName("javaVersions")
		private List<Integer> javaVersions;

		public Refs getRefs() {
			return refs;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public int getToolsVersion() {
			return toolsVersion;
		}

		public List<Integer> getJavaVersions() {
			return javaVersions;
		}

		@Override
		public String toString() {
			return
					"VersionInfo{" +
							"refs = '" + refs + '\'' +
							",name = '" + name + '\'' +
							",description = '" + description + '\'' +
							",toolsVersion = '" + toolsVersion + '\'' +
							",javaVersions = '" + javaVersions + '\'' +
							"}";
		}

		public static class Refs {

			@SerializedName("BuildData")
			private String buildData;

			@SerializedName("CraftBukkit")
			private String craftBukkit;

			@SerializedName("Bukkit")
			private String bukkit;

			@SerializedName("Spigot")
			private String spigot;

			public String getBuildData() {
				return buildData;
			}

			public String getCraftBukkit() {
				return craftBukkit;
			}

			public String getBukkit() {
				return bukkit;
			}

			public String getSpigot() {
				return spigot;
			}

			@Override
			public String toString() {
				return
						"Refs{" +
								"buildData = '" + buildData + '\'' +
								",craftBukkit = '" + craftBukkit + '\'' +
								",bukkit = '" + bukkit + '\'' +
								",spigot = '" + spigot + '\'' +
								"}";
			}
		}
	}

	public static class BuildDataInfo {

		@SerializedName("memberMapCommand")
		private String memberMapCommand;

		@SerializedName("packageMappings")
		private String packageMappings;

		@SerializedName("classMapCommand")
		private String classMapCommand;

		@SerializedName("finalMapCommand")
		private String finalMapCommand;

		@SerializedName("serverUrl")
		private String serverUrl;

		@SerializedName("toolsVersion")
		private int toolsVersion;

		@SerializedName("minecraftHash")
		private String minecraftHash;

		@SerializedName("minecraftVersion")
		private String minecraftVersion;

		@SerializedName("accessTransforms")
		private String accessTransforms;

		@SerializedName("memberMappings")
		private String memberMappings;

		@SerializedName("decompileCommand")
		private String decompileCommand;

		@SerializedName("classMappings")
		private String classMappings;

		public String getMemberMapCommand() {
			return memberMapCommand;
		}

		public String getPackageMappings() {
			return packageMappings;
		}

		public String getClassMapCommand() {
			return classMapCommand;
		}

		public String getFinalMapCommand() {
			return finalMapCommand;
		}

		public String getServerUrl() {
			return serverUrl;
		}

		public int getToolsVersion() {
			return toolsVersion;
		}

		public String getMinecraftHash() {
			return minecraftHash;
		}

		public String getMinecraftVersion() {
			return minecraftVersion;
		}

		public String getAccessTransforms() {
			return accessTransforms;
		}

		public String getMemberMappings() {
			return memberMappings;
		}

		public String getDecompileCommand() {
			return decompileCommand;
		}

		public String getClassMappings() {
			return classMappings;
		}
	}
}
