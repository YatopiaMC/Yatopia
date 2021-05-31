import java.util.Locale

val forkName = "Yatopia"
val forkNameLowercase = forkName.toLowerCase(Locale.ENGLISH)

rootProject.name = forkNameLowercase

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://mvn.thearcanebrony.net/repository/maven-public/")
        maven("https://jitpack.io/")
        mavenCentral()
    }
}

setupSubproject("$forkNameLowercase-api") {
    projectDir = File("$forkName-API")
    buildFileName = "../subprojects/api.gradle.kts"
}
setupSubproject("$forkNameLowercase-server") {
    projectDir = File("$forkName-Server")
    buildFileName = "../subprojects/server.gradle.kts"
}
setupSubproject("Yatoclip") { }

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
