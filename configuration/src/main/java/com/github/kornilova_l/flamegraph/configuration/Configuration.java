package com.github.kornilova_l.flamegraph.configuration;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class Configuration implements Cloneable {
    private List<MethodConfig> includingMethodConfigs;
    private List<MethodConfig> excludingMethodConfigs;
    private static final Pattern linePattern = Pattern.compile("!?[\\w.$<>]+\\((\\w|\\[]|\\$|\\.|\\+?, |\\+(?=\\)))*\\)\\+?");

    public Configuration() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * copy construction
     *
     * @param configuration configuration to copy
     */
    public Configuration(Configuration configuration) {
        includingMethodConfigs = new ArrayList<>();
        for (MethodConfig includingMethodConfig : configuration.includingMethodConfigs) {
            includingMethodConfigs.add(new MethodConfig(includingMethodConfig));
        }
        excludingMethodConfigs = new ArrayList<>();
        for (MethodConfig excludingMethodConfig : configuration.excludingMethodConfigs) {
            excludingMethodConfigs.add(new MethodConfig(excludingMethodConfig));
        }
    }

    private Configuration(List<MethodConfig> includingMethodConfigs, List<MethodConfig> excludingMethodConfigs) {
        this.includingMethodConfigs = includingMethodConfigs;
        this.excludingMethodConfigs = excludingMethodConfigs;
    }

    public Configuration(List<String> methodConfigLines) {
        this(new ArrayList<>(), new ArrayList<>());
        for (String methodConfigLine : methodConfigLines) {
            addLine(methodConfigLine);
        }
    }

    private void addLine(String methodConfigLine) {
        boolean isExcluding = methodConfigLine.charAt(0) == '!';
        if (isExcluding) {
            methodConfigLine = methodConfigLine.substring(1, methodConfigLine.length());
        }
        addMethodConfig(methodConfigLine, isExcluding);
    }

    public Configuration(InputStream inputStream) {
        this(new ArrayList<>(), new ArrayList<>());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines()
                    .filter(line -> !Objects.equals(line, ""))
                    .forEach(this::addLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValid(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .filter(line -> !Objects.equals(line, ""))
                    .allMatch(line -> linePattern.matcher(line).matches());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @NotNull
    private static List<MethodConfig> getApplicableMethodConfigs(@NotNull List<MethodConfig> methodConfigs,
                                                                 @NotNull MethodConfig testedConfig) {
        List<MethodConfig> applicableMethodConfigs = new ArrayList<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (methodConfig.isApplicableTo(testedConfig)) {
                applicableMethodConfigs.add(methodConfig);
            }
        }
        return applicableMethodConfigs;
    }

    public List<MethodConfig> getIncludingMethodConfigs() {
        return includingMethodConfigs;
    }

    public void setIncludingMethodConfigs(List<MethodConfig> includingMethodConfigs) {
        this.includingMethodConfigs = includingMethodConfigs;
    }

    public List<MethodConfig> getExcludingMethodConfigs() {
        return excludingMethodConfigs;
    }

    public void setExcludingMethodConfigs(List<MethodConfig> excludingMethodConfigs) {
        this.excludingMethodConfigs = excludingMethodConfigs;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (MethodConfig methodConfig : includingMethodConfigs) {
            if (methodConfig.isEnabled()) {
                stringBuilder.append(methodConfig.toString()).append("\n");
            }
        }
        for (MethodConfig methodConfig : excludingMethodConfigs) {
            if (methodConfig.isEnabled()) {
                stringBuilder.append("!").append(methodConfig.toString()).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public void addMethodConfig(MethodConfig methodConfig,
                                boolean isExcluded) {
        if (isExcluded) {
            excludingMethodConfigs.add(methodConfig);
        } else {
            includingMethodConfigs.add(methodConfig);
        }
    }

    public void addMethodConfig(@NotNull String methodConfigLine, boolean isExcluding) {
        String classAndMethod = methodConfigLine.substring(0, methodConfigLine.indexOf("("));
        String classPatternString = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
        String methodPatternString = classAndMethod.substring(
                classAndMethod.lastIndexOf(".") + 1,
                classAndMethod.length()
        );
        String parametersPattern = methodConfigLine.substring(methodConfigLine.indexOf("("), methodConfigLine.length());
        addMethodConfig(
                new MethodConfig(
                        classPatternString,
                        methodPatternString,
                        parametersPattern
                ),
                isExcluding
        );
    }

    public void maybeRemoveExactExcludingConfig(MethodConfig methodConfig) {
        excludingMethodConfigs.remove(methodConfig);
    }

    public void maybeRemoveExactIncludingConfig(MethodConfig methodConfig) {
        includingMethodConfigs.remove(methodConfig);
    }

    public boolean isMethodInstrumented(@NotNull MethodConfig methodConfig) {
        return getExcludingConfigs(methodConfig).size() == 0 &&
                getIncludingConfigs(methodConfig).size() != 0;
    }

    @NotNull
    public List<MethodConfig> getIncludingConfigs(@NotNull MethodConfig methodConfig) {
        return getApplicableMethodConfigs(includingMethodConfigs, methodConfig);
    }

    @NotNull
    public List<MethodConfig> getExcludingConfigs(@NotNull MethodConfig methodConfig) {
        return getApplicableMethodConfigs(excludingMethodConfigs, methodConfig);
    }

    public boolean isMethodExcluded(@NotNull MethodConfig methodConfig) {
        return getExcludingConfigs(methodConfig).size() != 0;
    }

    /**
     * copies links to fields of tempConfiguration
     *
     * @param tempConfiguration configuration from where links will be copied
     */
    public void assign(Configuration tempConfiguration) {
        tempConfiguration.removeDuplicates();
        for (MethodConfig includingMethodConfig : tempConfiguration.includingMethodConfigs) {
            includingMethodConfig.removeEmptyParams();
            includingMethodConfig.clearPatterns();
        }
        includingMethodConfigs = tempConfiguration.includingMethodConfigs;
        for (MethodConfig excludingMethodConfig : tempConfiguration.excludingMethodConfigs) {
            excludingMethodConfig.removeEmptyParams();
            excludingMethodConfig.clearPatterns();
        }
        excludingMethodConfigs = tempConfiguration.excludingMethodConfigs;
    }

    private void removeDuplicates() {
        Set<MethodConfig> temp = new HashSet<>();
        for (MethodConfig includingMethodConfig : includingMethodConfigs) {
            if (!hasDuplicate(temp, includingMethodConfig)) {
                temp.add(new MethodConfig(includingMethodConfig));
            }
        }
        includingMethodConfigs = new ArrayList<>();
        includingMethodConfigs.addAll(temp);
        temp = new HashSet<>();
        for (MethodConfig excludingMethodConfig : excludingMethodConfigs) {
            if (!hasDuplicate(temp, excludingMethodConfig)) {
                temp.add(new MethodConfig(excludingMethodConfig));
            }
        }
        excludingMethodConfigs = new ArrayList<>();
        excludingMethodConfigs.addAll(temp);
    }

    private boolean hasDuplicate(Set<MethodConfig> temp, MethodConfig methodConfig) {
        boolean hasDuplicate = false;
        for (MethodConfig tempConfig : temp) {
            if (Objects.equals(tempConfig.getQualifiedName() + tempConfig.parametersToString(),
                    methodConfig.getQualifiedName() + methodConfig.parametersToString())) {
                hasDuplicate = true;
            }
        }
        return hasDuplicate;
    }
}
