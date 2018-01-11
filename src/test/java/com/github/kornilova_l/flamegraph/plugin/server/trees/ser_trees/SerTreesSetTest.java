package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.SimpleTree;
import com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TwoThreads;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.generateSerFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SerTreesSetTest {
    private Filter filter0 = new Filter("*", null);
    private Filter filter1 = new Filter(null, "*");
    private Filter filter2 = new Filter("*run", null);
    private Filter filter3 = new Filter("*fun*", null);
    private Filter filter4 = new Filter("*fun4", null);
    private Filter filter5 = new Filter("*fun5", null);

    public static void main(String[] args) {
//        generateSerFile(new SimpleTree(), SimpleTree.fileName);
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
    public void filterOutgoingTree() {
        testOutgoingCallsFilter(TwoThreads.fileName, filter0, 0);
        testOutgoingCallsFilter(TwoThreads.fileName, filter1, 1);
        testOutgoingCallsFilter(TwoThreads.fileName, filter2, 2);
        testOutgoingCallsFilter(TwoThreads.fileName, filter3, 3);
        testOutgoingCallsFilter(TwoThreads.fileName, filter4, 4);
    }

    @Test
    public void filterIncomingTree() {
        testIncomingCallsFilter(TwoThreads.fileName, filter0, 0);
        testIncomingCallsFilter(TwoThreads.fileName, filter1, 1);
        testIncomingCallsFilter(TwoThreads.fileName, filter2, 2);
        testIncomingCallsFilter(TwoThreads.fileName, filter3, 3);
        testIncomingCallsFilter(TwoThreads.fileName, filter4, 4);
    }

    private void testCallTreeFilter(String fileName, Filter filter, int filterId) {
        Trees trees = getCallTree(fileName, filter);
        String res = trees == null ? "" : trees.toString();
        TestHelper.compare(res,
                new File("src/test/resources/expected/filtered-call-tree/" + fileName +
                        "" + filterId + ".txt"));
    }

    private void testOutgoingCallsFilter(String fileName, Filter filter, int filterId) {
        Tree tree = getTree(fileName, filter, TreeType.OUTGOING_CALLS);
        String res = tree == null ? "" : tree.toString();
        TestHelper.compare(res,
                new File("src/test/resources/expected/filtered_outgoing_calls/" + fileName +
                        "" + filterId + ".txt"));
    }

    private void testIncomingCallsFilter(String fileName, Filter filter, int filterId) {
        Tree tree = getTree(fileName, filter, TreeType.INCOMING_CALLS);
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
        assertTrue(callTree != null);

        StringBuilder actual = new StringBuilder();
        for (Tree tree : callTree.getTreesList()) {
            actual.append(tree.toString());
        }
        TestHelper.compare(actual.toString(),
                new File("src/test/resources/expected/" + fileName + ".txt"));
    }

    private Trees getCallTree(String fileName, @Nullable Filter filter) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/out/" + fileName + ".ser"));
        return treesSet.getCallTree(filter);
    }

    private Tree getTree(String fileName, @Nullable Filter filter, TreeType treeType) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/out/" + fileName + ".ser"));
        return treesSet.getTree(treeType, filter);
    }

    @Test
    public void getTreesPreview() {
        testTreesPreview(SimpleTree.fileName);
        testTreesPreview(TwoThreads.fileName);
    }

    private void testTreesPreview(String fileName) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/out/" + fileName + ".ser"));
        TreesPreview treesPreview = treesSet.getTreesPreview(null);
        assertNotNull(treesPreview);
        TestHelper.compare(treesPreview.toString(),
                new File("src/test/resources/expected/" + fileName + "-treesPreview.txt"));
    }
}