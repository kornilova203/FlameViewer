package com.github.korniloval.flameviewer.converters

enum class ResultType(val url: String) {
    BACKTRACES("trees/incoming-calls"),
    CALLTRACES("trees/outgoing-calls"),
    CALLTREE("trees/call-tree"),
    HOTSPOTS("hot-spots-json"),
    PREVIEW("trees/call-tree/preview")
}
