#!/usr/bin/env bash

basedir=$1
source "$basedir/scripts/functions.sh"
set -e

patcherDir=$basedir/KibblePatcher
patcherJar=$patcherDir/KibblePatcher.jar
# Get server jar
paperworkdir="$basedir/Tuinity/Paper/work"
mcver=$(cat "$paperworkdir/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)
serverJar="$basedir/Yatopia-Server/target/yatopia-$mcver.jar"

cd $patcherDir
./gradlew buildRoot

java -jar $patcherJar -yatopia -patch $serverJar
