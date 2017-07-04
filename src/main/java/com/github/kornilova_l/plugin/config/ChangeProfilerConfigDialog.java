package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.popup.util.DetailController;
import com.intellij.ui.popup.util.DetailViewImpl;
import com.intellij.ui.popup.util.ItemWrapper;
import com.intellij.ui.popup.util.MasterController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

// TODO: reimplement
public class ChangeProfilerConfigDialog extends DialogWrapper {
    @NotNull
    private final Project project;
    @NotNull
    private final CheckboxTree tree;


    final JLabel temp = new JLabel();

    private final MasterController myMasterController = new MasterController() {
        @Override
        public ItemWrapper[] getSelectedItems() {
            return null;
        }

        @Override
        public JLabel getPathLabel() {
            return temp;
        }
    };

    private final DetailController detailController = new DetailController(myMasterController);

    protected ChangeProfilerConfigDialog(@NotNull Project project) {
        super(project);
        this.project = project;

        CheckedTreeNode top =
                new CheckedTreeNode("The Java Series");
        createNodes(top);

        tree = new CheckboxTree(new CheckboxTree.CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                myCheckbox.setText(value.toString());
                super.customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        }, top);

//        JFrame f = new JFrame();
//        f.setLayout(new BorderLayout());
//
//        f.add(jPanel, BorderLayout.SOUTH);
//        f.pack();
//        f.setVisible(true);

        setTitle("Profiler Configuration");
        setModal(false);
        init();
        setOKButtonText("Done");
    }

    private class BookInfo {
        private final String s;
        private final String s1;

        public BookInfo(String s, String s1) {
            this.s = s;
            this.s1 = s1;
        }

        @Override
        public String toString() {
            return s + s1;
        }
    }


    private void createNodes(CheckedTreeNode top) {
        CheckedTreeNode category;
        CheckedTreeNode book;

        category = new CheckedTreeNode("Books for Java Programmers");
        top.add(category);

        //original Tutorial
        book = new CheckedTreeNode(new BookInfo
                ("The Java Tutorial: A Short Course on the Basics",
                        "tutorial.html"));
        category.add(book);

        //Tutorial Continued
        book = new CheckedTreeNode(new BookInfo
                ("The Java Tutorial Continued: The Rest of the JDK",
                        "tutorialcont.html"));
        category.add(book);

        //Swing Tutorial
        book = new CheckedTreeNode(new BookInfo
                ("The Swing Tutorial: A Guide to Constructing GUIs",
                        "swingtutorial.html"));
        category.add(book);

        //...add more books for programmers...

        category = new CheckedTreeNode("Books for Java Implementers");
        top.add(category);

        //VM
        book = new CheckedTreeNode(new BookInfo
                ("The Java Virtual Machine Specification",
                        "vm.html"));
        category.add(book);

        //Language Spec
        book = new CheckedTreeNode(new BookInfo
                ("The Java Language Specification",
                        "jls.html"));
        category.add(book);
//        new MyTreeCellRenderer().
    }

    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    private String getSplitterProportionKey() {
        return getDimensionServiceKey() + ".splitter";
    }

    private JComponent createDetailView() {
        DetailViewImpl detailView = new DetailViewImpl(project);
        detailView.setEmptyLabel("Select configuration");
        detailController.setDetailView(detailView);

        return detailView;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JBSplitter splitPane = new JBSplitter(0.3f);
        splitPane.setSplitterProportionKey(getSplitterProportionKey());

        splitPane.setFirstComponent(createMasterView());
        splitPane.setSecondComponent(createDetailView());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createMasterView() {
        System.out.println("create master view");
        JScrollPane treeView = new JScrollPane(tree);
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(treeView);
        return panel;
//        treeController = new ConfigItemsTreeController();
//        final JTree tree = new ConfigsCheckboxTree(project, treeController);
//
//        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tree).
//                setAddAction(new AnActionButtonRunnable() {
//                    @Override
//                    public void run(AnActionButton button) {
//                        System.out.println("run");
//                    }
//                }).
//                setToolbarPosition(ActionToolbarPosition.TOP).
//                setToolbarBorder(IdeBorderFactory.createEmptyBorder());
//
//        JPanel decoratedTree = decorator.createPanel();
//        decoratedTree.setBorder(IdeBorderFactory.createEmptyBorder());
//
//        JScrollPane pane = UIUtil.getParentOfType(JScrollPane.class, tree);
//        if (pane != null) pane.setBorder(IdeBorderFactory.createBorder());
//
//        treeController.setTreeView(tree);
//
//        treeController.buildTree(myConfigsItems);
//
//        initSelection(myConfigsItems);
//
////        myConfigsPanelProviders.forEach(provider -> provider.addListener(myRebuildAlarm::cancelAndRequest, project, myListenerDisposable));
//
//        return decoratedTree;
    }
}
