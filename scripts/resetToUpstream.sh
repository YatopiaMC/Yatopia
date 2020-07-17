#!/usr/bin/env bash

searchtxts=(Server API)
basedir=$1
basename=$(basename $baseproject)
branch=HEAD
for type in ${searchtxts[@]}; do
	baseproject=/Tuinity/Tuinity-$type
	target=YAPFA-$type
	echo "Setup upstream project.."
	cd "$basedir/$baseproject"
	$gitcmd fetch --all &> /dev/null
	# Create the upstream branch in Paper project with current state
	$gitcmd checkout master >/dev/null 2>&1 # possibly already in
	$gitcmd branch -D upstream &> /dev/null
	$gitcmd branch -f upstream "$branch" &> /dev/null && $gitcmd checkout upstream &> /dev/null
    cd $basedir
	# Create source project dirs
    if [ ! -d  "$basedir/$target" ]; then
        mkdir "$basedir/$target"
        cd "$basedir/$target"
        # $gitcmd remote add origin "$5"
	fi
    cd "$basedir/$target"
	$gitcmd init > /dev/null 2>&1

    echo "  "
	echo "Reset $target to $basename.."
	# Add the generated Paper project as the upstream remote of subproject
    $gitcmd remote rm upstream &> /dev/null
    $gitcmd remote add upstream "$basedir/$baseproject" &> /dev/null
	# Ensure that we are in the branch we want so not overriding things
    $gitcmd checkout master &> /dev/null || $gitcmd checkout -b master &> /dev/null
    $gitcmd fetch upstream &> /dev/null
	# Reset our source project to Paper
    cd "$basedir/$target" && $gitcmd reset --hard upstream/upstream &> /dev/null
done
