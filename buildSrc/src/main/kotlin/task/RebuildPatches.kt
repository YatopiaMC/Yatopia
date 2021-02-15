package task

import ensureSuccess
import forkName
import gitCmd
import org.gradle.api.Project
import org.gradle.api.Task
import taskGroup
import toothpick
import upstreams
import java.io.File
import java.nio.file.Paths
import Upstream

@Suppress("UNUSED_VARIABLE")
internal fun Project.createRebuildPatchesTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("rebuildPatches") {
    receiver(this)
    group = taskGroup
    doLast {
        for ((name, subproject) in toothpick.subprojects) {
            val (sourceRepo, projectDir, patchesDir) = subproject
            var previousUpstreamName = "origin/master"
            val folder = (if (patchesDir.endsWith("server")) "server" else "api")

            for (upstream in upstreams) {
                val patchPath = Paths.get("${upstream.patchPath}/$folder").toFile()

                if (patchPath.listFiles()?.isEmpty() != false) continue

                updatePatches(patchPath, upstream.name, folder, projectDir, previousUpstreamName)
                previousUpstreamName = "${upstream.name}-$folder"
            }
            ensureSuccess(gitCmd("checkout", "$forkName-$folder", dir = projectDir,
                printOut = true))

            updatePatches(patchesDir, toothpick.forkName, folder, projectDir, previousUpstreamName)

            logger.lifecycle(">>> Done rebuilding patches for $name")
        }
    }
}

private fun Project.updatePatches(
    patchPath: File,
    name: String,
    folder: String,
    projectDir: File,
    previousUpstreamName: String
) {
    logger.lifecycle(">>> Rebuilding patches for $name-$folder")
    if (!patchPath.exists()) {
        patchPath.mkdirs()
    }
    // Nuke old patches
    patchPath.listFiles()
        ?.filter { it -> it.name.endsWith(".patch") }
        ?.forEach { it -> it.delete() }

    ensureSuccess(
        gitCmd(
            "checkout", "$name-$folder", dir = projectDir,
            printOut = true
        )
    )
    ensureSuccess(
        gitCmd(
            "format-patch",
            "--no-stat", "--zero-commit", "--full-index", "--no-signature", "-N",
            "-o", patchPath.absolutePath, previousUpstreamName,
            dir = projectDir,
            printOut = false
        )
    )
    gitCmd(
        "add", patchPath.canonicalPath,
        dir = patchPath,
        printOut = true
    )
}
