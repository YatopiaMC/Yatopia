import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import java.io.File
import java.io.FileInputStream
import java.lang.Boolean
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

@Suppress("UNUSED_PARAMETER")
open class ToothpickExtension(objects: ObjectFactory) {
    lateinit var project: Project
    lateinit var forkName: String
    val forkNameLowercase
        get() = forkName.toLowerCase(Locale.ENGLISH)
    lateinit var forkUrl: String
    lateinit var forkVersion: String
    lateinit var groupId: String
    lateinit var minecraftVersion: String
    lateinit var nmsRevision: String
    lateinit var nmsPackage: String

    lateinit var upstream: String
    val upstreamLowercase
        get() = upstream.toLowerCase(Locale.ENGLISH)
    var upstreamBranch: String = "origin/master"

    var paperclipName: String? = null
    val calcPaperclipName
        get() = paperclipName ?: "${forkNameLowercase}-paperclip.jar"

    lateinit var serverProject: ToothpickSubproject
    fun server(receiver: ToothpickSubproject.() -> Unit) {
        serverProject = ToothpickSubproject()
        receiver(serverProject)
    }

    lateinit var apiProject: ToothpickSubproject
    fun api(receiver: ToothpickSubproject.() -> Unit) {
        apiProject = ToothpickSubproject()
        receiver(apiProject)
    }

    val subprojects: Map<String, ToothpickSubproject>
        get() = if (::forkName.isInitialized) mapOf(
            "$forkName-API" to apiProject,
            "$forkName-Server" to serverProject
        ) else emptyMap()

    val paperDir: File by lazy {
        if (upstream == "Paper") {
            project.upstreamDir
        } else {
            project.upstreamDir.walk().find {
                it.name == "Paper" && it.isDirectory
                        && it.resolve("work/Minecraft/${minecraftVersion}").exists()
            } ?: error("Failed to find Paper directory!")
        }
    }

    val paperWorkDir: File
        get() = paperDir.resolve("work/Minecraft/${minecraftVersion}")

    fun getUpstreams(rootProjectDir: File): MutableList<Upstream>? {
        val configDir = rootProjectDir.resolve("$rootProjectDir/upstreamConfig")
        val upstreams = configDir.listFiles()
        val uptreamArray = ArrayList<Upstream>()
        val prop = Properties()
        for (upstream in upstreams) {
            prop.load(FileInputStream(upstream))
            uptreamArray.add(Upstream(prop.getProperty("name"),
                Boolean.parseBoolean(prop.getProperty("useBlackList")),
                (prop.getProperty("list")),
                rootProjectDir,
                prop.getProperty("branch"),
                Integer.parseInt(upstream.name.substring(0,4)),
                project))
        }
        return uptreamArray.stream().sorted {upstream1, upstream2 -> upstream1.id - upstream2.id}.collect(Collectors.toList())
    }
}
