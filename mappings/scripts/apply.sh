#!/bin/sh
# set -e
cd "$(dirname "$0")"
cd ..
basedir="$(cd .. && pwd -P)"

gitcmd="git -c commit.gpgsign=false"

applyPatch(){
    local what="$1"
    local what_name="$(basename "$what")"
    local target="$2"
    local branch="$3"
    local patch_folder="$4"

    cd "$basedir/$what"
    $gitcmd branch -f upstream "$branch" >/dev/null

    cd "$basedir"
    if [ ! -d  "$basedir/$target" ]; then
        echo "doing clone"
        $gitcmd clone "$what" "$target"
    fi
    cd "$basedir/$target"

    echo "Resetting $target to $what_name..."
    $gitcmd remote rm upstream > /dev/null 2>&1
    $gitcmd remote add upstream "$basedir/$what" >/dev/null 2>&1
    $gitcmd checkout master 2>/dev/null || $gitcmd checkout -b master

    $gitcmd fetch upstream >/dev/null 2>&1
    $gitcmd reset --hard upstream/upstream
    echo "  Applying patches to $target..."
    $gitcmd am --abort >/dev/null 2>&1
    if [ -z "$(ls "$basedir/$patch_folder/"*.patch||true)" ];then
        echo "  no patches for $target"
    elif $gitcmd am --3way --ignore-whitespace "$basedir/$patch_folder/"*.patch; then
        echo "  Patches applied cleanly to $target"
    else
        echo "  Something did not apply cleanly to $target."
        echo "  Please review above details and finish the apply then"
        echo "  save the changes with rebuildPatches.sh"
        return 1
    fi
}

(applyPatch mappings/work/Yatopia-Server_yarn_unpatched Yatopia-Server_yarn HEAD mappedPatches)
