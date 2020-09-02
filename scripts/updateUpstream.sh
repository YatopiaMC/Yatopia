#!/usr/bin/env bash

# SCRIPT HEADER start
basedir=$1
source "$basedir/scripts/functions.sh"
echo "----------------------------------------"
echo "  $(bashcolor 1 32)Task$(bashcolorend) - Update Upstream"
echo "  This will update and patch Paper, importing necessary sources for patching."
#echo "  "
#echo "  $(bashcolor 1 32)Subtask:$(bashcolorend)"
#echo "  - Import Sources"
echo "  "
echo "  $(bashcolor 1 32)Projects:$(bashcolorend)"
echo "  - $(bashcolor 1 32)1$(bashcolorend) : Paper"
echo "  - $(bashcolor 1 32)2$(bashcolorend) : Yatopia"
echo "----------------------------------------"
# SCRIPT HEADER end

# This script are capable of patching paper which have the same effect with renewing the source codes of paper to its corresponding remote/official state, and also are able to reset the patches of paper to its head commit to override dirty changes which needs a argument with --resetPaper.

# After the patching, it will copying sources that do no exist in the Yatopia workspace but referenced in Yatopia patches into our workspace, depending on the content of our patches, this will be addressed by calling importSources.sh.

# Following by invoking generateImports.sh,  it will generate new added/imported files of paper compared to the original decompiled sources into mc-dev folder under the root dir of the project, whose intention is unclear yet.

# exit immediately if a command exits with a non-zero status
set -e

subtasks=1

if [ -z "$2" ]; then
  $basedir/scripts/fetchUpstream.sh
else
  if [ -z "$3" ]; then
    $basedir/scripts/fetchUpstream.sh true
  else
    $basedir/scripts/fetchUpstream.sh false true
  fi
fi

# patch paper
echo "  $(bashcolor 1 32)(0/$subtasks)$(bashcolorend) - Apply patches of Tuinity.."
echo "  "
paperVer=$(gethead Tuinity)
paperdir
./tuinity patch

echo "  $(bashcolor 1 32)($subtasks/$subtasks) Succeed$(bashcolorend) - Submodules have been updated, regenerated and imported, run 'Yatopia patch' to test/fix patches, and by 'Yatopia rbp' to rebuild patches that fixed with the updated upstream."
echo "  "
