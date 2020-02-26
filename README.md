# YAPFA
## (Yet another Paper fork attempt)
## What ##

This Fork tries to import universal patches from [EMC](https://github.com/starlis/empirecraft) and [Purpur](https://github.com/pl3xgaming/Purpur), while adding a few more "extrem" patches that modify the basic minecraft server for more performance.

## Building and setting up
Run the following commands in the root directory:

```
git submodule init
git submodule update
./yapfa up
./yapfa patch
```

## LICENSE

Everything is licensed under the MIT license, and is free to be used in your own fork.

See [EMC](https://github.com/starlis/empirecraft) and [Purpur](https://github.com/pl3xgaming/Purpur)
for the license of material used/modified by this project.

**By using this project you accept the Mojang EULA! Starting the server-jar requires that you have read and accepted the EULA because of [this patch](https://github.com/tr7zw/YAPFA/blob/master/patches/server/0017-EMC-Accept-the-EULA.patch)!**

## OS Support / Scripts
We only directly support Ubuntu 14.04 for the shell scripts. It may work elsewhere... but no promises. (This fork is developed under Windows with MINGW/WLS, and so far I don't intend to modify the scripts to any mojor point)

### scripts/importmcdev ###
Imports specific files from mc-dev that CB/Spigot doesn't use but we need.

### scripts/updatespigot ###
updates the Bukkit/CraftBukkit/Spigot baseline when passed --update, otherwise regenerates the baseline with changes
to mcdev from importmcdev

Run `scripts/applypatches` then `scripts/rebuildpatches` immediately after

### scripts/generatesources ###
Generates an mc-dev folder that can be added to IDE for the other mc-dev files located in minecraft-server.jar

### scripts/rebuildpatches ###
Rebuilds patch state from the YAPFA-* repo's current state. Automatically handles patch deletion and renaming
for you unlike Spigots version of these scripts.

### scripts/applypatches ###
Takes current patches/{bukkit,craftbukkit} and applys them on latest spigot upstream
