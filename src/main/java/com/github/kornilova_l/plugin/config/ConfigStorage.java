package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.Config> {
    @SuppressWarnings("PublicField")
    public static class Config {

        public Config() {
            this(new HashMap<>(), new HashSet<>());
        }

        private Config(HashMap<String, MethodConfig> methods, HashSet<String> patterns) {
            this.methods = methods;
            this.patterns = patterns;
        }

        public HashMap<String, MethodConfig> methods; // node for tree of methods
        public HashSet<String> patterns;

        /**
         * Delete method from config if it exists
         *
         * @param psiMethod wanted method
         * @return true if method was deleted
         */
        public boolean maybeRemove(@NotNull PsiMethod psiMethod) {
            String qualifiedName = getQualifiedName(psiMethod);

            if (methods.containsKey(qualifiedName)) {
                methods.remove(qualifiedName);
                return true;
            }
            return false;
        }

        @NotNull
        public static String getQualifiedName(PsiMethod psiMethod) {
            assert psiMethod.getContainingClass() != null;
            String qualifiedName = psiMethod.getContainingClass().getQualifiedName();
            assert qualifiedName != null;
            return qualifiedName + "." + psiMethod.getName();
        }

        public boolean contains(@NotNull PsiMethod psiMethod) {
            return methods.containsKey(getQualifiedName(psiMethod));
        }

        /**
         * Add method to storage
         * @param psiMethod which will be added
         */
        public void addMethod(@NotNull PsiMethod psiMethod) {
            methods.put(getQualifiedName(psiMethod), new MethodConfig(psiMethod));
        }
    }

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
}
