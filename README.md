# Java profiler for IntelliJ IDEA
See [link js files](/visualization/README.md)

Windows:
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
