plugins {
    `java-library`
    `maven-publish`
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

    if(!System.getenv("BRANCH_NAME").isNullOrEmpty()) {
        currentBranchDisplayName = System.getenv("BRANCH_NAME").replace("/${minecraftVersion}", "");
    } else if (!System.getenv("GITHUB_REF").isNullOrEmpty()) {
        currentBranchDisplayName = System.getenv("GITHUB_REF").substring("refs/heads/".length).replace("/${minecraftVersion}", "")
    } else {
        currentBranchDisplayName = gitCmd("rev-parse", "--abbrev-ref", "HEAD").output.toString().trim().replace("/${minecraftVersion}", "")
        if(currentBranchDisplayName == "HEAD") logger.warn("You are currently in \'detached HEAD\' state, branch information isn\'t available")
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
        mavenLocal()
        maven("${rootProjectDir}/.repository")
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
