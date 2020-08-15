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
  update Empirecraft ver/1.16.2
  update Origami 1.16
  update Purpur ver/1.16
  update Rainforest ver/1.16
  update Tuinity 1.16.2tmp
}

if [ -z "$1" ]; then
  updateAll
elif [ "$1" == "true" ]; then
  update Tuinity 1.16.2tmp
else
  updateAll
fi

git submodule update --recursive