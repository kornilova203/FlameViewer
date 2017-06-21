```bash
# Java
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/src/event.proto
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/src/tree.proto
protoc -I=src/main/java --java_out=src/main/java src/main/java/com/github/kornilova_l/protos/src/trees.proto

# JavaScript
cd src/main/java
protoc --js_out=import_style=commonjs,binary:. com/github/kornilova_l/protos/src/event.proto
protoc --js_out=import_style=commonjs,binary:. com/github/kornilova_l/protos/src/tree.proto
protoc --js_out=import_style=commonjs,binary:. com/github/kornilova_l/protos/src/trees.proto
# move files and fix import in tree_pb.js
```