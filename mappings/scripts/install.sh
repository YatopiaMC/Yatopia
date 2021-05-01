#!/bin/bash
set -e
cd "$(dirname "$0")"
cd ..
basedir="$(cd .. && pwd -P)"

source $basedir/mappings/scripts/source.sh

do_fixes(){
    find -name '*.java' | xargs --max-procs=4 --no-run-if-empty sed -i '/^import [a-zA-Z0-9]*;$/d'
}

git config init.defaultBranch master

cd $basedir/mappings/work
rm -fr Yatopia-Server_remapped
mkdir -p Yatopia-Server_remapped/src/main
cp -r Base/src/main/java Yatopia-Server_remapped/src/main/
cd Yatopia-Server_remapped
git init
git add .
git commit --quiet --message=Base
rm -fr src/main/java/*
cd ..
JAVA_OPTS="-Xms1G -Xmx2G" "${basedir}/mappings/mapper/build/install/mapper/bin/mapper" "${basedir}/mappings/unmap.srg" "${basedir}/Yatopia-Server_yarn/src/main/java" "${basedir}/mappings/work/Yatopia-Server_remapped/src/main/java"


cd ${basedir}/mappings/work/Yatopia-Server_remapped/src/main/java
do_fixes

cd "$basedir"
changed="$(cat mappedPatches/*.patch | grep "+++ b/" | sort | uniq | sed 's/\+\+\+ b\///g')"
cd ${basedir}/mappings/work/Yatopia-Server_remapped
# git add .
git add $changed #only commit the files that were modified
git diff --cached > a.patch


cd "${basedir}/${inputdirprefix}Server"
patch -p1 < "$basedir/mappings/work/Yatopia-Server_remapped/a.patch"
git add .
git commit -m "<Mapped Patches>"
