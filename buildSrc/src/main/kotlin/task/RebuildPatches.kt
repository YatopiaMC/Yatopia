package task

import ensureSuccess
import forkName
import gitCmd
import org.gradle.api.Project
import org.gradle.api.Task
import taskGroup
import toothpick
import upstreams
import java.nio.file.Paths

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

                if (!Paths.get("${upstream.patchPath}/$folder").toFile().exists()) {
                    patchPath.mkdirs()
                }

                logger.lifecycle(">>> Rebuilding patches for ${upstream.name}")

                if (patchPath.listFiles().isEmpty()) continue

                // Nuke old patches
                patchPath.listFiles()
                    ?.filter { it -> it.name.endsWith(".patch") }
                    ?.forEach { it -> it.delete() }
                ensureSuccess(gitCmd("checkout", "${upstream.name}-$folder", dir = projectDir))
                ensureSuccess(
                    gitCmd(
                        "format-patch",
                        "--no-stat", "--zero-commit", "--full-index", "--no-signature", "-N",
                        "-o", patchPath.absolutePath, previousUpstreamName,
                        dir = projectDir,
                        printOut = true
                    )
                )
                previousUpstreamName = "${upstream.name}-$folder"
            }
            ensureSuccess(gitCmd("checkout", "$forkName-$folder", dir = projectDir))
            if (!patchesDir.exists()) {
                patchesDir.mkdirs()
            }

            logger.lifecycle(">>> Rebuilding patches for $name")

            // Nuke old patches
            patchesDir.listFiles()
                ?.filter { it -> it.name.endsWith(".patch") }
                ?.forEach { it -> it.delete() }

            // And generate new
            ensureSuccess(
                gitCmd(
                    "format-patch",
                    "--no-stat", "--zero-commit", "--full-index", "--no-signature", "-N",
                    "-o", patchesDir.absolutePath, previousUpstreamName,
                    dir = projectDir,
                    printOut = true
                )
            )

            logger.lifecycle(">>> Done rebuilding patches for $name")
        }
    }
}
