package task

import gitCmd
import org.gradle.api.Project
import org.gradle.api.Task
import taskGroup
import upstreamDir
import upstreams

internal fun Project.createInitGitSubmodulesTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("initGitSubmodules") {
    receiver(this)
    group = taskGroup
    var upstreamNotInit = false
    for (upstream in upstreams) { upstreamNotInit = upstreamNotInit || upstream.repoPath.toFile().resolve(".git").exists() }
    onlyIf { !upstreamDir.resolve(".git").exists() || upstreamNotInit }
    doLast {
        var exit = gitCmd("submodule", "update", "--init", printOut = true).exitCode
        exit += gitCmd("submodule", "update", "--init", "--recursive", dir = upstreamDir, printOut = true).exitCode
        if (exit != 0) {
            error("Failed to checkout git submodules: git exited with code $exit")
        }
    }
}
