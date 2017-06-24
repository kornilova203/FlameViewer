```bash
# Java
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/event.proto
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/tree.proto
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/trees.proto

# JavaScript
cd src/main/java
protoc --js_out=import_style=commonjs,binary:. \
 com/github/kornilova_l/protos/event.proto \
 com/github/kornilova_l/protos/tree.proto \
 com/github/kornilova_l/protos/trees.proto
mv com/github/kornilova_l/protos/*.js ../../../visualization/js/generated
# fix import in tree_pb.js and trees_pb.js
```