repositories {
    loadRepositories(project)
}

dependencies {
    loadDependencies(project)
}

publishing {
    publications {
        create<MavenPublication>("server") {
            from(components.getByName("java"))
        }
    }

    repositories {
        maven {
            url = uri("https://repo.codemc.org/repository/nms-local/")
            credentials(PasswordCredentials::class)
        }
    }
}
