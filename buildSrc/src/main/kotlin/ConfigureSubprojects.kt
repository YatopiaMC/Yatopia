import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
// import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import transformer.ModifiedLog4j2PluginsCacheFileTransformer
import relocation.ToothpickRelocator
import kotlinx.dom.elements
import kotlinx.dom.search
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.yatopiamc.yatoclip.gradle.MakePatchesTask
import org.yatopiamc.yatoclip.gradle.PatchesMetadata
import org.yatopiamc.yatoclip.gradle.PropertiesUtils
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

internal fun Project.configureSubprojects() {
    subprojects {
        apply<JavaLibraryPlugin>()
        apply<MavenPublishPlugin>()

        tasks.withType<JavaCompile> {
            options.encoding = UTF_8.name()
        }
        tasks.withType<Javadoc> {
            options.encoding = UTF_8.name()
        }

        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    groupId = rootProject.group as String
                    version = rootProject.version as String
                    pom {
                        name.set(project.name)
                        url.set(toothpick.forkUrl)
                    }
                }
            }
        }

        when {
            project.name.endsWith("server") -> configureServerProject()
            project.name.endsWith("api") -> configureApiProject()
        }
    }
    rootProject.project("Yatoclip") {
        configureYatoclipProject()
    }
}

private fun Project.configureYatoclipProject() {
    try {
        rootProject.toothpick.serverProject.project.extensions.getByName("relocations")
    } catch (e: UnknownDomainObjectException) {
        return
    }

    apply<JavaLibraryPlugin>()
    apply<ShadowPlugin>()

    tasks.register<MakePatchesTask>("genPatches") {
        originalJar = rootProject.toothpick.paperDir.resolve("work").resolve("Minecraft")
            .resolve(rootProject.toothpick.minecraftVersion).resolve("${rootProject.toothpick.minecraftVersion}-m.jar")
        targetJar = rootProject.toothpick.serverProject.project.tasks.getByName("shadowJar").outputs.files.singleFile
        setRelocations(rootProject.toothpick.serverProject.project.extensions.getByName("relocations") as HashSet<PatchesMetadata.Relocation>)
        dependsOn(rootProject.toothpick.serverProject.project.tasks.getByName("shadowJar"))
        doLast {
            val prop = Properties()
            prop.setProperty("minecraftVersion", rootProject.toothpick.minecraftVersion)
            PropertiesUtils.saveProperties(
                prop,
                outputDir.toPath().parent.resolve("yatoclip-launch.properties"),
                "Yatoclip launch values"
            )
        }
    }

    val sourceSets = extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

    sourceSets.create("java9") {
        java {
            srcDir("src/java9")
        }
    }

    val shadowJar by tasks.getting(ShadowJar::class) {
        manifest {
            attributes(
                "Main-Class" to "org.yatopiamc.yatoclip.Yatoclip",
                "Launcher-Agent-Class" to "org.yatopiamc.yatoclip.YatoclipLaunch",
                "Premain-Class" to "org.yatopiamc.yatoclip.YatoclipLaunch",
                "Multi-Release" to "true"
            )
        }
        into("META-INF/versions/9") {
            from(sourceSets.getByName("java9").output)
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
    val buildTask = tasks.getByName("build")
    val buildTaskDependencies = HashSet(buildTask.dependsOn)
    buildTask.setDependsOn(HashSet<Task>())
    buildTask.onlyIf { false }
    tasks.register("yatoclip") {
        buildTaskDependencies.forEach {
            dependsOn(it)
        }
    }
}

private fun Project.configureServerProject() {
    apply<ShadowPlugin>()

    val generatePomFileForMavenJavaPublication by tasks.getting(GenerateMavenPom::class) {
        destination = project.buildDir.resolve("tmp/pom.xml")
    }

    tasks.withType<Test> {
        // didn't bother to look into why these fail. paper excludes them in paperweight as well though
        exclude("org/bukkit/craftbukkit/inventory/ItemStack*Test.class")
    }

    val shadowJar by tasks.getting(ShadowJar::class) {
        archiveClassifier.set("") // ShadowJar is the main server artifact
        dependsOn(generatePomFileForMavenJavaPublication)
        transform(ModifiedLog4j2PluginsCacheFileTransformer::class.java)
        mergeServiceFiles()
        manifest {
            attributes(
                    "Main-Class" to "org.bukkit.craftbukkit.Main",
                    "Implementation-Title" to "CraftBukkit",
                    "Implementation-Version" to toothpick.forkVersion,
                    "Implementation-Vendor" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date()),
                    "Specification-Title" to "Bukkit",
                    "Specification-Version" to "${project.rootProject.toothpick.minecraftVersion}-${project.rootProject.toothpick.nmsRevision}",
                    "Specification-Vendor" to "Bukkit Team"
            )
        }
        from(project.buildDir.resolve("tmp/pom.xml")) {
            // dirty hack to make "java -Dpaperclip.install=true -jar paperclip.jar" work without forking paperclip
            into("META-INF/maven/io.papermc.paper/paper")
        }

        val relocationSet = HashSet<PatchesMetadata.Relocation>()

        // Don't like to do this but sadly have to do this for compatibility reasons
        relocate("org.bukkit.craftbukkit", "org.bukkit.craftbukkit.v${toothpick.nmsPackage}") {
            exclude("org.bukkit.craftbukkit.Main*")
        }
        relocationSet.add(PatchesMetadata.Relocation("", "net.minecraft.server.v${toothpick.nmsPackage}", false))

        // Make sure we relocate deps the same as Paper et al.
        val dom = project.parsePom() ?: return@getting
        val buildSection = dom.search("build").first()
        val plugins = buildSection.search("plugins").first()
        plugins.elements("plugin").filter {
            val artifactId = it.search("artifactId").first().textContent
            artifactId == "maven-shade-plugin"
        }.forEach {
            it.search("executions").first()
                    .search("execution").first()
                    .search("configuration").first()
                    .search("relocations").first()
                    .elements("relocation").forEach { relocation ->
                        val pattern = relocation.search("pattern").first().textContent
                        val shadedPattern = relocation.search("shadedPattern").first().textContent
                        val rawString = relocation.search("rawString").firstOrNull()?.textContent?.toBoolean() ?: false
                        if (pattern != "org.bukkit.craftbukkit") { // We handle cb ourselves
                            val excludes = if (rawString) listOf("net/minecraft/data/Main*") else emptyList()
                            relocate(
                            ToothpickRelocator(
                                pattern,
                                shadedPattern.replace("\${minecraft_version}", toothpick.nmsPackage),
                                rawString,
                                excludes = excludes
                            )
                            )
                            relocationSet.add(PatchesMetadata.Relocation(pattern, shadedPattern, true))
                        }
                    }
        }
        project.extensions.add("relocations", relocationSet)
    }
    tasks.getByName("build") {
        dependsOn(shadowJar)
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("shadow") {
                artifact(project.tasks.named("shadowJar"))
            }
        }
    }
}

@Suppress("UNUSED_VARIABLE")
private fun Project.configureApiProject() {
    val jar by this.tasks.getting(Jar::class) {
        doFirst {
            buildDir.resolve("tmp/pom.properties")
                    .writeText("version=${project.rootProject.toothpick.minecraftVersion}-${project.rootProject.toothpick.nmsRevision}")
        }
        from(buildDir.resolve("tmp/pom.properties")) {
            into("META-INF/maven/${project.group}/${project.name}")
        }
        manifest {
            attributes("Automatic-Module-Name" to "org.bukkit")
        }
    }

    extensions.configure<PublishingExtension> {
        publications {
            getByName<MavenPublication>("mavenJava") {
                artifactId = project.name
                from(components["java"])
            }
        }
    }
}
