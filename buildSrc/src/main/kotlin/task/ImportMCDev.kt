package task

import LibraryImport
import ensureSuccess
import gitCmd
import internalTaskGroup
import libraryImports
import nmsImports
import org.gradle.api.Project
import org.gradle.api.Task
import toothpick
import upstreams
import java.io.File

internal fun Project.createImportMCDevTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("importMCDev") {
    receiver(this)
    group = internalTaskGroup
    val upstreamServer = toothpick.serverProject.baseDir
    val importLog = arrayListOf("Extra mc-dev imports")

    fun importNMS(className: String) {
        logger.lifecycle("Importing n.m.s.$className")
        importLog.add("Imported n.m.s.$className")
        val source = toothpick.paperWorkDir.resolve("spigot/net/minecraft/server/$className.java")
        if (!source.exists()) error("Missing NMS: $className")
        val target = upstreamServer.resolve("src/main/java/net/minecraft/server/$className.java")
        source.copyTo(target)
    }

    fun importLibrary(import: LibraryImport) {
        val (group, lib, prefix, file) = import
        logger.lifecycle("Importing $group.$lib $prefix/$file")
        importLog.add("Imported $group.$lib $prefix/$file")
        val source = toothpick.paperWorkDir.resolve("libraries/$group/$lib/$prefix/$file.java")
        if (!source.exists()) error("Missing Base: $lib $prefix/$file")
        val targetDir = upstreamServer.resolve("src/main/java/$prefix")
        val target = targetDir.resolve("$file.java")
        targetDir.mkdirs()
        source.copyTo(target)
    }

    fun getAndApplyNMS(patchesDir: File) {
        (patchesDir.listFiles() ?: return).asSequence()
            .flatMap { it.readLines().asSequence() }
            .filter { it.startsWith("+++ b/src/main/java/net/minecraft/server/") }
            .distinct()
            .map { it.substringAfter("/server/").substringBefore(".java") }
            .filter { !upstreamServer.resolve("src/main/java/net/minecraft/server/$it.java").exists() }
            .map { toothpick.paperWorkDir.resolve("spigot/net/minecraft/server/$it.java") }
            .filter {
                val exists = it.exists()
                if (!it.exists()) logger.lifecycle("NMS ${it.nameWithoutExtension} is either missing, or is a new file added through a patch")
                exists
            }
            .map { it.nameWithoutExtension }
            .forEach(::importNMS)
    }

    doLast {
        logger.lifecycle(">>> Importing mc-dev")
        val lastCommitIsMCDev = gitCmd(
            "log", "-1", "--oneline",
            dir = upstreamServer
        ).output?.contains("Extra mc-dev imports") == true
        if (lastCommitIsMCDev) {
            ensureSuccess(
                gitCmd(
                    "reset", "--hard", "HEAD~1",
                    dir = upstreamServer,
                    printOut = true
                )
            )
        }
        for (upstream in upstreams) {
            val patchesDir = rootProject.projectDir.resolve("${upstream.patchPath}/server")
            getAndApplyNMS(patchesDir)
        }

        val patchesDir = toothpick.serverProject.patchesDir
        getAndApplyNMS(patchesDir)


        // Imports from MCDevImports.kt
        nmsImports.forEach(::importNMS)
        libraryImports.forEach(::importLibrary)

        val add = gitCmd("add", ".", "-A", dir = upstreamServer).exitCode == 0
        val commit = gitCmd("commit", "-m", importLog.joinToString("\n"), dir = upstreamServer).exitCode == 0
        if (!add || !commit) {
            logger.lifecycle(">>> Didn't import any extra files")
        }
        logger.lifecycle(">>> Done importing mc-dev")
    }
}
