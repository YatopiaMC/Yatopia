#!/usr/bin/env bash
(
set -e
PS1="$"

function changelog() {
    base=$(git ls-tree HEAD $1  | cut -d' ' -f3 | cut -f1)
    cd $1 && git log --oneline ${base}...HEAD
}
paper=$(changelog Paper)

log="Updated Paper \n\nUpdating our baseline Paper reference\n\nPaper changes since last:\n$paper"

echo -e "$log" | git commit -F -

) || exit 1
