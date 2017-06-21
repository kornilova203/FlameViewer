package com.github.kornilova_l.profiler.agent;

import java.util.ArrayList;
import java.util.regex.Pattern;

class Configuration {

    private static final ArrayList<Pattern> fullNamePatterns = new ArrayList<>();
    private static final ArrayList<Pattern> classNamePatterns = new ArrayList<>();
    private static final ArrayList<Pattern> excludePatterns = new ArrayList<>();

    static void addIncludePattern(String line) {
        String[] parts = line.split("\\.");
        addPattern(parts[0], classNamePatterns);
        addPattern(line.replace(".", "\\."), fullNamePatterns);
    }

    static void addExcludePattern(String line) {
        addPattern(line, excludePatterns);
    }

    private static void addPattern(String line, ArrayList<Pattern> patterns) {
        patterns.add(
                Pattern.compile(
                        line.replaceAll("\\*", ".*")
                )
        );
    }

    static boolean isClassIncluded(String className) {
        return matchesAnyPattern(className, classNamePatterns);
    }

    static boolean isMethodIncluded(String fullName) {
        return matchesAnyPattern(fullName, fullNamePatterns);
    }

    static boolean isMethodExcluded(String fullName) {
        return matchesAnyPattern(fullName, excludePatterns);
    }

    private static boolean matchesAnyPattern(String line, ArrayList<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }
}
