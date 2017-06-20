```bash
# Java
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/src/event.proto
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/src/tree.proto

# JavaScript
protoc --js_out=import_style=commonjs,binary:. src/main/java/com/github/kornilova_l/protos/src/event.proto
# change import in tree.proto to "src/main/java/com/github/kornilova_l/protos/event.proto"
protoc --js_out=import_style=commonjs,binary:. src/main/java/com/github/kornilova_l/protos/src/tree.proto
# move files and fix import in tree_pb.js
```