package com.github.kornilova_l.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiTypeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import static com.github.kornilova_l.config.MethodConfig.parametersToString;

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

        public HashMap<String, MethodConfig> methods; // node for tree of methods
        public HashSet<String> patterns;

        public Config() {
            this(new HashMap<>(), new HashSet<>());
        }

        private Config(HashMap<String, MethodConfig> methods, HashSet<String> patterns) {
            this.methods = methods;
            this.patterns = patterns;
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
                    parametersToString(getParametersListForComparing(psiMethod.getParameterList().getParameters()));
        }

        static LinkedList<MethodConfig.Parameter> getParametersList(PsiParameter[] psiParameters) {
            LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
            for (PsiParameter psiParameter : psiParameters) {
                String jvmType = getJvmType(psiParameter.getTypeElement());
                if (jvmType == null) {
                    continue;
                }
                parameters.add(new MethodConfig.Parameter(
                        psiParameter.getType().getPresentableText(),
                        psiParameter.getName(),
                        jvmType
                ));
            }
            return parameters;
        }

        static LinkedList<MethodConfig.Parameter> getParametersListForComparing(PsiParameter[] psiParameters) {
            LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
            for (PsiParameter psiParameter : psiParameters) {
                parameters.add(new MethodConfig.Parameter(
                        psiParameter.getType().getPresentableText(),
                        psiParameter.getName()
                ));
            }
            return parameters;
        }

        @Nullable
        private static String getJvmType(@Nullable PsiTypeElement typeElement) {
            if (typeElement == null) {
                return null;
            }
            if (typeElement.getInnermostComponentReferenceElement() == null) { // primitive type
                switch (typeElement.getType().getPresentableText()) {
                    case "int":
                        return "I";
                    case "long":
                        return "J";
                    case "boolean":
                        return "Z";
                    case "char":
                        return "C";
                    case "short":
                        return "S";
                    case "byte":
                        return "B";
                    case "float":
                        return "F";
                    case "double":
                        return "D";
                    default:
                        throw new AssertionError("Not known primitive type");
                }
            }
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < typeElement.getType().getArrayDimensions(); i++) {
                result.append("[");
            }
            return  result.toString() +
                    "L" +
                    typeElement.getInnermostComponentReferenceElement().getQualifiedName().replaceAll("\\.", "/") +
                    ";";
        }

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

        public boolean contains(@NotNull PsiMethod psiMethod) {
            return methods.containsKey(getQualifiedNameWithParams(psiMethod));
        }

        /**
         * Add method to storage
         *
         * @param psiMethod which will be added
         */
        public void addMethod(@NotNull PsiMethod psiMethod) {
            methods.put(getQualifiedNameWithParams(psiMethod), new MethodConfig(psiMethod));
        }

        public void exportConfig(@NotNull File file) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                for (MethodConfig methodConfig : methods.values()) {
                    outputStream.write((
                            methodConfig.toStringForJvm() + " " + methodConfig.getWhichParamsAreEnabled() + "\n")
                            .getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void addMethod(String classPattern, String methodPattern, String parametersPattern) {
            methods.put(classPattern + "." + methodPattern + parametersPattern, new MethodConfig(classPattern, methodPattern, parametersPattern));
        }
    }
}
