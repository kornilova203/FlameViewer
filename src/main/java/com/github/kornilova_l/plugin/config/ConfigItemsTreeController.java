package com.github.kornilova_l.plugin.config;

import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

public class ConfigItemsTreeController implements ConfigsCheckboxTree.Delegate {
    private static final ConfigItemsTreeController.TreeNodeComparator COMPARATOR = new ConfigItemsTreeController.TreeNodeComparator();
    private final CheckedTreeNode myRoot;
    private final Map<ConfigItem, ConfigItemNode> myNodes = new HashMap<>();
    private List<XConfigGroupingRule> myGroupingRules;

    private JTree myTreeView;
    protected boolean myInBuild;

    public ConfigItemsTreeController(Collection<XConfigGroupingRule> groupingRules) {
        myRoot = new CheckedTreeNode("root");
        setGroupingRulesInternal(groupingRules);
    }

    public JTree getTreeView() {
        return myTreeView;
    }

    public void setTreeView(JTree treeView) {
        myTreeView = treeView;
        myTreeView.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                selectionChanged();
            }
        });
        if (treeView instanceof ConfigsCheckboxTree) {
            ((ConfigsCheckboxTree)treeView).setDelegate(this);
        }
        myTreeView.setShowsRootHandles(!myGroupingRules.isEmpty());
    }

    protected void selectionChanged() {
        if (myInBuild) return;
        selectionChangedImpl();
    }

    protected void selectionChangedImpl() {
    }

    @Override
    public void nodeStateDidChange(CheckedTreeNode node) {
        if (myInBuild) return;
        nodeStateDidChangeImpl(node);
    }

    protected void nodeStateDidChangeImpl(CheckedTreeNode node) {
        if (node instanceof ConfigItemNode) {
            ((ConfigItemNode)node).getConfigItem().setEnabled(node.isChecked());
        }
    }

    @Override
    public void nodeStateWillChange(CheckedTreeNode node) {
        if (myInBuild) return;
        nodeStateWillChangeImpl(node);
    }

    protected void nodeStateWillChangeImpl(CheckedTreeNode node) {
    }

    private void setGroupingRulesInternal(final Collection<XConfigGroupingRule> groupingRules) {
        myGroupingRules = new ArrayList<>(groupingRules);
    }

    public void buildTree(@NotNull Collection<? extends ConfigItem> settingsItems) {
        final TreeState state = TreeState.createOn(myTreeView, myRoot);
        myRoot.removeAllChildren();
        myNodes.clear();
        for (ConfigItem settingsItem : settingsItems) {
            ConfigItemNode node = new ConfigItemNode(settingsItem);
            CheckedTreeNode parent = getParentNode(settingsItem);
            parent.add(node);
            myNodes.put(settingsItem, node);
        }
        TreeUtil.sortRecursively(myRoot, COMPARATOR);
        myInBuild = true;
        ((DefaultTreeModel)(myTreeView.getModel())).nodeStructureChanged(myRoot);
        state.applyTo(myTreeView, myRoot);
        myInBuild = false;
    }


    @NotNull
    private CheckedTreeNode getParentNode(final ConfigItem config) {
        CheckedTreeNode parent = myRoot;
        for (int i = 0; i < myGroupingRules.size(); i++) {
            XConfigGroup group = myGroupingRules.get(i).getGroup(config.getConfig(), Collections.emptyList());
            if (group != null) {
                parent = getOrCreateGroupNode(parent, group, i);
                if (config.isEnabled()) {
                    parent.setChecked(true);
                }
            }
        }
        return parent;
    }

    private static Collection<XConfigGroup> getGroupNodes(CheckedTreeNode parent) {
        Collection<XConfigGroup> nodes = new ArrayList<>();
        Enumeration children = parent.children();
        while (children.hasMoreElements()) {
            Object element = children.nextElement();
            if (element instanceof ConfigsGroupNode) {
                nodes.add(((ConfigsGroupNode)element).getGroup());
            }
        }
        return nodes;
    }

    private static ConfigsGroupNode getOrCreateGroupNode(CheckedTreeNode parent, final XConfigGroup group,
                                                             final int level) {
        Enumeration children = parent.children();
        while (children.hasMoreElements()) {
            Object element = children.nextElement();
            if (element instanceof ConfigsGroupNode) {
                XConfigGroup groupFound = ((ConfigsGroupNode)element).getGroup();
                if (groupFound.equals(group)) {
                    return (ConfigsGroupNode)element;
                }
            }
        }
        ConfigsGroupNode groupNode = new ConfigsGroupNode<>(group, level);
        parent.add(groupNode);
        return groupNode;
    }

    public void setGroupingRules(Collection<XConfigGroupingRule> groupingRules) {
        setGroupingRulesInternal(groupingRules);
        rebuildTree(new ArrayList<>(myNodes.keySet()));
    }

    public void rebuildTree(Collection<ConfigItem> items) {
        List<ConfigItem> selectedConfigs = getSelectedConfigs(false);
        TreePath path = myTreeView.getSelectionPath();
        buildTree(items);
        if (myTreeView.getRowForPath(path) == -1 && !selectedConfigs.isEmpty()) {
            selectConfigItem(selectedConfigs.get(0), path);
        }
        else {
            selectConfigItem(null, path);
        }
    }

    public List<ConfigItem> getSelectedConfigs(boolean traverse) {
        TreePath[] selectionPaths = myTreeView.getSelectionPaths();
        if (selectionPaths == null || selectionPaths.length == 0) return Collections.emptyList();

        final ArrayList<ConfigItem> list = new ArrayList<>();
        for (TreePath selectionPath : selectionPaths) {
            TreeNode startNode = (TreeNode)selectionPath.getLastPathComponent();
            if (traverse) {
                TreeUtil.traverseDepth(startNode, node -> {
                    if (node instanceof ConfigItemNode) {
                        list.add(((ConfigItemNode)node).getConfigItem());
                    }
                    return true;
                });
            }
            else {
                if (startNode instanceof ConfigItemNode) {
                    list.add(((ConfigItemNode)startNode).getConfigItem());
                }
            }
        }

        return list;
    }

    public void selectConfigItem(@Nullable final ConfigItem config, TreePath path) {
        ConfigItemNode node = myNodes.get(config);
        if (node != null) {
            TreeUtil.selectNode(myTreeView, node);
        }
        else {
            TreeUtil.selectPath(myTreeView, path);
        }
    }

    public CheckedTreeNode getRoot() {
        return myRoot;
    }

    public void selectFirstConfigItem() {
        TreeUtil.selectPath(myTreeView, TreeUtil.getFirstLeafNodePath(myTreeView));
    }

    public void removeSelectedConfigs(Project project) {
        final TreePath[] paths = myTreeView.getSelectionPaths();
        if (paths == null) return;
        final List<ConfigItem> configs = getSelectedConfigs(true);
        for (TreePath path : paths) {
            Object node = path.getLastPathComponent();
            if (node instanceof ConfigItemNode) {
                final ConfigItem item = ((ConfigItemNode)node).getConfigItem();
                if (!item.allowedToRemove()) {
                    TreeUtil.unselectPath(myTreeView, path);
                    configs.remove(item);
                }
            }
        }
        if (configs.isEmpty()) return;
        TreeUtil.removeSelected(myTreeView);
        for (ConfigItem config : configs) {
            config.removed(project);
        }
    }

    private static class TreeNodeComparator implements Comparator<TreeNode> {
        public int compare(final TreeNode o1, final TreeNode o2) {
            if (o1 instanceof ConfigItemNode && o2 instanceof ConfigItemNode) {
                //noinspection unchecked
                ConfigItem c1 = ((ConfigItemNode)o1).getConfigItem();
                //noinspection unchecked
                ConfigItem c2 = ((ConfigItemNode)o2).getConfigItem();
                boolean default1 = c1.isDefaultConfig();
                boolean default2 = c2.isDefaultConfig();
                if (default1 && !default2) return -1;
                if (!default1 && default2) return 1;
                return c1.compareTo(c2);
            }
            if (o1 instanceof ConfigsGroupNode && o2 instanceof ConfigsGroupNode) {
                final ConfigsGroupNode group1 = (ConfigsGroupNode)o1;
                final ConfigsGroupNode group2 = (ConfigsGroupNode)o2;
                if (group1.getLevel() != group2.getLevel()) {
                    return group1.getLevel() - group2.getLevel();
                }
                return group1.getGroup().compareTo(group2.getGroup());
            }
            return o1 instanceof ConfigsGroupNode ? -1 : 1;
        }
    }
}
