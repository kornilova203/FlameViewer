package com.github.kornilova_l.server;

import java.util.regex.Pattern;

class ServerNames {
    static final String NAME = "flamegraph-profiler";
    static final String MAIN_NAME = "/" + NAME;
    static final String CALL_TREE = MAIN_NAME + "/call-tree";
    static final String CALL_TREE_JS_REQUEST = MAIN_NAME + "/trees/call-tree";
    static final String OUTGOING_CALLS = MAIN_NAME + "/outgoing-calls";
    static final String OUTGOING_CALLS_JS_REQUEST = MAIN_NAME + "/trees/outgoing-calls";
    static final String CALLERS = MAIN_NAME + "/callers";
    static final String CALLERS_JS_REQUEST = MAIN_NAME + "/trees/callers";
    static final Pattern CSS_PATTERN = Pattern.compile(MAIN_NAME + "/css.+css$");
    static final Pattern JS_PATTERN = Pattern.compile(MAIN_NAME + "/js.+js$");
    static final Pattern FONT_PATTERN = Pattern.compile(MAIN_NAME + "/.+ttf$");
}
