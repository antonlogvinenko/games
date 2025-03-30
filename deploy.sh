set -ex

echo "Deploying..."
date

git stash

clj -M:fig:min

WWW="$HOME/dev/antonlogvinenko.github.io/"
git -C $WWW reset --hard HEAD
cp -rf resources/public/css $WWW/
cp -r resources/public/fonts $WWW/
cp -r resources/public/index.html $WWW/
mkdir -p $WWW/cljs-out
cp target/public/cljs-out/dev-main.js $WWW/cljs-out/
git -C $WWW add --all
git -C $WWW commit -m "Deploying a new version at: `date`"
git -C $WWW push origin main

git stash pop