package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog

import com.github.kornilova_l.flamegraph.configuration.MethodConfig
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.decorator_actions.DialogHelper
import com.github.kornilova_l.flamegraph.plugin.ui.line_markers.LineMarkersHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

class ChangeConfigurationDialogTest : LightCodeInsightFixtureTestCase() {

    fun testOpenAndClose() {
        val dialog = ChangeConfigurationDialog(myFixture.project)
        assertTrue(dialog.isOKActionEnabled)
        assertFalse(dialog.isOK)
        dialog.doOKAction()
        assertTrue(dialog.isOK)
    }

    fun testAddConfig() {
        val dialog = ChangeConfigurationDialog(myFixture.project)
        val className = "package.Class"
        val methodName = "*"
        assertFalse(contains(dialog.trueConfiguration.includingMethodConfigs, methodName, className, 0))

        DialogHelper.saveConfig(className, methodName, false,
                ArrayList<MethodConfig.Parameter>(), dialog.includedTree, dialog.tempConfiguration)
        assertFalse(dialog.isOK)
        dialog.doOKAction()
        assertTrue(dialog.isOK)

        assertTrue(contains(dialog.trueConfiguration.includingMethodConfigs, methodName, className, 0))
    }

    fun testRemoveMethodInEditor() {
        val file = myFixture.copyFileToProject("src/test/resources/com/github/kornilova_l/flamegraph/plugin/ui/config_dialog/Main.java")
        myFixture.openFileInEditor(file)

        val className = "*.Main"
        val methodName = "main"

        val dialog = ChangeConfigurationDialog(myFixture.project)
        DialogHelper.saveConfig(className, methodName, false,
                listOf(MethodConfig.Parameter("*", false)), dialog.includedTree, dialog.tempConfiguration)

        dialog.doOKAction()

        val lineMarkerHolder = project.getComponent(LineMarkersHolder::class.java)
        assertEquals(1, lineMarkerHolder.lineMarkersCount)

        ApplicationManager.getApplication().runWriteAction({
            file.setBinaryContent(("package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;\n" +
                    "\n" +
                    "public class Main {\n" +
                    "    public static void newMain(String[] args) {\n" +
                    "\n" +
                    "    }\n" +
                    "}").toByteArray())
        })

        myFixture.doHighlighting() // rebuild tree

        assertEquals(0, lineMarkerHolder.lineMarkersCount) // line marker still exist. But it's method is invalid
    }

    private fun contains(methodConfigs: List<MethodConfig>, methodName: String, className: String, parametersCount: Int): Boolean {
        for (methodConfig in methodConfigs) {
            if (methodConfig.classPatternString == className && methodConfig.methodPatternString == methodName &&
                    methodConfig.parameters.size == parametersCount) {
                return true
            }
        }
        return false
    }
}