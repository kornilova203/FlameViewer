package com.github.kornilova_l.flamegraph.plugin.configuration;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PluginConfigManager {

    public static void exportConfig(@NotNull File file, @NotNull Configuration configuration) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(configuration.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getClassName(@NotNull PsiMethod psiMethod) {
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass instanceof PsiAnonymousClass) {
            PsiClass outerClass = getOuterClass((PsiAnonymousClass) psiClass);
            if (outerClass != null) {
                return formClassString(outerClass) + "$*";
            }
        } else {
            return formClassString(psiClass);
        }
        return "";
    }

    public static String getMethodName(@NotNull PsiMethod psiMethod) {
        String methodName = psiMethod.getName();
        if (isInit(psiMethod, methodName)) {
            return  "<init>";
        }
        return methodName;
    }

    @NotNull
    public static MethodConfig newMethodConfig(@NotNull PsiMethod psiMethod) {
        String classPatternString = getClassName(psiMethod);
        String methodPatternString = getMethodName(psiMethod);
        LinkedList<MethodConfig.Parameter> parameters = getParametersList(psiMethod.getParameterList().getParameters());
        return new MethodConfig(classPatternString, methodPatternString, parameters == null ? new ArrayList<>() : parameters,
                true, false);
    }

    @Nullable
    private static PsiClass getOuterClass(@NotNull PsiAnonymousClass psiClass) {
        PsiElement psiElement = psiClass.getParent();
        while (psiElement != null) {
            if (psiElement instanceof PsiClass) {
                return ((PsiClass) psiElement);
            }
            psiElement = psiElement.getParent();
        }
        return null;
    }

    private static boolean isInit(PsiMethod psiMethod, String methodPatternString) {
        PsiClass psiClass = psiMethod.getContainingClass();
        return psiClass != null &&
                Objects.equals(psiClass.getName(), methodPatternString);
    }

    @NotNull
    private static String formClassString(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
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

    @Nullable
    public static LinkedList<MethodConfig.Parameter> getParametersList(PsiParameter[] psiParameters) {
        if (psiParameters.length == 0) {
            return null;
        }
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

    @Nullable
    public static List<String> getParametersTypesList(PsiParameter[] psiParameters) {
        if (psiParameters.length == 0) {
            return null;
        }
        List<String> parameters = new ArrayList<>();
        for (PsiParameter psiParameter : psiParameters) {
            if (psiParameter.getTypeElement() == null ||
                    psiParameter.getName() == null) {
                continue;
            }
            parameters.add(psiTypeToString(psiParameter.getTypeElement()));
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
}
