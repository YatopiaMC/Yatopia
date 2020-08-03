#!/usr/bin/env bash

searchtxts=(Server API)
basedir=$1
basename=$(basename $baseproject)
branch=HEAD
for type in ${searchtxts[@]}; do
	baseproject=Tuinity/Tuinity-$type
	target=Yatopia-$type
	echo "$baseproject, $target, $branch, $basedir, $type, $basename"
	echo "Setup upstream project.."
	echo "$basedir/$baseproject"
	cd "$basedir/$baseproject"
	git fetch --all &> /dev/null
	# Create the upstream branch in Paper project with current state
	git checkout master >/dev/null 2>&1 # possibly already in
	git branch -D upstream &> /dev/null
	git branch -f upstream "$branch" &> /dev/null && git checkout upstream &> /dev/null
    cd $basedir
	# Create source project dirs
    if [ ! -d  "$basedir/$target" ]; then
        mkdir "$basedir/$target"
        cd "$basedir/$target"
        # git remote add origin "$5"
	fi
	echo "$basedir/$target"
    cd "$basedir/$target"
	git init > /dev/null 2>&1

    echo "  "
	echo "Reset $target to $basename.."
	# Add the generated Paper project as the upstream remote of subproject
    git remote rm upstream &> /dev/null
    git remote add upstream "$basedir/$baseproject" &> /dev/null
	# Ensure that we are in the branch we want so not overriding things
    git checkout master &> /dev/null || git checkout -b master &> /dev/null
    git fetch upstream &> /dev/null
	# Reset our source project to Paper
    cd "$basedir/$target" && git reset --hard upstream/upstream &> /dev/null
done

