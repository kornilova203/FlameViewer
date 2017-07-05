package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
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
            PsiMethod method = ((PsiMethod) element);
            if (config.contains(method)) {
                addIcon(element, result);
            }
        }
    }

    private static void addIcon(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(AllIcons.Nodes.Package)
                        .setTargets(element);
        result.add(builder.createLineMarkerInfo(element));
    }
}
