# EmpireCraft
## What
EmpireCraft is a fork of Spigot used by the [Empire Minecraft](https://ref.emc.gs/Aikar?gam=EmpireCraft) Server.

It contains many gameplay changes to suit our server, but more importantly, contains new performance improvements pending testing to be contributed to Spigot / Paper / Sponge.

We also have many APIs that we privately use but [choose not to](#why-we-dont-release-all-apis) publicly PR upstream.

## Why we don't release all APIs
APIs are tough to design. In public projects such as Bukkit, Spigot, Paper, etc., once an API is commited, it's almost forever. You can't go breaking it without solid justification. This is the politics game.

With that in mind, much thought has to be given to our APIs in current and future use cases and applications to ensure it can be extended without breaking. 

This is a lot of politics that we don't have time in our lives to deal with.

Therefore, we write APIs to OUR base needs, which is often not 'complete' or 'up to style guidelines' of upstream repositories. We do not have the time to write code that we personally do not need for these APIs.

We also want to retain the ability to make breaking changes to these APIs if it results in a better way to do things or performance improvements.

By contributing it upstream, we would give up that power.

So that is why we have many extremely useful APIs that are not PR'd upstream.

## License, Support, & Usage of Patches
All patches written by Aikar, Starlis LLC, and/or Contractors of Starlis LLC that are included within EmpireCraft are licensed MIT, and are free to be used in your own fork.

We offer __ABSOLUTELY NO SUPPORT__ for these patches. If you wish to use them, you must take the effort to extract them from our repo, apply them to your own, and repair any code changes to get it to compile (note: we use a `.callEvent()` utility method on the Event class for nearly all custom events to reduce diff.)

If we make any breaking changes, and you still wish to use these patches, it's your job to fix the changes!

So in summary, we love to share! Use anything we wrote in this repo how ever you please, but support it yourself :)

## OS Support & Scripts
We only directly support the latest LTS Ubuntu for shell scripts. It may work elsewhere... but no promises.

Many scripts will try to push to our repos, please change that if you fork :)

### scripts/importmcdev
Imports specific files from mc-dev that CB/Spigot doesn't use, but we need.

### scripts/generatesources
Generates an mc-dev folder that can be added to IDE for the other mc-dev files located in minecraft-server.jar.

### scripts/rebuildpatches
Rebuilds patch state from the EmpireCraft-* repo's current state. Automatically handles patch deletion and renaming for you, unlike Spigot's version of these scripts.

### scripts/applypatches
Takes current patches, i.e., Bukkit & CraftBukkit, and applies them on the latest Spigot upstream.
