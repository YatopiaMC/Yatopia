#!/bin/bash
cd "$(dirname "$0")"
cd ..
basedir="$(pwd -P)"

echo "Rebuilding patch files from current fork state..."
savePatches(){
    local what="$1"
    local patch_folder="$2"
    cd "$basedir/$what" || return 1

    mkdir -p "$basedir/$patch_folder"
    if [ -d ".git/rebase-apply" ]; then
        # in middle of a rebase, be smarter
        echo "REBASE DETECTED - PARTIAL SAVE"
        local last="$(cat .git/rebase-apply/last)"
        local next="$(cat .git/rebase-apply/next)"
        declare -a files=("$basedir/$patch_folder/"*.patch)
        for i in $(seq -f "%04g" 1 1 "$last")
        do
            if [ "$i" -lt "$next" ]; then
                rm "${files[`expr $i - 1`]}"
            fi
        done
    else
        if [ $(find $basedir/$patch_folder -type f | wc -l) != 0 ]; then
            rm "$basedir/$patch_folder/"*.patch
        fi
    fi

    git format-patch --no-signature --zero-commit --full-index --no-stat -N -o "$basedir/$patch_folder" upstream/upstream
    cd "$basedir" || return 1
    git add -A "$basedir/$patch_folder"
    echo "  Patches saved for $what to $patch_folder"
}

(savePatches Yatopia-Server_yarn mappedPatches) || exit 1
