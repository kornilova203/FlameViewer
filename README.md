# Flamegraph Visualizer
Instrumenting Java Profiler & Flamegraph Visualizer.

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
Plugin will be soon on plugins.jetbrains.com

Build jar with [Flight Recorder parser](https://github.com/kornilova-l/flight-recorder-parser-for-java-9) and place it in lib/ directory.

Generate java files from .proto files. Script is [here](protobuf/README.md)

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
./gradlew :agent:agentJar && \
./gradlew :agent:proxyJar && \
./gradlew copyAgent && \
./gradlew copyStatic && \
./gradlew runIdea

```
