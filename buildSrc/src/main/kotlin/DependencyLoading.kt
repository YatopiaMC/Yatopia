import kotlinx.dom.elements
import kotlinx.dom.search
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.project
import org.w3c.dom.Element

fun RepositoryHandler.loadRepositories(project: Project) {
  val dom = project.parsePom() ?: return
  val repositoriesBlock = dom.search("repositories").firstOrNull() ?: return

  // Load repositories
  repositoriesBlock.elements("repository").forEach { repositoryElem ->
    val url = repositoryElem.search("url").firstOrNull()?.textContent ?: return@forEach
    maven(url)
  }
}

fun DependencyHandlerScope.loadDependencies(project: Project) {
  val dom = project.parsePom() ?: return

  // Load dependencies
  dom.search("dependencies").forEach {
    loadDependencies(project, it)
  }
}

private fun DependencyHandlerScope.loadDependencies(project: Project, dependenciesBlock: Element) {
  dependenciesBlock.elements("dependency").forEach { dependencyElem ->
    val groupId = dependencyElem.search("groupId").first().textContent
    val artifactId = dependencyElem.search("artifactId").first().textContent
    val version = dependencyElem.search("version").firstOrNull()?.textContent
    val scope = dependencyElem.search("scope").firstOrNull()?.textContent
    val classifier = dependencyElem.search("classifier").firstOrNull()?.textContent

    val dependencyString =
      "$groupId:$artifactId${processOptionalDependencyElement(version)}${processOptionalDependencyElement(classifier)}"
    project.logger.debug("Read $scope scope dependency '$dependencyString' from '${project.name}' pom.xml")

    // Special case API
    if (artifactId == project.toothpick.apiProject.project.name
      || artifactId == "${project.toothpick.upstreamLowercase}-api"
    ) {
      if (project == project.toothpick.serverProject.project) {
        add("api", project(":${project.toothpick.forkNameLowercase}-api"))
      }
      return@forEach
    }

    when (scope) {
      "import" -> add("api", platform(dependencyString))
      "compile", null -> {
        add("api", dependencyString)
        if (version != null) {
          add("annotationProcessor", dependencyString)
        }
      }
      "provided" -> {
        add("compileOnly", dependencyString)
        add("testImplementation", dependencyString)
        add("annotationProcessor", dependencyString)
      }
      "runtime" -> add("runtimeOnly", dependencyString)
      "test" -> add("testImplementation", dependencyString)
    }
  }
}

private fun processOptionalDependencyElement(element: String?): String =
  element?.run { ":$this" } ?: ""