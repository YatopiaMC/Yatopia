#!/usr/bin/env bash

# SCRIPT HEADER start
echo "  "
echo "----------------------------------------"
echo "  $(bashcolor 1 32)Task$(bashcolorend) - Install remapped jar"
echo "  This will install the minecraft-server dependency in your maven local repository"
echo "  Use this only if you have dependency issues with it"
echo "----------------------------------------"
# SCRIPT HEADER end

PS1=$
basedir=$(pwd)

minecraftversion=$(cat "$basedir/Tuinity/Paper/work/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
jarpath="$basedir/Tuinity/Paper/work/Minecraft/$minecraftversion"

cd "$basedir/Tuinity/Paper/work/CraftBukkit" || exit 1 # Need to be in a directory with a valid POM otherwise maven complains
mvn install:install-file -q -Dfile="$jarpath-mapped.jar" -Dpackaging=jar -DgroupId=io.papermc -DartifactId=minecraft-server -Dversion="$minecraftversion-SNAPSHOT"
if [ "$?" != "0" ]; then
  echo "Failed to install minecraft-server dependency"
  exit 1
fi