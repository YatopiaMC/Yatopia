val kotlinxDomVersion = "0.0.10"
val shadowVersion = "6.1.0"
val mustacheVersion = "0.9.6"
val javaxMailVersion = "1.4.4"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx.dom:$kotlinxDomVersion")
    implementation("com.github.jengelman.gradle.plugins:shadow:$shadowVersion")
    implementation("com.github.spullara.mustache.java:compiler:$mustacheVersion")
    implementation("javax.mail:mail:$javaxMailVersion")
}

gradlePlugin {
    plugins {
        register("Toothpick") {
            id = "toothpick"
            implementationClass = "Toothpick"
        }
    }
}
