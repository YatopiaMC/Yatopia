#!/usr/bin/env bash

# SCRIPT HEADER start
basedir=$1
source "$basedir/scripts/functions.sh"
gpgsign="$($gitcmd config commit.gpgsign || echo "false")"
echo "  "
echo "----------------------------------------"
echo "  $(bashcolor 1 32)Task$(bashcolorend) - Apply Patches"
echo "  This will apply all of Yatopia patches on top of the Paper."
echo "  "
echo "  $(bashcolor 1 32)Subtask:$(bashcolorend)"
echo "  - Import Sources"
echo "  "
echo "  $(bashcolor 1 32)Modules:$(bashcolorend)"
echo "  - $(bashcolor 1 32)1$(bashcolorend) : API"
echo "  - $(bashcolor 1 32)2$(bashcolorend) : Server"
echo "----------------------------------------"
# SCRIPT HEADER end

needimport=$2
function enableCommitSigningIfNeeded {
	if [[ "$gpgsign" == "true" ]]; then
		$gitcmd config commit.gpgsign true
	fi
}
function applyPatch {
    baseproject=$1
    basename=$(basename $baseproject)
    target=$2
    branch=$3
    patch_folder=$4
	cd $basedir/$2
    # Skip if that software have no patch
    haspatch=-f "$basedir/patches/$patch_folder/"*.patch >/dev/null 2>&1 # too many files
	if [ ! haspatch ]; then
	    echo "  $(bashcolor 1 33)($5/$6) Skipped$(bashcolorend) - No patch found for $target under patches/$patch_folder"
		return
	fi
	
	# Disable GPG signing before AM, slows things down and doesn't play nicely.
	# There is also zero rational or logical reason to do so for these sub-repo AMs.
	# Calm down kids, it's re-enabled (if needed) immediately after, pass or fail.
	$gitcmd config commit.gpgsign false	
	
	if [[ $needimport != "1" ]]; then
	    if [ $baseproject != "Tuinity/Tuinity-API" ]; then
	        echo "  $(bashcolor 1 32)($5/$6)$(bashcolorend) - Import new introduced NMS files.."
			#cd $basedir/Yatopia-Server/
			#branch_name="$(git symbolic-ref HEAD 2>/dev/null)"
			#branch_name=${branch_name:-9}
			#cd $basedir
	        basedir && $scriptdir/importSources.sh $basedir "Yatopia" || exit 1
		fi
    fi
	#$gitcmd branch $target
	
	echo "  "

	echo "  $(bashcolor 1 32)($5/$6)$(bashcolorend) - Apply patches to $target.."
	# Abort previous applying operation
    #$gitcmd am --abort >/dev/null 2>&1
	# Apply our patches on top Paper in our dirs
    #$gitcmd am --no-utf8 --3way --ignore-whitespace "$basedir/patches/$patch_folder/"*.patch
	
	cd $basedir/$2
	git branch -d $2
	git branch $2
	git checkout $2
	# for filename in $basedir/patches/$patch_folder/*.patch; do
	# 	# Abort previous applying operation
	# 	git am --abort >/dev/null 2>&1
	# 	# Apply our patches on top Paper in our dirs
	# 	git am --reject --whitespace=fix --no-utf8 --3way --ignore-whitespace $filename || (
	# 	files=`$gitcmd diff --name-only | grep -E '.rej$' `
	# 	if [[ files != null ]]; then
	# 		for filerej in files; do
	# 			echo "Error found .rej file! Deleting. This might have unforseen consqunces!"
	# 			rm -f filerej
	# 		done
	# 	fi
	# 	filenamend="${filename##*/}"
	# 	filenamens=${filenamend%/*}
	# 	filenameedited=${filenamens%.*}  # retain the part before the period
	# 	filenameedited=${filenameedited:5}  # retain the part after the frist slash				
	# 	git add .
	# 	git commit -m $filenameedited
	# 	)
	# done

	$gitcmd am --abort >/dev/null 2>&1
	# Apply our patches on top Paper in our dirs
    $gitcmd am --reject --3way --whitespace=fix "$basedir/patches/$patch_folder/"*.patch
	cd $basedir

    if [ "$?" != "0" ]; then
        echo "  Something did not apply cleanly to $target."
        echo "  Please review above details and finish the apply then"
        echo "  save the changes with rebuildPatches.sh"
		echo "  or use 'git am --abort' to cancel this applying."
        echo "  $(bashcolor 1 33)($5/$6) Suspended$(bashcolorend) - Resolve the conflict or abort the apply"
		echo "  "
		cd "$basedir/$target"
        exit 1
    else
        echo "  $(bashcolor 1 32)($6/$6) Succeed$(bashcolorend) - Patches applied cleanly to $target"
		echo "  "
    fi
}

rm -rf $basedir/Yatopia/Yatopia-Server

rm -rf $basedir/Yatopia/Yatopia-API


$1/scripts/resetToUpstream.sh $1
$1/scripts/getUpstream.sh $1

(applyPatch Yatopia/Yatopia-API ${FORK_NAME}-API HEAD api $API_REPO 0 2 &&
applyPatch Yatopia/Yatopia-Server ${FORK_NAME}-Server HEAD server $SERVER_REPO 1 2 && enableCommitSigningIfNeeded) || (
enableCommitSigningIfNeeded
exit 1 )

