import org.yatopiamc.toothpick.*

plugins {
    `java-library`
    `maven-publish`
    id("org.yatopiamc.toothpick") version "1.0.1-SNAPSHOT"
}

toothpick {
    forkName = "Yatopia"
    groupId = "org.yatopiamc"
    val versionTag = System.getenv("BUILD_NUMBER")
        ?: "\"${gitCmd("rev-parse", "--short", "HEAD").output}\""
    if(!System.getenv("BRANCH_NAME").isNullOrEmpty()) {
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

    minecraftVersion = "1.17"
    nmsPackage = "1_17_R1"
    nmsRevision = "R0.1-SNAPSHOT"

    upstream = "Paper"
    upstreamBranch = "origin/master"

    paperclipName = "yatopia-$minecraftVersion-paperclip.jar"

    patchCreditsOutput = "PATCHES.md"
    patchCreditsTemplate = ".template.md"

    server {
        project = project(":$forkNameLowercase-server")
        patchesDir = rootProject.projectDir.resolve("patches/server")
    }
    api {
        project = project(":$forkNameLowercase-api")
        patchesDir = rootProject.projectDir.resolve("patches/api")
    }

    logger.lifecycle("Configured version string: $calcVersionString")
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://mvn.thearcanebrony.net/maven-public/")
        mavenLocal()
        maven("${rootProjectDir}/.repository")
    }

    java {
        if(JavaVersion.VERSION_16 > JavaVersion.current()){
            error("This build must be run with Java 16 or newer")
        }
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
        withSourcesJar()
    }

tasks.withType<JavaCompile>().configureEach {
    options.isIncremental = true
    options.isFork = true
    options.encoding = "UTF-8"
    options.release.set(16)
    }
}

