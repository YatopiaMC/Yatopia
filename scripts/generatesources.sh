#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
. $(dirname $SOURCE)/init.sh


cd $basedir
tuinityVer=$(cat current-tuinity)

minecraftversion=$(cat $basedir/Tuinity/work/BuildData/info.json | grep minecraftVersion | cut -d '"' -f 4)
decompile="Tuinity/work/Minecraft/$minecraftversion/spigot"

mkdir -p mc-dev/src/net/minecraft/server

cd mc-dev
if [ ! -d ".git" ]; then
	git init
fi

rm src/net/minecraft/server/*.java
for i in $basedir/$decompile/net/minecraft/server/*.java;
do 
	cp "$i" src/net/minecraft/server
done


base="$basedir/Tuinity/Tuinity-Server/src/main/java/net/minecraft/server"
cd $basedir/mc-dev/src/net/minecraft/server/
for file in $(/bin/ls $base)
do
	if [ -f "$file" ]; then
		rm -f "$file"
	fi
done
cd $basedir/mc-dev
git add . -A
git commit . -m "mc-dev"
git tag -a "$tuinityVer" -m "$tuinityVer" 2>/dev/null
