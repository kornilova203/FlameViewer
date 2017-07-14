# Java profiler for IntelliJ IDEA
see [link js files](/visualization/README.md)
windows:
```
gradlew :agent:jar && \
gradlew copyAgent && \
gradlew copyStatic && \
gradlew copyIcons && \
gradlew runIdea
```

Linux:
```bash
./gradlew :agent:jar && \
./gradlew copyAgent && \
./gradlew copyStatic && \
./gradlew copyIcons && \
./gradlew runIdea

```