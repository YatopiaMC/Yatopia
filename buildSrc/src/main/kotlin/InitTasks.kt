import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import task.*

@Suppress("UNUSED_VARIABLE")
internal fun Project.initToothpickTasks() {
    gradle.taskGraph.whenReady {
        val fast = project.hasProperty("fast")
        tasks.withType<Test> {
            onlyIf { !fast }
        }
        tasks.withType<Javadoc> {
            onlyIf { !fast || gradle.taskGraph.allTasks.any { it.name.contains("publish", ignoreCase = true) } }
        }
    }

    tasks.getByName("build") {
        doFirst {
            val readyToBuild =
                upstreamDir.resolve(".git").exists()
                        && toothpick.subprojects.values.all { it.projectDir.exists() && it.baseDir.exists() }
            if (!readyToBuild) {
                error("Workspace has not been setup. Try running `./gradlew applyPatches` first")
            }
        }
    }

    val initGitSubmodules = createInitGitSubmodulesTask()

    val setupUpstream = createSetupUpstreamTask {
        dependsOn(initGitSubmodules)
    }

    val importMCDev = createImportMCDevTask {
        mustRunAfter(setupUpstream)
    }

    val paperclip = createPaperclipTask {
        val shadowJar = toothpick.serverProject.project.tasks.getByName("shadowJar")
        dependsOn(shadowJar)
        inputs.file(shadowJar.outputs.files.singleFile)
    }

    val applyPatches = createApplyPatchesTask {
        // If Paper has not been setup yet or if we modified the submodule (i.e. upstream update), patch
        if (!lastUpstream.exists()
            || !upstreamDir.resolve(".git").exists()
            || lastUpstream.readText() != gitHash(upstreamDir)
        ) {
            dependsOn(setupUpstream)
        }
        mustRunAfter(setupUpstream)
        dependsOn(importMCDev)
    }

    val patchCredits = createPatchCreditsTask()

    val fixBranch = createFixBranchesTask()

    val rebuildPatches = createRebuildPatchesTask {
        dependsOn(fixBranch)
        finalizedBy(patchCredits)
    }

    val updateUpstream = createUpdateUpstreamTask {
        finalizedBy(setupUpstream)
    }

    val upstreamCommit = createUpstreamCommitTask()

}
