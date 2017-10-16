# Java profiler for IntelliJ IDEA
Instrumenting Java profiler with flamegraph visualizer.

## Features

Threads preview
![threads preview](screenshots/preview.png)

Detailed view of thread
![](screenshots/thread.png)

Zoom
![](screenshots/zoom.png)

Call Traces
![](screenshots/call-traces.png)

Back Traces
![](screenshots/back-traces.png)

Filtering
![](screenshots/filter.png)

Search
![](screenshots/search.png)

Hot Spots
![](screenshots/hot-spots.png)

## Compiling

See [js files](/visualization/README.md)

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
