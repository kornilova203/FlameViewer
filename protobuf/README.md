```bash
# JavaScript
cd protobuf/src/main/java
protoc --js_out=import_style=commonjs,binary:. \
 com/github/kornilova_l/flamegraph/proto/event.proto \
 com/github/kornilova_l/flamegraph/proto/tree.proto \
 com/github/kornilova_l/flamegraph/proto/trees.proto \
 com/github/kornilova_l/flamegraph/proto/tree_preview.proto \
 com/github/kornilova_l/flamegraph/proto/trees_preview.proto
mv com/github/kornilova_l/flamegraph/proto/*.js ../../../../visualization/static/js/generated
cd ../../../../
perl -p -i -e "s/require\('..\/..\/..\/..\/..\/com\/github\/kornilova_l\/flamegraph\/proto/require('./g" visualization/static/js/generated/*.js
```