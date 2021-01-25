import org.yatopia.yatoclip.gradle.PatchesMetadata
import java.util.Properties

plugins {
    java
    `java-library`
}

apply(plugin = "com.github.johnrengelman.shadow")

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    implementation("com.github.ishlandbukkit:jbsdiff:deff66b794")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("commons-io:commons-io:2.8.0")
}

tasks.register<org.yatopia.yatoclip.gradle.MakePatchesTask>("genPatches") {
    originalJar = rootProject.toothpick.paperDir.resolve("work").resolve("Minecraft").resolve(rootProject.toothpick.minecraftVersion).resolve("${rootProject.toothpick.minecraftVersion}-m.jar")
    targetJar = rootProject.toothpick.serverProject.project.tasks.getByName("shadowJar").outputs.files.singleFile
    setRelocations(rootProject.toothpick.serverProject.project.extensions.get("relocations") as java.util.HashSet<PatchesMetadata.Relocation>)
    dependsOn(rootProject.toothpick.serverProject.project.tasks.getByName("shadowJar"))
    doLast {
        val prop = Properties()
        prop.setProperty("minecraftVersion", rootProject.toothpick.minecraftVersion)
        org.yatopia.yatoclip.gradle.PropertiesUtils.saveProperties(prop, outputDir.toPath().parent.resolve("yatoclip-launch.properties"), "Yatoclip launch values")
    }
}

val shadowJar by tasks.getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    manifest {
        attributes(
            "Main-Class" to "org.yatopia.yatoclip.Yatoclip"
        )
    }
}

tasks.register<Copy>("copyJar") {
    val targetName = "yatopia-${rootProject.toothpick.minecraftVersion}-yatoclip.jar"
    from(shadowJar.outputs.files.singleFile) {
        rename { targetName }
    }

    into(rootProject.projectDir)

    doLast {
        logger.lifecycle(">>> $targetName saved to root project directory")
    }

    dependsOn(shadowJar)
}

tasks.getByName("processResources").dependsOn(tasks.getByName("genPatches"))
tasks.getByName("assemble").dependsOn(tasks.getByName("copyJar"))
tasks.getByName("jar").enabled = false
tasks.getByName("sourcesJar").enabled = false
val buildTask = tasks.getByName("build")
val buildTaskDependencies = kotlin.collections.HashSet(buildTask.dependsOn)
buildTask.setDependsOn(kotlin.collections.HashSet<Task>())
buildTask.onlyIf { false }
tasks.register("yatoclip") {
    buildTaskDependencies.forEach {
        dependsOn(it)
    }
}
