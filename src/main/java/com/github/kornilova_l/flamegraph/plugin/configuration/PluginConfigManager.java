package com.github.kornilova_l.flamegraph.plugin.configuration;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class PluginConfigManager {
    private static final Map<Project, Configuration> states = new HashMap<>();

    public static Configuration getConfiguration(@NotNull Project project) {
        return states.computeIfAbsent(
                project,
                k -> ((ConfigStorage) project.getComponent(PersistentStateComponent.class)).getState());
    }

    public static void exportConfig(@NotNull File file, @NotNull Configuration configuration) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(configuration.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static MethodConfig newMethodConfig(@NotNull PsiMethod psiMethod) {
        String classPatternString;
        String methodPatternString;
        if (psiMethod.getContainingClass() == null ||
                psiMethod.getContainingClass().getQualifiedName() == null) {
            classPatternString = "";
        } else {
            classPatternString = formClassString(psiMethod);
        }
        methodPatternString = psiMethod.getName();
        LinkedList<MethodConfig.Parameter> parameters = getParametersList(psiMethod.getParameterList().getParameters());
        return new MethodConfig(classPatternString, methodPatternString, parameters, true, false);
    }

    @NotNull
    private static String formClassString(@NotNull PsiMethod psiMethod) {
        StringBuilder stringBuilder = new StringBuilder();
        PsiClass psiClass = psiMethod.getContainingClass();
        String packageName = "";
        while (psiClass != null) {
            stringBuilder.insert(0, psiClass.getName() + "$");
            PsiClass nextClass = psiClass.getContainingClass();
            if (nextClass == null) {
                packageName = psiClass.getQualifiedName();
                if (packageName != null) {
                    int dot = packageName.lastIndexOf(".");
                    if (dot == -1) {
                        packageName = "";
                    } else {
                        packageName = packageName.substring(0, packageName.lastIndexOf("."));
                    }
                } else {
                    packageName = "";
                }
            }
            psiClass = nextClass;
        }
        String string = stringBuilder.toString();
        if (Objects.equals(packageName, "")) {
            return string.substring(0, string.length() - 1);
        }
        return packageName + "." + string.substring(0, string.length() - 1);
    }

    @NotNull
    private static LinkedList<MethodConfig.Parameter> getParametersList(PsiParameter[] psiParameters) {
        LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
        for (PsiParameter psiParameter : psiParameters) {
            if (psiParameter.getTypeElement() == null ||
                    psiParameter.getName() == null) {
                continue;
            }
            parameters.add(new MethodConfig.Parameter(psiTypeToString(psiParameter.getTypeElement()), false));
        }
        return parameters;
    }

    @NotNull
    private static String psiTypeToString(@NotNull PsiTypeElement typeElement) {
        String name = removeGeneric(typeElement.getType().getPresentableText());
        String typeElementString = removeGeneric(typeElement.toString());
        int dot = typeElementString.indexOf(".");
        if (dot != -1) {
            String outerClasses = typeElementString.substring(typeElementString.indexOf(":") + 1, typeElementString.lastIndexOf("."));
            return outerClasses.replaceAll("\\.", "\\$") + "$" + name;
        }
        return name;
    }

    @NotNull
    private static String removeGeneric(@NotNull String typeName) {
        int generic = typeName.indexOf('<');
        if (generic != -1) {
            return typeName.substring(0, generic)
                    + typeName.substring(typeName.lastIndexOf('>') + 1, typeName.length());
        }
        return typeName;
    }

    @NotNull
    public static MethodConfig newMethodConfig(TreeProtos.Tree.Node node) {
        TreeProtos.Tree.Node.NodeInfo nodeInfo = node.getNodeInfo();
        String desc = nodeInfo.getDescription();
        return new MethodConfig(nodeInfo.getClassName(),
                nodeInfo.getMethodName(),
                desc.substring(0, desc.indexOf(")") + 1));
    }

    @NotNull
    public static Configuration newConfiguration(@Nullable String[] includingConfigs,
                                                 @Nullable String[] excludingConfigs) {
        Configuration configuration = new Configuration();
        if (includingConfigs != null) {
            for (String includingConfig : includingConfigs) {
                if (includingConfig != null) {
                    configuration.addMethodConfig(includingConfig, false);
                }
            }
        }
        if (excludingConfigs != null) {
            for (String excludingConfig : excludingConfigs) {
                if (excludingConfig != null) {
                    configuration.addMethodConfig(excludingConfig, true);
                }
            }
        }
        return configuration;
    }
}
