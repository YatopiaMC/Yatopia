val kotlinxDomVersion = "0.0.10"
val shadowVersion = "7.0.0"
val mustacheVersion = "0.9.6"
val javaxMailVersion = "1.4.4"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx.dom:$kotlinxDomVersion")
    implementation("com.github.johnrengelman:shadow:$shadowVersion")
    implementation("com.github.spullara.mustache.java:compiler:$mustacheVersion")
    implementation("javax.mail:mail:$javaxMailVersion")
    implementation("com.github.ishlandbukkit:jbsdiff:deff66b794")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.google.guava:guava:30.0-jre")
    implementation("commons-io:commons-io:2.8.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
}

gradlePlugin {
    plugins {
        register("Toothpick") {
            id = "toothpick"
            implementationClass = "Toothpick"
        }
    }
}
