cd Yatopia-$2
for filename in $1/patches/$2/*.patch; do
	# Abort previous applying operation
	git am --abort >/dev/null 2>&1
	# Apply our patches on top Paper in our dirs
	git am --reject --whitespace=fix --no-utf8 --3way --ignore-whitespace $filename || (
	#files=`$gitcmd diff --name-only | grep -E '.rej$' `
	#if [[ files != null ]]; then
	#	for filerej in files; do
	#		echo "Error found .rej file! Deleting. This might have unforseen consqunces!"
	#		rm -f filerej
	#	done
	#fi
	filenamend="${filename##*/}"
	filenamens=${filenamend%/*}
	filenameedited=${filenamens%.*}  # retain the part before the period
	filenameedited=${filenameedited:5}  # retain the part after the frist slash				
	git add .
	git commit -m $filenameedited
	)
	echo "Press any key to continue"
	while [ true ] ; do
		read -t 3 -n 1
		if [ $? = 0 ] ; then
			exit ;
		else
			echo "waiting for the keypress"
		fi
	done
done
