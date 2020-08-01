#!/bin/bash
patchdir="$1/patches"
searchtxts=(server api)
i=0

echo "Starting Upstream Patching!"
cd $patchdir
for D in */; do
    if [ -d "${D}" ]; then
		dnoslash=${D%/*}
        if [[ $dnoslash != "server" ]]; then
			if [[ $dnoslash != "api" ]]; then
				echo "Found $dnoslash directory!"
				for file in ${searchtxts[@]}; do
					i=0
					rm -rf -f "$1/patches/$dnoslash/$file/"
					echo "Looking for $file file!"
					echo "$(cat $patchdir/$dnoslash/$file.txt)"
					IFS='&'
					read -ra ADDR <<< $(cat $patchdir/$dnoslash/$file.txt)
					for patch in ${ADDR[@]}; do
						echo "Found $patch in $file!"
						echo $1/$dnoslash/patches/$file
						for filename in $1/$dnoslash/patches/$file/*.patch; do
							filenamend="${filename##*/}"
							filenamens=${filenamend%/*}
		 					filenameedited=${filenamens%.*}  # retain the part before the period
							filenameedited=${filenameedited:5}  # retain the part after the frist slash
							if [[ $filenameedited == $patch ]]; then
								echo "Found Matching file!"
								if [[ $i == 0 ]]; then
									echo "Making $file dir in $dnoslash patch dir"
									mkdir $1/patches/$dnoslash/$file
								fi
								((i=i+1))
								printf -v num "%04d" $i
								echo "Making ${num}-${patch}.patch file for YAPFA"
								cp $1/$dnoslash/patches/$file/$filenamens $1/patches/$dnoslash/$file/"${num}-${patch}.patch"	
							fi
						done
					done
					IFS=' '
				done
				$1/scripts/applyUpstream.sh $1 $dnoslash || exit 1
			fi
		fi
    fi
done

