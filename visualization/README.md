```bash
sudo apt-get install nodejs
sudo apt-get install npm

cd visualization
npm install # couple of warnings is ok
npm run watch-accumulative-trees
# ctrl + c
npm run watch-call-tree
# ctrl + c

# if template was changed
java -jar node_modules/google-closure-templates/javascript/SoyToJsSrcCompiler.jar \
    --outputPathFormat static/js/generated/tree-templates.js \
    --srcs templates/tree-templates.soy 
    
cp -r static/* /home/lk/.flamegraph-profiler/static
```