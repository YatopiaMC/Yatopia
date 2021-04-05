package task

import org.gradle.api.Project
import org.gradle.api.Task
import taskGroup
import upstreams
import gitCmd
import toothpick
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import ensureSuccess

internal fun Project.createFixBranchesTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("fixBranches") {
    receiver(this)
    group = taskGroup
    val folderArray = arrayListOf("api", "server")
    doLast {
        for (folder in folderArray) {
            val subprojectWorkDir = Paths.get("${toothpick.forkName}-${if (folder == "api") {"API"} else {"Server"}}").toFile()
            // val currentBranchCommits = gitCmd("--no-pager", "log", "${toothpick.forkName}-$folder...${toothpick.upstreamBranch}", "--pretty=oneline",
            val currentBranchCommits = gitCmd("--no-pager", "log", "master...${toothpick.upstreamBranch}", "--pretty=oneline",
                dir = subprojectWorkDir).output.toString()
            val nameMap = ConcurrentHashMap<String, String>()
            for (upstream in upstreams) {
                val patchPath = Paths.get("${upstream.patchPath}/$folder").toFile()
                if (patchPath.listFiles()?.isEmpty() != false) continue
                val commitName = gitCmd("--no-pager", "log", "${upstream.name}-$folder", "-1", "--format=\"%s\"",
                    dir = subprojectWorkDir).output.toString()
                val branchName = "${upstream.name}-$folder"
                val commitNameFiltered = commitName.substring(1, commitName.length-1)
                for (line in currentBranchCommits.split("\\n".toRegex()).stream().parallel()) {
                    val commitNameIterator = line.substring(41, line.length)
                    if (commitNameIterator == commitNameFiltered) {
                        val hash = line.substring(0, 40)
                        nameMap.put(branchName, hash)
                        continue
                    }
                }
            }
            for (upstream in upstreams) {
                val patchPath = Paths.get("${upstream.patchPath}/$folder").toFile()
                if (patchPath.listFiles()?.isEmpty() != false) continue
                val branchName = "${upstream.name}-$folder"
                ensureSuccess(gitCmd("checkout", branchName, dir = subprojectWorkDir, printOut = true))
                    ensureSuccess(gitCmd("reset", "--hard", nameMap.get(branchName) as String, dir = subprojectWorkDir,
                        printOut = true))
            }
            // ensureSuccess(gitCmd("checkout", "${toothpick.forkName}-$folder", dir = subprojectWorkDir,
            ensureSuccess(gitCmd("checkout", "master", dir = subprojectWorkDir,
                printOut = true))
        }
    }
}