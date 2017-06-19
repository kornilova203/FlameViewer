package com.github.kornilova_l.profiler;

import java.util.ArrayList;
import java.util.regex.Pattern;

class Configuration {
    static final ArrayList<Pattern> fullNamePatterns = new ArrayList<>();
    static final ArrayList<Pattern> classNamePatterns = new ArrayList<>();

    static void addClassNamePattern(String line) {
        String className = line.split("\\.")[0];
        classNamePatterns.add(
                Pattern.compile(
                        className.replaceAll("\\*", ".*")
                )
        );
    }

    static void addFullNamePattern(String line) {
        fullNamePatterns.add(
                Pattern.compile(
                        line.replaceAll("\\*", ".*")
                )
        );
    }

    static boolean matchesAnyPattern(String string, ArrayList<Pattern> patterns) {
        for (Pattern classNamePattern : patterns) {
            if (classNamePattern.matcher(string).matches()) {
                return true;
            }
        }
        return false;
    }
}
