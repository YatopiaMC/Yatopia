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
import java.nio.file.Paths
import bashCmd

internal fun Project.createApplyPatchesTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("applyPatches") {
    receiver(this)
    group = taskGroup

    fun checkCursed(project: Project): Boolean {
        return project.properties.getOrDefault("cursed", "false").toString().toBoolean()
    }

    fun applyPatches(patchDir: Path, applyName: String, name: String, wasGitSigningEnabled: Boolean, projectDir: File): Boolean {
        if (Files.notExists(patchDir)) return true

        val patchPaths = Files.newDirectoryStream(patchDir)
            .map { it.toFile() }
            .filter { it.name.endsWith(".patch") }
            .sorted()
            .takeIf { it.isNotEmpty() } ?: return true
        val patches = patchPaths.map { it.absolutePath }.toTypedArray()

        logger.lifecycle(">>> Applying $applyName patches to $name")

        gitCmd("am", "--abort")

        //Cursed Apply Mode that makes fixing stuff a lot easier
        if (checkCursed(project)) {
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

    fun applyPatchesYarn(): Boolean { // Todo actually port to kotlin
        val projectDir = Paths.get("${rootProject.projectDir}/$forkName-Server_yarn").toFile()
        val importDir = Paths.get("${rootProject.projectDir}/mappings/work/$forkName-Server_yarn_unpatched").toFile()
        logger.lifecycle(">>> Resetting subproject $name")
        if (projectDir.exists()) {
            ensureSuccess(gitCmd("fetch", "origin", dir = projectDir))
            ensureSuccess(gitCmd("reset", "--hard", "origin/master", dir = projectDir))
        } else {
            ensureSuccess(gitCmd("clone", importDir.toString(), projectDir.toString(), printOut = true))
            ensureSuccess(gitCmd("checkout", "-b", "upstream/upstream", printOut = true, dir = projectDir))
            ensureSuccess(gitCmd("checkout", "master", printOut = true, dir = projectDir))
        }
        logger.lifecycle(">>> Done resetting subproject $name")

        projectDir.mkdirs()
        val applyName = "mappedPatches"
        val name = "$forkName-Server_yarn"
        val patchDir: Path = Paths.get("${rootProject.projectDir}/mappedPatches")
        if (Files.notExists(patchDir)) return true
        

        val patchPaths = Files.newDirectoryStream(patchDir)
            .map { it.toFile() }
            .filter { it.name.endsWith(".patch") }
            .sorted()
            .takeIf { it.isNotEmpty() } ?: return true
        val patches = patchPaths.map { it.absolutePath }.toTypedArray()

        logger.lifecycle(">>> Applying $applyName patches to $name")

        gitCmd("am", "--abort")

        //Cursed Apply Mode that makes fixing stuff a lot easier
        if (checkCursed(project)) {
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
        }
        return false;
    }

    fun initYarn() { // Todo actually port to kotlin
        val paperDecompDir = toothpick.paperDecompDir
        bashCmd("cd mappings/mapper && ./gradlew installDist", printOut = true)
        bashCmd("rm -fr mappings/work/Base", printOut = true)
        bashCmd("mkdir -p mappings/work/Base/src/main/java/com/mojang", printOut = true)
        bashCmd("cp -r $forkName-Server/src/main/java/* mappings/work/Base/src/main/java/", printOut = true)
        bashCmd("cp -r $paperDecompDir/libraries/com.mojang/*/* mappings/work/Base/src/main/java/", printOut = true)
        bashCmd("rm -fr mappings/work/$forkName-Server_yarn_unpatched && mkdir -p mappings/work/$forkName-Server_yarn_unpatched/src/main/java", printOut = true)
        bashCmd("cp $forkName-Server/.gitignore $forkName-Server/pom.xml $forkName-Server/checkstyle.xml $forkName-Server/CONTRIBUTING.md $forkName-Server/LGPL.txt $forkName-Server/LICENCE.txt $forkName-Server/README.md mappings/work/$forkName-Server_yarn_unpatched/", printOut = true)
        bashCmd("JAVA_OPTS='-Xms1G -Xmx2G' mappings/mapper/build/install/mapper/bin/mapper mappings/map.srg mappings/work/Base/src/main/java mappings/work/$forkName-Server_yarn_unpatched/src/main/java", printOut = true)
        bashCmd("find -name '*.java' | xargs --max-procs=4 --no-run-if-empty sed -i '/^import [a-zA-Z0-9]*;$/d'", dir = File("${rootProject.projectDir}/mappings/work/$forkName-Server_yarn_unpatched/src/main/java"))
        bashCmd("git init && git add . && git commit --quiet --message=init", dir = File("${rootProject.projectDir}/mappings/work/$forkName-Server_yarn_unpatched"), printOut = true)
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
                ensureSuccess(gitCmd("clone", sourceRepo.absolutePath, projectDir.absolutePath, printOut = true))
            }
            logger.lifecycle(">>> Done resetting subproject $name")

            val wasGitSigningEnabled = temporarilyDisableGitSigning(projectDir)

            for (upstream in upstreams) {
                if (((folder == "server" && upstream.serverList?.isEmpty() != false) || (folder == "api" && upstream.apiList?.isEmpty() != false)) && !upstream.useBlackList) continue
                if (((folder == "server" && upstream.getRepoServerPatches()?.isEmpty() != false) || (folder == "api" && upstream.getRepoAPIPatches()?.isEmpty() != false)) && upstream.useBlackList) continue
                project.gitCmd("branch", "-D", "${upstream.name}-$folder", dir = projectDir)
                project.gitCmd("checkout", "-b", "${upstream.name}-$folder", dir = projectDir)
                // Apply patches
                val patchDir = Paths.get("${upstream.patchPath}/$folder")

                if (applyPatches(patchDir, upstream.name, name, wasGitSigningEnabled, projectDir)) continue
            }
            // project.gitCmd("branch", "-D", "$forkName-$folder", dir = projectDir)
            // project.gitCmd("checkout", "-b", "$forkName-$folder", dir = projectDir)
            project.gitCmd("branch", "-D", "master", dir = projectDir)
            project.gitCmd("checkout", "-b", "master", dir = projectDir)
            val patchDir = patchesDir.toPath()
            // Apply patches
            if (applyPatches(patchDir, forkName, name, wasGitSigningEnabled, projectDir)) continue

            if (wasGitSigningEnabled) reEnableGitSigning(projectDir)
            logger.lifecycle(">>> Done applying patches to $name")
        }
        bashCmd("rm -fr patches/server/*-Mapped-Patches.patch")

        initYarn()
        if (applyPatchesYarn()) {}
        
    }
}
