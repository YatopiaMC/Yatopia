package task

import Upstream
import ensureSuccess
import gitCmd
import org.apache.tools.ant.util.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import rootProjectDir
import taskGroup
import toothpick
import upstreamDir
import upstreams
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


internal fun Project.createUpdateUpstreamTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("updateUpstream") {
    receiver(this)
    group = taskGroup
    doLast {
        ensureSuccess(gitCmd("fetch", dir = upstreamDir, printOut = true))
        ensureSuccess(gitCmd("reset", "--hard", toothpick.upstreamBranch, dir = upstreamDir, printOut = true))
        ensureSuccess(gitCmd("add", toothpick.upstream, dir = rootProjectDir, printOut = true))
        for (upstream in upstreams) {
            ensureSuccess(gitCmd("fetch", dir = upstream.repoPath.toFile(), printOut = true))
            ensureSuccess(gitCmd("reset", "--hard", upstream.branch, dir = upstream.repoPath.toFile(), printOut = true))
            ensureSuccess(gitCmd("add", "upstream/${upstream.name}", dir = rootProjectDir, printOut = true))
        }
        ensureSuccess(gitCmd("submodule", "update", "--init", "--recursive", dir = upstreamDir, printOut = true))
        val fileUtils = FileUtils.getFileUtils()
        for (upstream in upstreams) {
            val serverRepoPatches = upstream.getRepoServerPatches()
            val apiRepoPatches = upstream.getRepoAPIPatches()
            val serverPatches = upstream.serverList
            val apiPatches = upstream.apiList
            logger.lifecycle(">>> Pulling ${upstream.name} patches")
            updatePatches(serverRepoPatches, upstream, fileUtils, serverPatches, "server")
            updatePatches(apiRepoPatches, upstream, fileUtils, apiPatches, "api")
            upstream.updateUpstreamCommitHash()
        }
    }
}

private fun updatePatches(
    repoPatches: MutableList<String>?,
    upstream: Upstream,
    fileUtils: FileUtils,
    patches: MutableList<String>?,
    folder: String
) {
    if (repoPatches != null) {
        var i = 0
        val currentPatchList = Paths.get("${upstream.patchPath}/$folder").toFile().listFiles() as Array<File>?
        val tmpFolder = Paths.get("${upstream.patchPath}/tmp/$folder").toFile()
        tmpFolder.mkdirs()
        if (currentPatchList != null) {
            for (patch in currentPatchList) {
                if (patch.exists()) {
                    fileUtils.copyFile(
                        "${upstream.patchPath}/$folder/${patch.name}",
                        "${upstream.patchPath}/tmp/$folder/${patch.name}"
                    )
                    patch.delete()
                }
            }
        }
        val currentPatchListFiltered = currentPatchList?.toList()
            ?.stream()?.sorted()?.map { patch -> patch.name.substring(5, patch.name.length) }
            ?.collect(Collectors.toList())
        for (patch in repoPatches) {
            if (patches != null) {
                if ((patches.contains(patch) && upstream.useBlackList) || (!patches.contains(patch) && !upstream.useBlackList)) {
                    continue
                } else {
                    i++
                    updatePatch(fileUtils, upstream, repoPatches, patch, i, folder, currentPatchListFiltered)
                }
            }
        }
        val tmpFolderList = tmpFolder.listFiles()
        if (tmpFolderList != null) {
            for (patch in tmpFolderList) {
                patch.delete()
            }
        }
    }
}

private fun updatePatch(
    fileUtils: FileUtils,
    upstream: Upstream,
    serverRepoPatches: MutableList<String>,
    patch: String,
    i: Int,
    folder: String,
    currentPatchListFiltered: MutableList<String>?
) {
    if (currentPatchListFiltered == null || patchHasDiff(upstream, serverRepoPatches, patch, folder, currentPatchListFiltered)) {
        fileUtils.copyFile("${upstream.repoPath}/patches/$folder/" +
                "${String.format("%04d", serverRepoPatches.indexOf(patch) + 1)}-$patch",
            "${upstream.patchPath}/$folder/${String.format("%04d", i)}-$patch"
        )
    } else {
        fileUtils.copyFile("${upstream.patchPath}/tmp/$folder/" +
                "${String.format("%04d", currentPatchListFiltered.indexOf(patch) + 1)}-$patch",
            "${upstream.patchPath}/$folder/${String.format("%04d", i)}-$patch"
        )
    }
}

fun patchHasDiff(
    upstream: Upstream,
    serverRepoPatches: MutableList<String>,
    patch: String,
    folder: String,
    currentPatchListFiltered: MutableList<String>
): Boolean {
    if (!Paths.get("${upstream.patchPath}/tmp/$folder/${String.format("%04d", currentPatchListFiltered.indexOf(patch) + 1)}-$patch").toFile().isFile) return true
    if (!patchChanged(upstream, serverRepoPatches, patch, folder)) return false
    val upstreamFile = Files.readAllLines(Paths.get("${upstream.repoPath}/patches/$folder/${String.format("%04d", serverRepoPatches.indexOf(patch) + 1)}-$patch"), StandardCharsets.UTF_8)
    val repoFile = Files.readAllLines(Paths.get("${upstream.patchPath}/tmp/$folder/${String.format("%04d", currentPatchListFiltered.indexOf(patch) + 1)}-$patch"), StandardCharsets.UTF_8)
    return upstreamFile.stream().filter {line -> line.startsWith("+") || line.startsWith("-")}
        .filter {line -> if (line.startsWith("---") || line.startsWith("+++")) {
            line.substring(3, line.length).trim().isNotBlank()
        }
        else if (line.startsWith("--") || line.startsWith("++")) {
            line.substring(2, line.length).trim().isNotBlank()
        }
        else {
            line.substring(1, line.length).trim().isNotBlank()
        } }
        .filter {line -> if (repoFile.contains(line)) {
            repoFile.remove(line)
            return@filter false
        } else { return@filter true } }.collect(Collectors.toList()).isNotEmpty()
}

fun patchChanged(
    upstream: Upstream,
    serverRepoPatches: MutableList<String>,
    patch: String,
    folder: String
): Boolean {
    val diffCheckCmdResult = upstream.project.gitCmd("diff", "--name-only", upstream.upstreamCommit, upstream.getCurrentCommitHash(), dir = upstream.repoPath.toFile() )
    val diffCheckResult = diffCheckCmdResult.output.toString()
    if (diffCheckResult.isBlank()) return false
    val diffCheckChangeFiles = diffCheckResult.split("\\n".toRegex()).toTypedArray().toList()
    return diffCheckChangeFiles.contains("patches/$folder/${String.format("%04d", serverRepoPatches.indexOf(patch) + 1)}-$patch")
}