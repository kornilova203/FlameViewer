```bash
sudo apt-get install nodejs
sudo apt-get install npm

cd visualization
npm install # couple of warnings is ok
npm run watch-js

# if template was changed
java -jar node_modules/google-closure-templates/javascript/SoyToJsSrcCompiler.jar \
    --outputPathFormat js/src/tree-templates.js \
    --srcs templates/tree-templates.soy 
```