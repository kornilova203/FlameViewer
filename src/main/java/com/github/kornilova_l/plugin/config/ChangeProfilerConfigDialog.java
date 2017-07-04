package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.popup.util.DetailController;
import com.intellij.ui.popup.util.ItemWrapper;
import com.intellij.ui.popup.util.MasterController;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointsDialogState;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointPanelProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

// TODO: reimplement
public class ChangeProfilerConfigDialog extends DialogWrapper {
    @NotNull
    private final Project myProject;
    private final List<ConfigPanelProvider> myConfigsPanelProviders;
    private final Object myInitialBreakpoint;
    private final Collection<ConfigItem> myConfigsItems = new ArrayList<>();
    private ConfigItemsTreeController treeController;
    final JLabel temp = new JLabel();
    private final Set<XConfigGroupingRule> myRulesEnabled = new TreeSet<>(XConfigGroupingRule.PRIORITY_COMPARATOR);
    private final List<XConfigGroupingRule> myRulesAvailable = new ArrayList<>();

    private final MasterController myMasterController = new MasterController() {
        @Override
        public ItemWrapper[] getSelectedItems() {
            final List<ConfigItem> res = treeController.getSelectedConfigs(false);
            return res.toArray(new ItemWrapper[res.size()]);
        }

        @Override
        public JLabel getPathLabel() {
            return temp;
        }
    };
    private final DetailController myDetailController = new DetailController(myMasterController);

    protected ChangeProfilerConfigDialog(@NotNull Project project, Object config, @NotNull List<ConfigPanelProvider> providers) {
        super(project);
        myProject = project;
        myConfigsPanelProviders = providers;
        myInitialBreakpoint = config;

        collectGroupingRules();

        collectItems();

        setTitle("Breakpoints");
        setModal(false);
        init();
        setOKButtonText("Done");
    }

    void collectItems() {
        if (!myConfigsPanelProviders.isEmpty()) {
            disposeItems();
            myConfigsItems.clear();
            for (ConfigPanelProvider panelProvider : myConfigsPanelProviders) {
                panelProvider.provideConfigItems(myProject, myConfigsItems);
            }
        }
    }

    private void disposeItems() {
        myConfigsItems.forEach(ConfigItem::dispose);
    }

    private void collectGroupingRules() {
        for (ConfigPanelProvider provider : myConfigsPanelProviders) {
            provider.createConfigsGroupingRules(myRulesAvailable);
        }
        myRulesAvailable.sort(XConfigGroupingRule.PRIORITY_COMPARATOR);

        myRulesEnabled.clear();

        for (XConfigGroupingRule rule : myRulesAvailable) {
            if (rule.isAlwaysEnabled()) {
                myRulesEnabled.add(rule);
            }
        }

    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    private String getSplitterProportionKey() {
        return getDimensionServiceKey() + ".splitter";
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JBSplitter splitPane = new JBSplitter(0.3f);
        splitPane.setSplitterProportionKey(getSplitterProportionKey());

        splitPane.setFirstComponent(createMasterView());
//        splitPane.setSecondComponent(createDetailView());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private void saveCurrentItem() {
        ItemWrapper item = myDetailController.getSelectedItem();
        if (item instanceof ConfigItem) {
            ((ConfigItem) item).saveState();
        }
    }

    private JComponent createMasterView() {
        treeController = new ConfigItemsTreeController(myRulesEnabled) {
            @Override
            public void nodeStateWillChangeImpl(CheckedTreeNode node) {
                if (node instanceof ConfigItemNode) {
                    ((ConfigItemNode) node).getConfigItem().saveState();
                }
                super.nodeStateWillChangeImpl(node);
            }

            @Override
            public void nodeStateDidChangeImpl(CheckedTreeNode node) {
                super.nodeStateDidChangeImpl(node);
                if (node instanceof ConfigItemNode) {
                    myDetailController.doUpdateDetailView(true);
                }
            }

            @Override
            protected void selectionChangedImpl() {
                super.selectionChangedImpl();
                saveCurrentItem();
                myDetailController.updateDetailView();
            }
        };
        final JTree tree = new ConfigsCheckboxTree(myProject, treeController) {
            @Override
            protected void onDoubleClick(CheckedTreeNode node) {
                if (node instanceof ConfigsGroupNode) {
                    TreePath path = TreeUtil.getPathFromRoot(node);
                    if (isExpanded(path)) {
                        collapsePath(path);
                    } else {
                        expandPath(path);
                    }
                } else {
                    navigate(false);
                }
            }
        };

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tree).
                setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        System.out.println("run");
                    }
                }).
                setToolbarPosition(ActionToolbarPosition.TOP).
                setToolbarBorder(IdeBorderFactory.createEmptyBorder());

        JPanel decoratedTree = decorator.createPanel();
        decoratedTree.setBorder(IdeBorderFactory.createEmptyBorder());

        JScrollPane pane = UIUtil.getParentOfType(JScrollPane.class, tree);
        if (pane != null) pane.setBorder(IdeBorderFactory.createBorder());

        treeController.setTreeView(tree);

        treeController.buildTree(myConfigsItems);

        initSelection(myConfigsItems);

//        myConfigsPanelProviders.forEach(provider -> provider.addListener(myRebuildAlarm::cancelAndRequest, myProject, myListenerDisposable));

        return decoratedTree;
    }

    private void initSelection(Collection<ConfigItem> myConfigsItems) {
        TreeUtil.expandAll(treeController.getTreeView());
        treeController.selectFirstConfigItem();
        selectBreakpoint(myInitialBreakpoint);
    }

    private boolean selectBreakpoint(Object config) {
        if (config != null) {
            for (ConfigItem item : myConfigsItems) {
                if (item.getConfig() == config) {
                    treeController.selectConfigItem(item, null);
                    return true;
                }
            }
        }
        return false;
    }

    private void navigate(final boolean requestFocus) {
        treeController.getSelectedConfigs(false).stream().findFirst().ifPresent(b -> b.navigate(requestFocus));
    }
}
