# YAPFA
## (Yet another ~~Paper~~ Tuinity fork attempt)
[![Github-CI](https://github.com/tr7zw/YAPFA/workflows/CI/badge.svg)](https://github.com/tr7zw/YAPFA/actions?query=workflow%3ACI) [![CodeMC-CI](https://ci.codemc.io/job/Tr7zw/job/YAPFA/badge/icon?style=plastic)](https://ci.codemc.io/job/Tr7zw/job/YAPFA/)
[![Discord](https://img.shields.io/discord/342814924310970398?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discordapp.com/invite/yk4caxM)
[![Patreon](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.herokuapp.com%2Ftr7zw%2Fpledges&style=for-the-badge)](https://www.patreon.com/tr7zw)

## What ##

This Fork tries to import universal/performance patches from [EMC](https://github.com/starlis/empirecraft), [Lithium](https://github.com/jellysquid3/lithium-fabric), [Akarin](https://github.com/Akarin-project/Akarin) and [Purpur](https://github.com/pl3xgaming/Purpur), while adding a few more (optional via config) "extrem" patches that modify the vanilla minecraft gameplay for more performance(noteable killing entity pushing/collisionboxes). This fork was based on Paper, but is now based on [Tuinity](https://github.com/Spottedleaf/Tuinity).

## Building and setting up
Run the following commands in the root directory:

```
git submodule init
git submodule update
./fetchUpstream.sh
./yapfa build
./yapfa paperclip
```

## LICENSE

Everything is licensed under the MIT license, and is free to be used in your own fork.

See [EMC](https://github.com/starlis/empirecraft), [Lithium](https://github.com/jellysquid3/lithium-fabric), [Akarin](https://github.com/Akarin-project/Akarin), [Purpur](https://github.com/pl3xgaming/Purpur) and [Tuinity](https://github.com/Spottedleaf/Tuinity)
for the license of material used/modified by this project.

**By using this project you accept the Mojang EULA! Starting the server-jar requires that you have read and accepted the EULA because of [this patch](https://github.com/tr7zw/YAPFA/blob/master/patches/server/0017-EMC-Accept-the-EULA.patch)!**