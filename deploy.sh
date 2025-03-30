set -ex
clj -M:fig:min
rm -Rf docs/*
cp -r resources/public/css docs/
cp -r resources/public/fonts docs/
mkdir docs/cljs-out
cp target/public/cljs-out/dev-main.js docs/cljs-out/