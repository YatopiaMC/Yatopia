<img src="https://i.imgur.com/oKQjlHP.png" alt="Yatopia" align="right">
<div align="center">
  <h1>Yatopia (Formerly YAPFA)</h1>
  <h3>The Most Powerful Minecraft Server Software</h3>

[![Github-CI](https://github.com/tr7zw/Yatopia/workflows/CI/badge.svg)](https://github.com/tr7zw/Yatopia/actions?query=workflow%3ACI)
[![Discord](https://img.shields.io/discord/342814924310970398?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discordapp.com/invite/yk4caxM)
[![Patreon](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.herokuapp.com%2Ftr7zw%2Fpledges&style=for-the-badge)](https://www.patreon.com/tr7zw)
</div>

## Introduction ##

A fork of [Tuinity](https://github.com/Spottedleaf/Tuinity), current side upstreams are:

* [EMC](https://github.com/starlis/empirecraft)
* [Lithium](https://github.com/jellysquid3/lithium-fabric)
* [Akarin](https://github.com/Akarin-project/Akarin)
* [Papercut](https://github.com/Minevictus/Papercut/)
* [Origami](https://github.com/MrIvanPlays/Origami)
* [Origami(2)](https://github.com/Minebench/Origami)
* [Purpur](https://github.com/pl3xgaming/Purpur)

Notable features are: Async & Optimized Pathfinding, Eigencraft Redstone (1.16), Async Advancements, Optimizer Pathfinder, Ability to slow down villager brains, Ability to disable collisions & collisionboxes, Major Hopper Performance Improvements, Nether TNT Bug + A lot more that all contribute towards ultimate performance.

## Experience Greatness ##

Available at the following options:

* **[GitHub Actions](https://github.com/tr7zw/Yatopia/actions)** - 1.16.1
* **[CodeMC](https://ci.codemc.io/job/Tr7zw/job/YAPFA/)** - 1.15.2

## Config Entries

### disableEntityStuckChecks
``Default``: ``false`` Entities don't check if they are stuck in a wall and should suffocate. E.g useful in hubs where player damage is canceled anyway.

### disablePlayerOutOfWorldBorderCheck
``Default``: ``false`` Players don't check if they are outside of the world border (to take damage). Can be used everywhere where the world border doesn't apply anyway like hubs/minigames.

### disableEntityCollisions
``Default``: ``false`` All entities stop pushing each other. The only exception is that players still will be pushed since this is clientside and controlled by a scoreboard team flag. This also breaks the entity cramming (which players can bypass anyway with ladders). Greatly improves performance where entity pushing is not required.

### disableEntityCollisionboxes
``Default``: ``false`` Removes the collision box from entities. This only really affects shulker mobs (not shulker boxes) so that mobs will fall through them like any other mob. Players still can walk over them since that is clientside. Greatly improves performance, with minimal impact on in-game behavior.

### onlyPlayerCollisions
``Default``: ``false`` Does the same as ``disableEntityCollisions`` except that players are still able to push other entities.

### pistonPushLimit
``Default``: ``12`` Just a fun option that enables you to modify the maximum amount of blocks that a piston can push. Together with slime/honey blocks and pushable tiles from Tuinity this allows a lot of new machines/doors.

### itemStuckSleepTicks
``Default``: ``1`` Controls how often a dropped item checks rather it's stuck inside a block (their most expansive operation). Higher values like ``15`` can be safely used and greatly improves dropped item performance if your server has thousands of them (from big farms/TNT/farming players etc).

### villager.simplerVillagerBehavior
``Default``: ``false`` Replaces the entire villager AI to the old AI system that all other mobs use. This breaks iron farms/villager breeders/villages (WIP to fix these problems), all villagers get a random profession (if they didn't have one) like in 1.8, don't need nor use workstations and refresh their trades after some time (trades are completely unaffected). This makes villagers as performant as all the other mobs by getting rid of their "brain" (Mojang's name^^) and behavior controller system, instead using the normal goal selector system. This way villagers are over twice as performant, but with the drawbacks that for now Iron farms/villager breeders are broken, so they can just be used for trading systems.

### villager.villagersHideAtNight
``Default``: ``false`` Addon for ``villager.simplerVillagerBehavior``, gives villagers the ability to drink an invisibility potion like wandering traders at night to make it less likely for them to be killed since (for now) they don't stay at the village/use their houses at night and no ability for them to respawn.

## Building and setting up
Run the following commands in the root directory:

```
git submodule update --init --recursive --force
./yatopia full
```

## LICENSE

Everything is licensed under the MIT license and is free to be used in your own fork.

See [EMC](https://github.com/starlis/empirecraft), [Lithium](https://github.com/jellysquid3/lithium-fabric), [Akarin](https://github.com/Akarin-project/Akarin), [Purpur](https://github.com/pl3xgaming/Purpur), [Papercut](https://github.com/Minevictus/Papercut/), [Origami](https://github.com/MrIvanPlays/Origami), [Origami(2)](https://github.com/Minebench/Origami), and [Tuinity](https://github.com/Spottedleaf/Tuinity)
for the license of material used/modified by this project.

**To use this project you'll have to accept the Mojang EULA!**
