pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://wav.jfrog.io/artifactory/repo/")
    }
}

rootProject.name = "Yatopia"

include("Yatopia-API", "Yatopia-Server")