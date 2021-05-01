#!/bin/sh
set -e
cd "$(dirname "$0")"
cd ..
basedir="$(pwd -P)"

source $basedir/scripts/source.sh

do_fixes(){
    find -name '*.java' | xargs --max-procs=4 --no-run-if-empty sed -i '/^import [a-zA-Z0-9]*;$/d'
}

cd mapper
./gradlew installDist
cd "$basedir"


echo "Copying files for the 'Base' Folder"
rm -fr $basedir/Base
mkdir -p $basedir/Base/src/main/java/com/mojang
bash -c "cp -r ${basedir}/${inputdirprefix}Server/src/main/java/* ${basedir}/Base/src/main/java/"
# bash -c "cp -r ${basedir}/${inputdirprefix}API/src/main/java/* Base/src/main/java/"
mcver=$(cat "$paperdir/work/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)

cp -r ${basedir}/${paperdir}/work/Minecraft/"${mcver}"/libraries/com.mojang/*/* ${basedir}/Base/src/main/java/


echo "Setting up 'Mapped' Folder"
rm -fr $basedir/Yatopia-Server_yarn_unpatched
mkdir -p $basedir/Yatopia-Server_yarn_unpatched/src/main/java
echo "Remapping...."
cp "${basedir}/${inputdirprefix}Server/.gitignore" "${basedir}/${inputdirprefix}Server/pom.xml" "${basedir}/${inputdirprefix}Server/checkstyle.xml" "${basedir}/${inputdirprefix}Server/CONTRIBUTING.md" "${basedir}/${inputdirprefix}Server/LGPL.txt" "${basedir}/${inputdirprefix}Server/LICENCE.txt" "${basedir}/${inputdirprefix}Server/README.md" Yatopia-Server_yarn_unpatched/

JAVA_OPTS="-Xms1G -Xmx2G" ${basedir}/mapper/build/install/mapper/bin/mapper ${basedir}/map.srg ${basedir}/Base/src/main/java ${basedir}/Yatopia-Server_yarn_unpatched/src/main/java

echo "Applying fixes..."
cd "$basedir"/Yatopia-Server_yarn_unpatched/src/main/java
do_fixes

cd "$basedir"/Yatopia-Server_yarn_unpatched
git init
git add .
git commit --quiet --message=init
cd "$basedir"
