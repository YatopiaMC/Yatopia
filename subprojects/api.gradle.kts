repositories {
    loadRepositories(project)
}

dependencies {
    loadDependencies(project)
}

java {
    withJavadocJar()
}

publishing {
	publications {
		create<MavenPublication>("api") {
			from(components.getByName("java"))
		}
	}

	repositories {
		maven {
			val releasesRepo = "https://repo.codemc.org/repository/maven-releases/"
			val snapshotsRepo = "https://repo.codemc.org/repository/maven-snapshots/"

			val versionString = project.version.toString()
			url = if (versionString.endsWith("-SNAPSHOT")) {
				uri(snapshotsRepo)
			} else {
				uri(releasesRepo)
			}
			credentials(PasswordCredentials::class)
		}
	}
}
