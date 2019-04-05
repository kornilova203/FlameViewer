package com.github.korniloval.flameviewer.converters

enum class ResultType(val url: String) {
    BACKTRACES("trees/back-traces"),
    CALLTRACES("trees/call-traces"),
    CALLTREE("trees/call-tree"),
    HOTSPOTS("hot-spots-json"),
    PREVIEW("trees/call-tree/preview")
}
