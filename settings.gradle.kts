import java.util.Locale

val forkName = "Yatopia"
val forkNameLowercase = forkName.toLowerCase(Locale.ENGLISH)

rootProject.name = forkNameLowercase

pluginManagement {
    repositories {
        gradlePluginPortal()
                maven("https://wav.jfrog.io/artifactory/repo/")
        maven("https://mvn.thearcanebrony.net/repository/maven-public/")
        maven("https://jitpack.io/")
        mavenCentral()
    }
}
