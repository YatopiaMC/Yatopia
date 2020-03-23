#!/usr/bin/env bash
(
set -e
PS1="$"

function changelog() {
    base=$(git ls-tree HEAD $1  | cut -d' ' -f3 | cut -f1)
    cd $1 && git log --oneline ${base}...HEAD
}
tuinity=$(changelog Tuinity)

log="Updated Tuinity \n\nUpdating our baseline Tuinity reference\n\nTuinity changes since last:\n$tuinity"

echo -e "$log" | git commit -F -

) || exit 1
