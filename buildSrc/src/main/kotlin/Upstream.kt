import org.gradle.api.Project
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

open class Upstream(in_name: String, in_useBlackList: Boolean, in_list: String, in_rootProjectDir: File, in_branch: String, in_id: Int, in_project: Project) {
    var name: String = in_name
    var useBlackList: Boolean = in_useBlackList
    private var list: ArrayList<String> = ArrayList(in_list.split(",".toRegex()))
    private var rootProjectDir: File = in_rootProjectDir
    var branch = in_branch
    var id = in_id

    var serverList = list.stream().filter { patch -> patch.startsWith("server/") }
        ?.sorted()?.map { patch -> patch.substring(7, patch.length) }?.collect(Collectors.toList())
    var apiList = list.stream().filter { patch -> patch.startsWith("api/") }
        ?.sorted()?.map { patch -> patch.substring(4, patch.length) }?.collect(Collectors.toList())


    var patchPath = Paths.get("$rootProjectDir/patches/$name/patches")
    var repoPath = Paths.get("$rootProjectDir/upstream/$name")

    var project = in_project

    var upstreamCommit = getUpstreamCommitHash()

    private fun getUpstreamCommitHash(): String {
        val commitFileFolder = Paths.get("$rootProjectDir/upstreamCommits")
        val commitFilePath = Paths.get("$commitFileFolder/$name")
        val commitFile = commitFilePath.toFile()
        var commitHash: String
        if (commitFile.isFile) {
            commitHash = Files.readAllLines(commitFilePath).toString()
            commitHash = commitHash.substring(1, commitHash.length - 1)
            if (commitHash == "") {
                commitHash = updateHashFile(commitFile)
            }
        } else {
            Files.createFile(commitFilePath)
            commitHash = updateHashFile(commitFile)
        }
        return commitHash;
    }

    public fun updateUpstreamCommitHash() {
        val commitFileFoler = Paths.get("$rootProjectDir/upstreamCommits")
        val commitFilePath = Paths.get("$commitFileFoler/$name")
        val commitFile = commitFilePath.toFile()
        updateHashFile(commitFile)
        upstreamCommit = getUpstreamCommitHash()
    }

    public fun getCurrentCommitHash(): String {
        return project.getCommitHash()
    }

    private fun updateHashFile(commitFile: File): String {
        var commitHash: String
        commitHash = project.getCommitHash()
        val fileWriter = FileWriter(commitFile)
        fileWriter.use { out -> out.write(commitHash) }
        fileWriter.close()
        return commitHash
    }

    private fun Project.getCommitHash(): String = gitHash(repo = repoPath.toFile())

    public fun getRepoServerPatches(): MutableList<String>? {
        return getRepoPatches(rootProjectDir.resolve("$repoPath/patches/server")).stream()
            .sorted().map {patch -> patch.substring(5, patch.length) }.collect(Collectors.toList())
    }

    public fun getRepoAPIPatches(): MutableList<String>? {
        return getRepoPatches(rootProjectDir.resolve("$repoPath/patches/api")).stream()
            .sorted().map {patch -> patch.substring(5, patch.length) }.collect(Collectors.toList())
    }

    private fun getRepoPatches(path: File): ArrayList<String> {
        val files = path.listFiles()
        val filesList = ArrayList<String>()
        for (patch in files) {
            filesList.add(patch.name)
        }
        return filesList
    }
}
