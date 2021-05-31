import org.yatopiamc.toothpick.loadDependencies
import org.yatopiamc.toothpick.loadRepositories

plugins {
  `java-library`
  `maven-publish`
}

repositories {
    loadRepositories(project)
}

dependencies {
    loadDependencies(project)
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.codemc.org/repository/nms-local/")
            credentials(PasswordCredentials::class)
        }
    }
}
