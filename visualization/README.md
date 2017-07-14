```bash
# for linux:
sudo apt-get install nodejs
sudo apt-get install npm

# for windows https://nodejs.org/en/download/
# while installation choose npm package manager

cd visualization
npm install # couple of warnings is ok

# windows:
mkdir static\js\out
# linux:
mkdir -p static/js/out

npm run watch-accumulative-trees
# wait for few seconds and ctrl + c
npm run watch-call-tree
# wait for few seconds and ctrl + c

# DONE

# -------------------------------------------------------------------------

# if template was changed
java -jar node_modules/google-closure-templates/javascript/SoyToJsSrcCompiler.jar \
    --outputPathFormat static/js/generated/tree-templates.js \
    --srcs templates/tree-templates.soy 
    
cp -r static/* /home/lk/.flamegraph-profiler/static
```