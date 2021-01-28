import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import java.io.File

val Project.toothpick: ToothpickExtension
    get() = rootProject.extensions.findByType(ToothpickExtension::class)!!

fun Project.toothpick(receiver: ToothpickExtension.() -> Unit) {
    toothpick.project = this
    receiver(toothpick)
    allprojects {
        group = toothpick.groupId
        version = toothpick.calcVersionString
    }
    configureSubprojects()
    initToothpickTasks()
}

val Project.lastUpstream: File
    get() = rootProject.projectDir.resolve("last-${toothpick.upstreamLowercase}")

val Project.rootProjectDir: File
    get() = rootProject.projectDir

val Project.upstreamDir: File
    get() = rootProject.projectDir.resolve(toothpick.upstream)

val Project.upstream: String
    get() = toothpick.upstream

val Project.upstreams: MutableList<Upstream>
    get() = toothpick.getUpstreams(rootProject.projectDir) as MutableList<Upstream>

val Project.forkName: String
    get() = toothpick.forkName

val Project.patchCreditsOutput: String
    get() = toothpick.patchCreditsOutput

val Project.patchCreditsTemplate: String
    get() = toothpick.patchCreditsTemplate
