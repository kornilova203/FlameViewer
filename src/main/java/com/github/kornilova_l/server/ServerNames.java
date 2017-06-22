package com.github.kornilova_l.server;

import java.util.regex.Pattern;

class ServerNames {
    static final String NAME = "flamegraph-profiler";
    static final String MAIN_NAME = "/" + NAME;
    static final String RESULTS = MAIN_NAME + "/results";
    static final String ORIGINAL_TREE = MAIN_NAME + "/trees/original-tree";
    static final Pattern CSS_PATTERN = Pattern.compile(MAIN_NAME + "/css.+css$");
    static final Pattern JS_PATTERN = Pattern.compile(MAIN_NAME + "/js.+js$");
    static final Pattern FONT_PATTERN = Pattern.compile(MAIN_NAME + "/.+ttf$");
}
