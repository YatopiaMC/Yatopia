package task

import ensureSuccess
import forkName
import gitCmd
import org.gradle.api.Project
import org.gradle.api.Task
import reEnableGitSigning
import taskGroup
import temporarilyDisableGitSigning
import toothpick
import upstreams
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

internal fun Project.createApplyPatchesTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("applyPatches") {
    receiver(this)
    group = taskGroup
    fun applyPatches(patchDir: Path, applyName: String, name: String, wasGitSigningEnabled: Boolean, projectDir: File): Boolean {
        val patchPaths = Files.newDirectoryStream(patchDir)
            .map { it.toFile() }
            .filter { it.name.endsWith(".patch") }
            .sorted()
            .takeIf { it.isNotEmpty() } ?: return true
        val patches = patchPaths.map { it.absolutePath }.toTypedArray()

        logger.lifecycle(">>> Applying $applyName patches to $name")

        gitCmd("am", "--abort")

        //Cursed Apply Mode that makes fixing stuff a lot easier
        if (false) {
            for (patch in patches) {
                val gitCommand = arrayListOf("am", "--3way", "--ignore-whitespace",
                    "--rerere-autoupdate", "--whitespace=fix", "--reject", "-C0", patch)
                if (gitCmd(*gitCommand.toTypedArray(), dir = projectDir, printOut = true).exitCode != 0) {
                    gitCmd("add", ".", dir = projectDir, printOut = true)
                    gitCmd("am", "--continue", dir = projectDir, printOut = true)
                }
            }
        } else {
            val gitCommand = arrayListOf("am", "--3way", "--ignore-whitespace",
                "--rerere-autoupdate", "--whitespace=fix",  *patches)
            ensureSuccess(gitCmd(*gitCommand.toTypedArray(), dir = projectDir, printOut = true)) {
                if (wasGitSigningEnabled) reEnableGitSigning(projectDir)
            }
        }

        return false;
    }

    doLast {
        for ((name, subproject) in toothpick.subprojects) {
            val (sourceRepo, projectDir, patchesDir) = subproject

            val folder = (if (patchesDir.endsWith("server")) "server" else "api")

            // Reset or initialize subproject
            logger.lifecycle(">>> Resetting subproject $name")
            if (projectDir.exists()) {
                ensureSuccess(gitCmd("fetch", "origin", dir = projectDir))
                ensureSuccess(gitCmd("reset", "--hard", "origin/master", dir = projectDir))
            } else {
                ensureSuccess(gitCmd("clone", sourceRepo.absolutePath, projectDir.absolutePath))
            }
            logger.lifecycle(">>> Done resetting subproject $name")

            val wasGitSigningEnabled = temporarilyDisableGitSigning(projectDir)

            for (upstream in upstreams) {
                if (((folder == "server" && upstream.serverList?.isEmpty() != false) || (folder == "api" && upstream.apiList?.isEmpty() != false)) && !upstream.useBlackList) continue
                project.gitCmd("branch", "-D", "${upstream.name}-$folder", dir = projectDir)
                project.gitCmd("checkout", "-b", "${upstream.name}-$folder", dir = projectDir)
                // Apply patches
                val patchDir = Path.of("${upstream.patchPath}/$folder")

                if (applyPatches(patchDir, upstream.name, name, wasGitSigningEnabled, projectDir)) continue
            }
            project.gitCmd("branch", "-D", "$forkName-$folder", dir = projectDir)
            project.gitCmd("checkout", "-b", "$forkName-$folder", dir = projectDir)
            val patchDir = patchesDir.toPath()
            // Apply patches
            if (applyPatches(patchDir, forkName, name, wasGitSigningEnabled, projectDir)) continue

            if (wasGitSigningEnabled) reEnableGitSigning(projectDir)
            logger.lifecycle(">>> Done applying patches to $name")
        }
    }
}
