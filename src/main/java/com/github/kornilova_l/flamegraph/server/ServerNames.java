package com.github.kornilova_l.flamegraph.server;

import java.util.regex.Pattern;

class ServerNames {
    private static final String NAME = "flamegraph-profiler";
    static final String MAIN_NAME = "/" + NAME;
    static final String SELECT_FILE = MAIN_NAME + "/select-file";
    static final String FILE_LIST = MAIN_NAME + "/file-list";
    static final String CALL_TREE = MAIN_NAME + "/call-tree";
    static final String CALL_TREE_JS_REQUEST = MAIN_NAME + "/trees/call-tree";
    static final String OUTGOING_CALLS = MAIN_NAME + "/outgoing-calls";
    static final String OUTGOING_CALLS_JS_REQUEST = MAIN_NAME + "/trees/outgoing-calls";
    static final String INCOMING_CALLS = MAIN_NAME + "/incoming-calls";
    static final String INCOMING_CALLS_JS_REQUEST = MAIN_NAME + "/trees/incoming-calls";
    static final String UPLOAD_FILE = MAIN_NAME + "/upload-file";
    static final Pattern CSS_PATTERN = Pattern.compile(MAIN_NAME + "/css.+css$");
    static final Pattern JS_PATTERN = Pattern.compile(MAIN_NAME + "/js.+js$");
    static final Pattern FONT_PATTERN = Pattern.compile(MAIN_NAME + "/.+ttf$");
    static final Pattern PNG_PATTERN = Pattern.compile(MAIN_NAME + "/.+png$");
}
