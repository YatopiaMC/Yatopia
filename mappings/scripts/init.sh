#!/bin/sh
set -e
cd "$(dirname "$0")"
cd ..
basedir="$(cd .. && pwd -P)"

source $basedir/mappings/scripts/source.sh

do_fixes(){
    find -name '*.java' | xargs --max-procs=4 --no-run-if-empty sed -i '/^import [a-zA-Z0-9]*;$/d'
}

cd ${basedir}/mappings/mapper
./gradlew installDist
cd "$basedir"


echo "Copying files for the 'Base' Folder"
rm -fr $basedir/mappings/work/Base
mkdir -p $basedir/mappings/work/Base/src/main/java/com/mojang
bash -c "cp -r ${basedir}/${inputdirprefix}Server/src/main/java/* ${basedir}/mappings/work/Base/src/main/java/"
# bash -c "cp -r ${basedir}/${inputdirprefix}API/src/main/java/* Base/src/main/java/"
mcver=$(cat "$paperdir/work/BuildData/info.json" | grep minecraftVersion | cut -d '"' -f 4)

cp -r ${basedir}/${paperdir}/work/Minecraft/"${mcver}"/libraries/com.mojang/*/* ${basedir}/mappings/work/Base/src/main/java/


echo "Setting up 'Mapped' Folder"
rm -fr $basedir/mappings/work/Yatopia-Server_yarn_unpatched
mkdir -p $basedir/mappings/work/Yatopia-Server_yarn_unpatched/src/main/java
echo "Remapping...."
cp "${basedir}/${inputdirprefix}Server/.gitignore" "${basedir}/${inputdirprefix}Server/pom.xml" "${basedir}/${inputdirprefix}Server/checkstyle.xml" "${basedir}/${inputdirprefix}Server/CONTRIBUTING.md" "${basedir}/${inputdirprefix}Server/LGPL.txt" "${basedir}/${inputdirprefix}Server/LICENCE.txt" "${basedir}/${inputdirprefix}Server/README.md" mappings/work/Yatopia-Server_yarn_unpatched/

JAVA_OPTS="-Xms1G -Xmx2G" ${basedir}/mappings/mapper/build/install/mapper/bin/mapper ${basedir}/mappings/map.srg ${basedir}/mappings/work/Base/src/main/java ${basedir}/mappings/work/Yatopia-Server_yarn_unpatched/src/main/java

echo "Applying fixes..."
cd "$basedir"/mappings/work/Yatopia-Server_yarn_unpatched/src/main/java
do_fixes

cd "$basedir"/mappings/work/Yatopia-Server_yarn_unpatched
git init
git add .
git commit --quiet --message=init
cd "$basedir"
