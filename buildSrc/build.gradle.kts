val kotlinxDomVersion = "0.0.10"
val shadowVersion = "7.0.0"
val mustacheVersion = "0.9.7"
val javaxMailVersion = "1.6.2"
val jbsdiffVersion = "deff66b794"
val gsonVersion = "2.8.6"
val guavaVersion = "30.1.1-jre"
val commonsioVersion = "2.8.0"

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
    implementation("javax.mail:javax.mail-api:$javaxMailVersion")
    implementation("com.github.ishlandbukkit:jbsdiff:$jbsdiffVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("commons-io:commons-io:$commonsioVersion")
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
