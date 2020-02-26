#!/usr/bin/env bash
# get base dir regardless of execution location
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
        DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
        SOURCE="$(readlink "$SOURCE")"
        [[ "$SOURCE" != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SOURCE=$([[ "$SOURCE" = /* ]] && echo "$SOURCE" || echo "$PWD/${SOURCE#./}")
basedir=$(dirname "$SOURCE")
cd "$basedir"
git pull
. scripts/init.sh
git submodule sync
git submodule update --init
(
	cd Paper/
	git submodule update --init
)
mc=$(cat $basedir/Paper/work/BuildData/info.json | grep minecraftVersion | cut -d '"' -f 4)

function update() {
	cd $basedir
	folder=${FORK_NAME}-$1
	if [ ! -d "$folder" ]; then
		git clone "$2" $folder
	fi

        cd $basedir/$folder
        git fetch origin
        git checkout master
        git reset --hard origin/$mc
}
mvn clean install -N
update API "$API_REPO"
update Server "$SERVER_REPO"

cd ..
if [ "$1" != "--nocompile" ]; then
	mvn clean install
fi