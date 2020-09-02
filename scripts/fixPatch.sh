cd Yatopia-$2
for filename in $1/patches/$2/*.patch; do
  # Abort previous applying operation
  git am --abort >/dev/null 2>&1
  # Apply our patches on top Paper in our dirs
  git am --reject --whitespace=fix --3way --ignore-whitespace $filename || (
    filenamend="${filename##*/}"
    filenamens=${filenamend%/*}
    filenameedited=${filenamens%.*}    # retain the part before the period
    filenameedited=${filenameedited:5} # retain the part after the frist slash
    git add .
    git commit -m $filenameedited
  )
  echo "Press any key to continue"
  while [ true ]; do
    read -t 3 -n 1
    if [ $? = 0 ]; then
      exit
    else
      echo "waiting for the keypress"
    fi
  done
done
