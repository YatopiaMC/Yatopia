<img width="200" src="https://yatopiamc.org/static/img/yatopia-shiny.gif" alt="Yatopia" align="right">
<div align="left">
<h1>Yatopia</h1>

[![Github-CI](https://github.com/YatopiaMC/Yatopia/workflows/CI/badge.svg)](https://github.com/YatopiaMC/Yatopia/actions?query=workflow%3ACI)
[![CodeMC](https://ci.codemc.io/buildStatus/icon?job=YatopiaMC%2FYatopia%2Fver%252F1.16.5)](https://ci.codemc.io/job/YatopiaMC/job/Yatopia/job/ver%252F1.16.5/)
[![Discord](https://img.shields.io/discord/342814924310970398?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discord.gg/YatopiaMC)
[![API](https://img.shields.io/website?down_color=lightgrey&down_message=offline&label=API&up_color=green&up_message=online&url=http%3A%2F%2Fapi.yatopiamc.org%2F)](https://api.yatopiamc.org/v2/latestBuild?branch=ver/1.16.5)
<h3>Blazing fast <a href="https://github.com/pl3xgaming/Purpur">Purpur</a> fork with best in class performance.</h3>
</div>

## So what is Yatopia?
Yatopia is a performance-oriented fork of [Purpur](https://github.com/pl3xgaming/Purpur) that adds even more optimizations overtop the already great performance that Purpur inherits from it's upstreams. We have also ported some optimizations from the following projects:
* [Akarin](https://github.com/Akarin-project/Akarin)
* [EMC](https://github.com/starlis/empirecraft)
* [Lithium](https://github.com/jellysquid3/lithium-fabric)
* [Origami](https://github.com/Minebench/Origami)
* [Cadmium](https://github.com/LucilleTea/cadmium-fabric)
* [Tic-Tacs](https://github.com/Gegy/tic-tacs)


## Try it out 
The latest stable builds of Yatopia are always available over at our [downloads page](https://yatopiamc.org/download.html). You can also download the latest development build [here](https://api.yatopiamc.org/v2/latestBuild/download?branch=ver/1.16.5).

## Documentation
You can find a full explanation of the Yatopia configuration file on the [wiki](https://github.com/YatopiaMC/Yatopia/wiki). Check out the list of patches included in this project and who created them [here](PATCHES.md).

## Building and setting up
Run the following commands in the root directory:

```shell
./gradlew applyPatches
./gradlew build paperclip
```


## Using Yatopia-API
To build your plugin against the Yatopia-API, first add the CodeMC maven repository:

# Maven
Add the CodeMC Repo:
```xml
<repositories>
    <repository>
        <id>codemc-repo</id>
        <url>https://repo.codemc.io/repository/maven-public/</url>
    </repository>
</repositories>
```

And then add the Yatopia-API dependency:
```xml
<dependencies>
    <dependency>
        <groupId>org.yatopiamc</groupId>
        <artifactId>yatopia-api</artifactId>
        <version>1.16.5-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

# Gradle

> Groovy DSL

Add the CodeMC Repo:
```groovy
repositories {
    maven {
        url 'https://repo.codemc.io/repository/maven-public/'
    }
}
```

And then add the Yatopia-API dependency:
```groovy
dependencies {
    compileOnly 'org.yatopiamc:yatopia-api:1.16.5-R0.1-SNAPSHOT'
}
```

> Kotlin DSL

Add the CodeMC Repo:
```kotlin
repositories {
    maven("https://repo.codemc.io/repository/maven-public/")
}
```

And then add the Yatopia-API dependency:
```kotlin
dependencies {
    compileOnly("org.yatopiamc:yatopia-api:1.16.5-R0.1-SNAPSHOT")
}
```

## Why aren't there many API additions?
(Modified from [starlis/empirecraft](https://github.com/starlis/empirecraft/))
<p>
APIs are tough to design. In projects such as Bukkit, Spigot, Sponge, Paper, etc once an API is committed, it's almost forever. You can't go breaking it without solid justification. This is the politics game.

With that in mind, much thought has to be given to the API in now and future use cases and applications to ensure it can be extended without breaking.

This is a lot of politics that we don't have time in our lives to deal with. 

That being said we make light API additions when requested.
</p>

## License
License information can be found [here](/Licensing/LICENSE.md).

## Security
Security information can be found [here](/SECURITY.md).

## Statistics
[![bStats Graph Data](https://bstats.org/signatures/server-implementation/Yatopia.svg)](https://bstats.org/plugin/server-implementation/Yatopia)

Made with <span style="color: #e25555;">&#9829;</span> on Earth.
