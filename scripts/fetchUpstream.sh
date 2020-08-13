#!/usr/bin/env bash
git submodule update --init -f
cd $1
cd Tuinity
git clean -fx
git clean -fd
git fetch
git reset --hard origin/1.16.2tmp
git submodule update --init --recursive -f