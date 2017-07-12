package com.github.kornilova_l.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.Config> {
    Config config;
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");


    public ConfigStorage() {
        config = new Config();
    }

    public Config getState() {
        return config;
    }

    public void loadState(Config config) {
        this.config = config;
    }

    @SuppressWarnings("PublicField")
    public static class Config {

        public Collection<MethodConfig> includingMethodConfigs; // node for tree of includingMethodConfigs
        public Collection<MethodConfig> excludingMethodConfigs; // node for tree of includingMethodConfigs

        public Config() {
            this(new TreeSet<>(), new TreeSet<>());
        }

        private Config(TreeSet<MethodConfig> includingMethodConfigs, TreeSet<MethodConfig> excludingMethodConfigs) {
            this.includingMethodConfigs = includingMethodConfigs;
            this.excludingMethodConfigs = excludingMethodConfigs;
        }

        public void exportConfig(@NotNull File file) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                for (MethodConfig methodConfig : includingMethodConfigs) {
                    if (methodConfig.isEnabled) {
                        outputStream.write((
                                methodConfig.toStringForExport() + "\n")
                                .getBytes());
                    }
                }
                for (MethodConfig methodConfig : excludingMethodConfigs) {
                    if (methodConfig.isEnabled) {
                        outputStream.write((
                                "!" +
                                        methodConfig.toStringForExport() + "\n")
                                .getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @NotNull
        private Collection<MethodConfig> getExcludingConfigs(@NotNull PsiMethod psiMethod) {
            Collection<MethodConfig> excludingConfigs = new TreeSet<>();
            for (MethodConfig methodConfig : excludingMethodConfigs) {
                if (methodConfig.isApplicableTo(psiMethod)) {
                    excludingConfigs.add(methodConfig);
                }
            }
            return excludingConfigs;
        }

        @NotNull
        private Collection<MethodConfig> getIncludingConfigs(@NotNull PsiMethod psiMethod) {
            Collection<MethodConfig> includingConfigs = new TreeSet<>();
            for (MethodConfig methodConfig : includingMethodConfigs) {
                if (methodConfig.isApplicableTo(psiMethod)) {
                    includingConfigs.add(methodConfig);
                }
            }
            return includingConfigs;
        }

        public void maybeRemoveExactIncludingConfig(PsiMethod method) {
            includingMethodConfigs.remove(new MethodConfig(method));
        }

        public boolean isMethodExcluded(PsiMethod psiMethod) {
            return getExcludingConfigs(psiMethod).size() != 0;
        }

        public boolean isMethodInstrumented(PsiMethod method) {
            return getExcludingConfigs(method).size() == 0 &&
                    getIncludingConfigs(method).size() != 0;
        }

        public void maybeRemoveExactExcludingConfig(PsiMethod method) {
            excludingMethodConfigs.remove(new MethodConfig(method));
        }

        public void addMethodConfig(PsiMethod psiMethod, boolean isExcluding) {
            if (isExcluding) {
                excludingMethodConfigs.add(new MethodConfig(psiMethod));
            } else {
                includingMethodConfigs.add(new MethodConfig(psiMethod));
            }
        }

        public void addMethodConfig(String methodConfigLine, boolean isExcluding) {
            if (isExcluding) {
                excludingMethodConfigs.add(new MethodConfig(methodConfigLine));
            } else {
                includingMethodConfigs.add(new MethodConfig(methodConfigLine));
            }
        }

        public MethodConfig addMethodConfig(@NotNull String classPatternString,
                                    @NotNull String methodPatternString,
                                    @NotNull String parametersPattern,
                                    boolean isExcluding) {
            if (isExcluding) {
                MethodConfig methodConfig = new MethodConfig(classPatternString, methodPatternString, parametersPattern);
                excludingMethodConfigs.add(methodConfig);
                return methodConfig;
            } else {
                MethodConfig methodConfig = new MethodConfig(classPatternString, methodPatternString, parametersPattern);
                includingMethodConfigs.add(new MethodConfig(classPatternString, methodPatternString, parametersPattern));
                return methodConfig;
            }
        }

        @NotNull
        public List<MethodConfig> findIncludingConfigs(@NotNull String className) {
            List<MethodConfig> wantedConfigs = new LinkedList<>();
            for (MethodConfig methodConfig : includingMethodConfigs) {
                if (methodConfig.isApplicableToClass(className)) {
                    wantedConfigs.add(methodConfig);
                }
            }
            return wantedConfigs;
        }

        public boolean isMethodExcluded(String className, String methodName, String jvmDesc) {
            String[] jvmParameters = splitJvmParams(jvmDesc.substring(jvmDesc.indexOf("(") + 1, jvmDesc.indexOf(")")));
            for (MethodConfig excludingMethodConfig : excludingMethodConfigs) {
                if (excludingMethodConfig.isApplicableTo(className, methodName, jvmParameters)) {
                    return true;
                }
            }
            return false;
        }

        @Nullable
        private static String[] splitJvmParams(String partOfDescWithParams) {
            ArrayList<String> paramsDesc = new ArrayList<>();
            Matcher m = paramsPattern.matcher(partOfDescWithParams);
            while (m.find()) {
                paramsDesc.add(m.group());
            }
            if (paramsDesc.isEmpty()) {
                return null;
            }
            String[] ret = new String[paramsDesc.size()];
            paramsDesc.toArray(ret);
            return ret;
        }
    }
}
