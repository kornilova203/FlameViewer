package com.github.kornilova_l.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.TreeSet;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.Config> {
    Config config;

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

        public Collection<MethodConfig> methodConfigs; // node for tree of methodConfigs

        public Config() {
            this(new TreeSet<>());
        }

        private Config(TreeSet<MethodConfig> methodConfigs) {
            this.methodConfigs = methodConfigs;
        }

        /**
         * Return collection of applicable configs
         *
         * @param psiMethod method
         * @return empty collection if method will not be instrumented
         */
        @NotNull
        public Collection<MethodConfig> getIncludingConfigs(@NotNull PsiMethod psiMethod) {
            Collection<MethodConfig> includingConfigs = new TreeSet<>();
            for (MethodConfig methodConfig : methodConfigs) {
                if (methodConfig.isApplicableTo(psiMethod) &&
                        !methodConfig.isExcluding) {
                    includingConfigs.add(methodConfig);
                }
            }
            return includingConfigs;
        }

        public void addMethod(@NotNull PsiMethod psiMethod, boolean isExcluding) {
            methodConfigs.add(new MethodConfig(psiMethod, isExcluding));
        }

        public void exportConfig(@NotNull File file) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                for (MethodConfig methodConfig : methodConfigs) {
                    outputStream.write((
                            methodConfig.toString() + " " + methodConfig.getWhichParamsAreEnabled() + "\n")
                            .getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void addMethod(String classPattern, String methodPattern, String parametersPattern) {
            methodConfigs.add(new MethodConfig(classPattern, methodPattern, parametersPattern));
        }

        public void maybeRemoveExactIncludingConfig(PsiMethod method) {
            methodConfigs.remove(new MethodConfig(method, false));
        }

        public boolean isMethodExcluded(PsiMethod psiMethod) {
            return getExcludingConfigs(psiMethod).size() != 0;
        }

        @NotNull
        public Collection<MethodConfig> getExcludingConfigs(@NotNull PsiMethod psiMethod) {
            Collection<MethodConfig> includingConfigs = new TreeSet<>();
            for (MethodConfig methodConfig : methodConfigs) {
                if (methodConfig.isApplicableTo(psiMethod) &&
                        methodConfig.isExcluding) {
                    includingConfigs.add(methodConfig);
                }
            }
            return includingConfigs;
        }

        public boolean isMethodInstrumented(PsiMethod method) {
            return getExcludingConfigs(method).size() == 0 &&
                    getIncludingConfigs(method).size() != 0;
        }
    }
}
