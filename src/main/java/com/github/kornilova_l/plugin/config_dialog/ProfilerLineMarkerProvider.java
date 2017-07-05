package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ProfilerLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            Collection<? super RelatedItemLineMarkerInfo> result) {
        super.getLineMarkerInfo(element);
        ConfigStorage.Config config = ProjectConfigManager.getConfig(element.getProject());
        if (element instanceof PsiMethod) {
            PsiMethod psiMethod = ((PsiMethod) element);
            if (config.contains(psiMethod)) {
                addIcon(element, result);
            } else {
                removeIconIfPresent(element.getProject(), result);
            }
        }
    }

    private static void removeIconIfPresent(Project project, Collection<? super RelatedItemLineMarkerInfo> result) {
        System.out.println("removeIconIfPresent");
        ConfigStorage.Config config = ProjectConfigManager.getConfig(project);
        System.out.println(result);
//        result.clear();
        for (Object markerInfo : result) {
//            ((LineMarkerInfo) markerInfo);
        }
    }

    private static void addIcon(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(AllIcons.Css.Pseudo_class)
                        .setTargets(element);
        result.add(builder.createLineMarkerInfo(element));
    }
}
