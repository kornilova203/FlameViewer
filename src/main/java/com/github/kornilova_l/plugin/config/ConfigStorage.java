package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
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
            String qualifiedNameWithParams = getQualifiedNameWithParams(psiMethod);

            if (methods.containsKey(qualifiedNameWithParams)) {
                methods.remove(qualifiedNameWithParams);
                return true;
            }
            return false;
        }

        @NotNull
        public static String getQualifiedNameWithParams(PsiMethod psiMethod) {
            if (psiMethod.getContainingClass() == null) {
                return psiMethod.getName();
            }
            String classQualifiedName = psiMethod.getContainingClass().getQualifiedName();
            if (classQualifiedName == null) {
                return psiMethod.getName();
            }
            return classQualifiedName +
                    "." +
                    psiMethod.getName() +
                    parametersToString(getParametersList(psiMethod.getParameterList().getParameters()));
        }

        static String parametersToString(List<MethodConfig.Parameter> parameters) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            for (int i = 0; i < parameters.size(); i++) {
                MethodConfig.Parameter parameter = parameters.get(i);
                stringBuilder.append(parameter.type).append(" ").append(parameter.name);
                if (i != parameters.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(")");
            return stringBuilder.toString();
        }

        static LinkedList<MethodConfig.Parameter> getParametersList(PsiParameter[] psiParameters) {
            LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
            for (PsiParameter psiParameter : psiParameters) {
                parameters.add(new MethodConfig.Parameter(psiParameter.getType().getPresentableText(), psiParameter.getName()));
                System.out.println(psiParameter.getType().getPresentableText());
            }
            return parameters;
        }

        public boolean contains(@NotNull PsiMethod psiMethod) {
            return methods.containsKey(getQualifiedNameWithParams(psiMethod));
        }

        /**
         * Add method to storage
         * @param psiMethod which will be added
         */
        public void addMethod(@NotNull PsiMethod psiMethod) {
            methods.put(getQualifiedNameWithParams(psiMethod), new MethodConfig(psiMethod));
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
