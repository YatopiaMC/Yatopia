
import xyz.jpenilla.toothpick.gitCmd
import xyz.jpenilla.toothpick.toothpick

plugins {
    `java-library`
    id("xyz.jpenilla.toothpick") version "1.0.0-SNAPSHOT"
}

toothpick {
    forkName = "Yatopia"
    groupId = "org.yatopiamc"
    val versionTag = System.getenv("BUILD_NUMBER")
        ?: "\"${gitCmd("rev-parse", "--short", "HEAD").output}\""
    
    var currentBranch = ""
    if (!System.getenv("BRANCH_NAME").isNullOrEmpty()) {
        currentBranch = System.getenv("BRANCH_NAME")
    } else if (!System.getenv("GITHUB_HEAD_REF").isNullOrEmpty()) {
        currentBranch = System.getenv("GITHUB_HEAD_REF")
    } else if (!System.getenv("GITHUB_REF").isNullOrEmpty()) {
        currentBranch = System.getenv("GITHUB_REF").substring("refs/heads/".length)
    } else {
        currentBranch = gitCmd("rev-parse", "--abbrev-ref", "HEAD").output.toString().trim()
        if(currentBranch == "HEAD") logger.warn("You are currently in \'detached HEAD\' state, branch information isn\'t available")
    }
    
    forkVersion = "git-$forkName-$currentBranch-$versionTag"
    forkUrl = "https://github.com/YatopiaMC/Yatopia"

    minecraftVersion = "1.16.5"
    nmsPackage = "1_16_R3"
    nmsRevision = "R0.1-SNAPSHOT"

    upstream = "Purpur"
    upstreamBranch = "origin/ver/1.16.5"

    paperclipName = "yatopia-$minecraftVersion-paperclip"

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
        maven("https://jitpack.io")
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
