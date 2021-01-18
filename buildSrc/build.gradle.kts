val kotlinxDomVersion = "0.0.10"
val shadowVersion = "6.1.0"

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
    implementation("com.github.spullara.mustache.java:compiler:0.9.6")
    implementation("javax.mail:mail:1.4.4")
}

gradlePlugin {
    plugins {
        register("Toothpick") {
            id = "toothpick"
            implementationClass = "Toothpick"
        }
    }
}
