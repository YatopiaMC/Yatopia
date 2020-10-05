#!/usr/bin/env bash

PS1=$
basedir=`pwd`

function update {
  branch=$2
  if [ -z "$2" ]; then
    branch="master"
  fi
  cd "$basedir/$1"
  git fetch && git reset --hard origin/$branch
  git add $1
}

function updateAll {
  update Akarin 1.16.2
  update Empirecraft master
  update Origami 1.16
  update Purpur ver/1.16.3
  update Tuinity ver/1.16.3
  git submodule update --recursive
}

if [ -z "$1" ]; then
  updateAll
elif [ "$1" == "true" ]; then
  update Tuinity ver/1.16.3
  git submodule update --recursive
elif [ "$1" == "false" ]; then
  if [ "$2" == "true" ]; then
    git submodule update --init -f
    cd "$basedir"
    cd Tuinity
    git clean -fx
    git clean -fd
    git fetch
    git reset --hard origin/ver/1.16.3
    git submodule update --init --recursive -f
  else 
    updateAll
  fi
else
  updateAll
fi