#!/bin/bash
searchtxts=(Server API)
gpgsign="$(git config commit.gpgsign || echo "false")"
scriptdir=$1/scripts
function enableCommitSigningIfNeeded {
	if [[ "$gpgsign" == "true" ]]; then
		git config commit.gpgsign true
	fi
}
# Disable GPG signing before AM, slows things down and doesn't play nicely.
# There is also zero rational or logical reason to do so for these sub-repo AMs.
# Calm down kids, it's re-enabled (if needed) immediately after, pass or fail.
git config commit.gpgsign false
cd $1/patches/$2
for D in ${searchtxts[@]}; do
	echo "IMPORTANT:!!! $D"
    if [ true ]; then
		echo $D
		dnoslash=$D
		echo "DnoS: $dnoslash"
		echo "$1/YAPFA-$dnoslash"
		cd $1/YAPFA-$dnoslash
		echo "Appyling $2 $dnoslash files!"
		dnoslashlower="${dnoslash,,}"
		#git branch $2-upstream
		#git checkout $2-upstream
		if [ $dnoslashlower != "api" ]; then
			echo "$"
			echo "Import new introduced NMS files.. test"
				$scriptdir/importSources.sh $1 $2 || exit 1
		fi				
		for filename in $1/patches/$2/$dnoslashlower/*.patch; do
			# Abort previous applying operation
			git am --abort >/dev/null 2>&1
			# Apply our patches on top Paper in our dirs
			git am --reject --whitespace=fix --no-utf8 --3way --ignore-whitespace $filename || (
			files=`$gitcmd diff --name-only | grep -E '.rej$' `
			if [[ files != null ]]; then
				for filerej in files; do
					echo "Error found .rej file! Deleting. This might have unforseen consqunces!"
					rm -f filerej
				done
			fi
			filenamend="${filename##*/}"
			filenamens=${filenamend%/*}
			filenameedited=${filenamens%.*}  # retain the part before the period
			filenameedited=${filenameedited:5}  # retain the part after the frist slash				
			git add .
			git commit -m $filenameedited
			)
		done
	fi
done
#git am --no-utf8 --3way --ignore-whitespace "$basedir/patches/$patch_folder/"*.patch
enableCommitSigningIfNeeded