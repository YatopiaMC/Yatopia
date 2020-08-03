#!/usr/bin/env bash
cd $1
git submodule update --init -f
cd Tuinity
git submodule update --init --recursive -f