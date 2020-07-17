#!/bin/bash
searchtxts=(server api)
gpgsign="$($gitcmd config commit.gpgsign || echo "false")"
function enableCommitSigningIfNeeded {
	if [[ "$gpgsign" == "true" ]]; then
		$gitcmd config commit.gpgsign true
	fi
}
# Disable GPG signing before AM, slows things down and doesn't play nicely.
# There is also zero rational or logical reason to do so for these sub-repo AMs.
# Calm down kids, it's re-enabled (if needed) immediately after, pass or fail.
$gitcmd config commit.gpgsign false
cd $1/patches/$2
for D in */; do
    if [ -d "${D}" ]; then
		dnoslash=${D%/*}
		$gitcmd branch -b $2-Upstream
		if [ $dnoslash != "api" ]; then
			echo "  $(bashcolor 1 32)($5/$6)$(bashcolorend) - Import new introduced NMS files.."
			basedir && $scriptdir/importSources.sh $1 $dnoslash || exit 1
		fi				
		for filename in $1/patches/$2/$dnoslash/*.patch; do
			cd $1/YAPFA-$dnoslash
			# Abort previous applying operation
			$gitcmd am --abort >/dev/null 2>&1
			# Apply our patches on top Paper in our dirs
			$gitcmd am --reject --whitespace=fix --no-utf8 --3way --ignore-whitespace $filename || (
			files=`$gitcmd diff --name-only | grep -E '.rej$' `
			if [[ $files != null ]]; then
				echo "Error found .rej file cannot recover. Aborting patching!"
				exit 1
			fi
			filenamend="${filename##*/}"
			filenamens=${filenamend%/*}
			filenameedited=${filenamens%.*}  # retain the part before the period
			filenameedited=${filenameedited:5}  # retain the part after the frist slash				
			$gitcmd add .
			$gitcmd commit -m filenameedited
			)
		done
	fi
done
$gitcmd am --no-utf8 --3way --ignore-whitespace "$basedir/patches/$patch_folder/"*.patch
enableCommitSigningIfNeeded