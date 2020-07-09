cd Tuinity
git clean -fx
git clean -fd
git fetch
git reset --hard origin/master
git submodule update --init --recursive -f
cd ..
sh patchPaper.sh
cd Tuinity
./tuinity paperclip
cd ..
./yapfa patch