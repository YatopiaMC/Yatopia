<img width="200" src="https://yatopia.net/static/img/yatopia-shiny.gif" alt="Yatopia" align="right">
<div align="left">
<h1>Yatopia</h1>

[![Github-CI](https://github.com/YatopiaMC/Yatopia/workflows/CI/badge.svg)](https://github.com/YatopiaMC/Yatopia/actions?query=workflow%3ACI)
[![CodeMC](https://ci.codemc.io/buildStatus/icon?job=YatopiaMC%2FYatopia%2Fver%252F1.16.3)](https://ci.codemc.io/job/YatopiaMC/job/Yatopia/job/ver%252F1.16.3/)
[![Discord](https://img.shields.io/discord/342814924310970398?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discord.io/YatopiaMC)
[![API](https://img.shields.io/website?down_color=lightgrey&down_message=offline&label=API&up_color=green&up_message=online&url=http%3A%2F%2Fapi.yatopia.net%2F)](https://api.yatopia.net/v2/latestBuild?branch=ver/1.16.3)
<h3>Blazing fast <a href="https://github.com/Spottedleaf/Tuinity">Tuinity</a> fork with no-compromises performance.</h3>
</div>

## So what is Yatopia?
Yatopia combines the best patches from many Paper forks and optimization mods, as well as many unique optimizations. We borrow some of our patches from the following repos:

* [Akarin](https://github.com/Akarin-project/Akarin)
* [EMC](https://github.com/starlis/empirecraft)
* [Lithium](https://github.com/jellysquid3/lithium-fabric)
* [Origami](https://github.com/Minebench/Origami)
* [Purpur](https://github.com/pl3xgaming/Purpur)
* [Tic-TACS](https://github.com/gegy1000/tic-tacs/)


## Try it out 
The latest stable builds of Yatopia are always available over at our [downloads page](https://yatopia.net/download.html). You can also download the latest development build [here](https://api.yatopia.net/v2/latestBuild/download?branch=ver/1.16.3).

## Documentation

You can find a full explanation of the Yatopia configuration file on the [wiki](https://github.com/YatopiaMC/Yatopia/wiki). Check out the list of patches included in this project and who created them [here](PATCHES.md). You can also find our recommended config base [here](https://github.com/YatopiaMC/Yatopia/wiki/Configurations-Parameters-recommended)!

## Contributors

[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/0)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/0)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/1)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/1)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/2)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/2)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/3)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/3)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/4)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/4)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/5)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/5)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/6)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/6)
[![](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/images/7)](https://sourcerer.io/fame/budgidiere/YatopiaMC/Yatopia/links/7)


## Building and setting up

Run the following commands in the root directory:

```shell
./yatopia in
./yatopia full
```

If you are repatching you need to delete `Yatopa-API` and `Yatopia-Server` folders.

## Using Yatopia-API

To build your plugin against the Yatopia-API, first add the CodeMC maven repository:
```xml
<repositories>
    <!-- CodeMC -->
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
        <groupId>net.yatopia</groupId>
        <artifactId>yatopia-api</artifactId>
        <version>1.16.3-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Why aren't there many API additions?

(Modified from [starlis/empirecraft](https://github.com/starlis/empirecraft/))
<p>
APIs are tough to design. In projects such as Bukkit, Spigot, Sponge, Paper, etc once an API is commited, it's almost forever. You can't go breaking it without solid justification. This is the politics game.

With that in mind, much thought has to be given to the API in now and future use cases and applications to ensure it can be extended without breaking.

This is a lot of politics that we don't have time in our lives to deal with. 

That being said we make light API additions when requested.
</p>

## License

License information can be found [here](https://github.com/YatopiaMC/Yatopia/blob/ver/1.16.3/Licensing/LICENSE.md).

## Security

Security information can be found found [here](https://github.com/YatopiaMC/Yatopia/blob/ver/1.16.3/SECURITY.md).

## Statistics
[![bStats Graph Data](https://bstats.org/signatures/server-implementation/Yatopia.svg)](https://bstats.org/plugin/server-implementation/Yatopia)

Made with <span style="color: #e25555;">&#9829;</span> on Earth.
