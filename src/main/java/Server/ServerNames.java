package Server;

import com.intellij.openapi.diagnostic.Logger;

import java.util.regex.Pattern;

class ServerNames {
    static final Logger LOG = Logger.getInstance(ProfilerRestService.class.getName());
    static final String NAME = "flamegraph-profiler";
    static final String MAIN_NAME = "/" + NAME;
    static final String RESULTS = MAIN_NAME + "/results";
    static final Pattern CSS_PATTERN = Pattern.compile(MAIN_NAME + "/css.+css$");
    static final Pattern JS_PATTERN = Pattern.compile(MAIN_NAME + "/js.+js$");
}
