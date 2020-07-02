cd Tuinity
git clean -fx
git clean -fd
git fetch
git reset --hard origin/ver/1.16
git submodule update --init --recursive -f
cd ..
sh patchPaper.sh
cd Tuinity
./tuinity paperclip
cd ..
./yapfa patch