#!/usr/bin/env bash

(
set -e

function changeLog() {
    base=$(git ls-tree HEAD $1  | cut -d' ' -f3 | cut -f1)
    cd $1 && git log --oneline ${base}...HEAD
}
tuinity=$(changeLog Tuinity)
akarin=$(changeLog Akarin)
empirecraft=$(changeLog Empirecraft)
origami=$(changeLog Origami)
purpur=$(changeLog Purpur)

updated=""
logsuffix=""
if [ ! -z "$tuinity" ]; then
    logsuffix="$logsuffix\n\nTuinity Changes:\n$tuinity"
    updated="Tuinity"
fi
if [ ! -z "$akarin" ]; then
  logsuffix="$logsuffix\n\nAkarin Changes:\n$akarin"
  if [ -z "$updated" ]; then updated="Akarin"; else updated="$updated/Akarin"; fi
fi
if [ ! -z "$empirecraft" ]; then
  logsuffix="$logsuffix\n\nEMC Changes:\n$empirecraft"
  if [ -z "$updated" ]; then updated="EMC"; else updated="$updated/EMC"; fi
fi
if [ ! -z "$origami" ]; then
  logsuffix="$logsuffix\n\nOrigami Changes:\n$origami"
  if [ -z "$updated" ]; then updated="Origami"; else updated="$updated/Origami"; fi
fi
if [ ! -z "$purpur" ]; then
  logsuffix="$logsuffix\n\nPurpur Changes:\n$purpur"
  if [ -z "$updated" ]; then updated="Purpur"; else updated="$updated/Purpur"; fi
fi
disclaimer="Upstream/An Sidestream has released updates that appears to apply and compile correctly\nThis update has NOT been tested by YatopiaMC and as with ANY update, please do your own testing."

if [ ! -z "$1" ]; then
    disclaimer="$@"
fi

log="Updated Upstream and Sidestream(s) ($updated)\n\n${disclaimer}${logsuffix}"

echo -e "$log" | git commit -F -

) || exit 1
