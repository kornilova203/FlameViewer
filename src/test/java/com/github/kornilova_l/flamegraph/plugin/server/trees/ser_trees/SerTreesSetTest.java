package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.SimpleTree;
import com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TwoThreads;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.generateSerFile;
import static org.junit.Assert.*;

public class SerTreesSetTest {
    private Filter filter0 = new Filter("*", null);
    private Filter filter1 = new Filter(null, "*");
    private Filter filter2 = new Filter("*run", null);
    private Filter filter3 = new Filter("*fun*", null);
    private Filter filter4 = new Filter("*fun4", null);
    private Filter filter5 = new Filter("*fun5", null);

    /**
     * Generates ser file for given Runnable using Logger.
     * Runnable should have code that would be inserted by Java agent.
     *
     * It is possible to run it before executing test but generation must be done in
     * separate process.
     */
    public static void main(String[] args) {
        generateSerFile(new TwoThreads(), TwoThreads.fileName);
    }

    @Test
    public void filterCallTree() {
        testCallTreeFilter(SimpleTree.fileName, filter0, 0);
        testCallTreeFilter(SimpleTree.fileName, filter1, 1);
        testCallTreeFilter(SimpleTree.fileName, filter2, 2);
        testCallTreeFilter(SimpleTree.fileName, filter3, 3);
        testCallTreeFilter(SimpleTree.fileName, filter5, 4);

        testCallTreeFilter(TwoThreads.fileName, filter0, 0);
        testCallTreeFilter(TwoThreads.fileName, filter1, 1);
        testCallTreeFilter(TwoThreads.fileName, filter2, 2);
        testCallTreeFilter(TwoThreads.fileName, filter3, 3);
        testCallTreeFilter(TwoThreads.fileName, filter4, 4);
    }

    @Test
    public void filterCallTraces() {
        testCallTracesFilter(TwoThreads.fileName, filter0, 0);
        testCallTracesFilter(TwoThreads.fileName, filter1, 1);
        testCallTracesFilter(TwoThreads.fileName, filter2, 2);
        testCallTracesFilter(TwoThreads.fileName, filter3, 3);
        testCallTracesFilter(TwoThreads.fileName, filter4, 4);
    }

    @Test
    public void filterBackTraces() {
        testBackTracesFilter(TwoThreads.fileName, filter0, 0);
        testBackTracesFilter(TwoThreads.fileName, filter1, 1);
        testBackTracesFilter(TwoThreads.fileName, filter2, 2);
        testBackTracesFilter(TwoThreads.fileName, filter3, 3);
        testBackTracesFilter(TwoThreads.fileName, filter4, 4);
    }

    private void testCallTreeFilter(String fileName, Filter filter, int filterId) {
        Trees trees = getCallTree(fileName, filter);
        String res = trees == null ? "" : trees.toString();
        TestHelper.compare(res,
                new File("src/test/resources/expected/filtered-call-tree/" + fileName +
                        "" + filterId + ".txt"));
    }

    private void testCallTracesFilter(String fileName, Filter filter, int filterId) {
        Tree tree = getTree(fileName, filter, TreeType.CALL_TRACES);
        String res = tree == null ? "" : tree.toString();
        TestHelper.compare(res,
                new File("src/test/resources/expected/filtered_outgoing_calls/" + fileName +
                        "" + filterId + ".txt"));
    }

    private void testBackTracesFilter(String fileName, Filter filter, int filterId) {
        Tree tree = getTree(fileName, filter, TreeType.BACK_TRACES);
        String res = tree == null ? "" : tree.toString();
        TestHelper.compare(res,
                new File("src/test/resources/expected/filtered_incoming_calls/" + fileName +
                        "" + filterId + ".txt"));
    }

    @Test
    public void getCallTreeTest() {
        callTreeTest(SimpleTree.fileName);
        callTreeTest(TwoThreads.fileName);
    }

    private void callTreeTest(String fileName) {
        Trees callTree = getCallTree(fileName, null);
        assertNotNull(callTree);

        StringBuilder actual = new StringBuilder();
        for (Tree tree : callTree.getTreesList()) {
            actual.append(tree.toString());
        }
        TestHelper.compare(actual.toString(),
                new File("src/test/resources/expected/" + fileName + ".txt"));
    }

    private Trees getCallTree(String fileName, @Nullable Filter filter) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/generated/" + fileName + ".ser"));
        return treesSet.getCallTree(filter);
    }

    private Tree getTree(String fileName, @Nullable Filter filter, TreeType treeType) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/generated/" + fileName + ".ser"));
        return treesSet.getTree(treeType, filter);
    }

    @Test
    public void getTreesPreview() {
        testTreesPreview(SimpleTree.fileName);
        testTreesPreview(TwoThreads.fileName);
    }

    private void testTreesPreview(String fileName) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/generated/" + fileName + ".ser"));
        TreesPreview treesPreview = treesSet.getTreesPreview(null);
        assertNotNull(treesPreview);
        TestHelper.compare(treesPreview.toString(),
                new File("src/test/resources/expected/" + fileName + "-treesPreview.txt"));
    }

    @Test
    public void setTreeWidthTest() {
        Tree.Builder tree = Tree.newBuilder();
        tree.setBaseNode(Tree.Node.newBuilder());
        Tree.Node.Builder baseNode = tree.getBaseNodeBuilder();

        Tree.Node.Builder node = Tree.Node.newBuilder()
                .setOffset(10)
                .setWidth(20);
        baseNode.addNodes(node);

        TreesUtil.INSTANCE.setTreeWidth(tree);
        TreesUtil.INSTANCE.setNodesCount(tree);

        assertEquals(30, tree.getWidth());

        /* Empty tree */
        tree = Tree.newBuilder();
        tree.setBaseNode(Tree.Node.newBuilder());

        TreesUtil.INSTANCE.setTreeWidth(tree);
        TreesUtil.INSTANCE.setNodesCount(tree);

        assertEquals(0, tree.getWidth());
    }
}