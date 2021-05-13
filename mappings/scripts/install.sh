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
rm -fr "${inputdirprefix}Server_remapped"
mkdir -p "${inputdirprefix}Server_remapped/src/main"
cp -r Base/src/main/java "${inputdirprefix}Server_remapped/src/main/"
cd "${inputdirprefix}Server_remapped"
git init
git add .
git commit --quiet --message=Base
rm -fr src/main/java/*
cd ..
JAVA_OPTS="-Xms1G -Xmx2G" "${basedir}/mappings/mapper/build/install/mapper/bin/mapper" "${basedir}/mappings/unmap.srg" "${basedir}/${inputdirprefix}Server_yarn/src/main/java" "${basedir}/mappings/work/${inputdirprefix}Server_remapped/src/main/java"


cd "${basedir}/mappings/work/${inputdirprefix}Server_remapped/src/main/java"
do_fixes

cd "$basedir"
changed="$(cat mappedPatches/*.patch | grep "+++ b/\|+++ a/\|--- b/\|--- a/" | sort | uniq | sed 's/\+\+\+ b\///g' | sed 's/\+\+\+ a\///g' | sed 's/\-\-\- b\///g' | sed 's/\-\-\- a\///g')"
cd "${basedir}/mappings/work/${inputdirprefix}Server_remapped"
# git add .
for file in $changed; do
    git add $file || true
done
# git add $changed #only commit the files that were modified
git diff --cached > a.patch


cd "${basedir}/${inputdirprefix}Server"
if [ $(git log --pretty=format:'%s' | grep '<Mapped Patches>' | wc -l) != "0" ]; then
    latestcommit=$(git log --pretty=format:'%s' | head -n1)
    echo "found 'Mapped Patches' commit"
    if [ "$latestcommit" == "<Mapped Patches>" ]; then
        echo "'Mapped Patches' commit is the latest commit, reverting before applying..."
        git reset --hard HEAD^
    fi
fi

patch -p1 < "$basedir/mappings/work/${inputdirprefix}Server_remapped/a.patch"
git add .
git commit -m "<Mapped Patches>"
