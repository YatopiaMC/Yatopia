plugins {
    `java-library`
    toothpick
}

toothpick {
    forkName = "Yatopia"
    groupId = "org.yatopiamc"
    val versionTag = System.getenv("BUILD_NUMBER")
        ?: "\"${gitCmd("rev-parse", "--short", "HEAD").output}\""
    forkVersion = "git-$forkName-$versionTag"
    forkUrl = "https://github.com/YatopiaMC/Yatopia"

    minecraftVersion = "1.16.5"
    nmsPackage = "1_16_R3"
    nmsRevision = "R0.1-SNAPSHOT"

    upstream = "Paper"
    upstreamBranch = "origin/master"

    paperclipName = "Yatopia-launcher.jar"

    server {
        project = project(":$forkNameLowercase-server")
        patchesDir = rootProject.projectDir.resolve("patches/server")
    }
    api {
        project = project(":$forkNameLowercase-api")
        patchesDir = rootProject.projectDir.resolve("patches/api")
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.mrivanplays.com/repository/ivan/")
        mavenLocal()
    }

    java {
        if(JavaVersion.VERSION_1_8 > JavaVersion.current()){
            error("This build must be run with Java 8 or better")
        }
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.current()
        withSourcesJar()
    }
}