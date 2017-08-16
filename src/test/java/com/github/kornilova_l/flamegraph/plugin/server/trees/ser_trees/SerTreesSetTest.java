package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper;
import com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.SimpleTree;
import com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TwoThreads;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import org.junit.Test;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.generateSerFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SerTreesSetTest {

    public static void main(String[] args) {
//        generateSerFile(new SimpleTree(), SimpleTree.fileName);
        generateSerFile(new TwoThreads(), TwoThreads.fileName);
    }

    @Test
    public void filterCallTree() throws Exception {
//        SerTreesSet treesSet = new SerTreesSet(
//                new File("src/callTreeTest/resources/SerTreesSetTest/test_data01.ser"));
//        Configuration configuration = new Configuration();
//        configuration.addMethodConfig("*.fun1(*)", false);
//        TreesProtos.Trees callTree = treesSet.getCallTree(configuration);
//        assertTrue(callTree != null);
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(callTree.getTrees(0), new File("src/callTreeTest/resources/SerTreesSetTest/result01.txt"));
//
//        configuration.addMethodConfig("*.fun2(*)", false);
//        callTree = treesSet.getCallTree(configuration);
//        assertTrue(callTree != null);
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(callTree.getTrees(0),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result02.txt"));
//
//        // remove first node from call tree
//        Configuration configuration2 = new Configuration();
//        configuration2.addMethodConfig("*.fun3(*)", false);
//        configuration2.addMethodConfig("*.fun2(*)", false);
//        callTree = treesSet.getCallTree(configuration2);
//        assertTrue(callTree != null);
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(callTree.getTrees(0),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result03.txt"));
//
//        // remove parent of children
//        SerTreesSet treesSet2 = new SerTreesSet(
//                new File("src/callTreeTest/resources/SerTreesSetTest/test_data04.ser"));
//        callTree = treesSet2.getCallTree(configuration2);
//        assertTrue(callTree != null);
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(callTree.getTrees(0),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result04.txt"));
//
//        // check incoming calls
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(treesSet2.getTree(TreeManager.TreeType.INCOMING_CALLS, configuration2),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result05.txt"));
//
//        // remove parent of twins
//        SerTreesSet treesSet3 = new SerTreesSet(
//                new File("src/callTreeTest/resources/SerTreesSetTest/test_data06.ser"));
//        callTree = treesSet3.getCallTree(configuration2);
//        assertTrue(callTree != null);
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(callTree.getTrees(0),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result06.txt"));
//
//        // check outgoing calls
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(treesSet3.getTree(TreeManager.TreeType.OUTGOING_CALLS, configuration2),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result07.txt"));
//
//        // remove parent will have twins
//        SerTreesSet treesSet4 = new SerTreesSet(
//                new File("src/callTreeTest/resources/SerTreesSetTest/test_data08.ser"));
//        callTree = treesSet4.getCallTree(configuration2);
//        assertTrue(callTree != null);
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(callTree.getTrees(0),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result08.txt"));
//
//        com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper.compare(treesSet4.getTree(TreeManager.TreeType.OUTGOING_CALLS, configuration2),
//                new File("src/callTreeTest/resources/SerTreesSetTest/result09.txt"));
    }

    @Test
    public void getCallTreeTest() throws Exception {
        callTreeTest(SimpleTree.fileName);
        callTreeTest(TwoThreads.fileName);
    }

    private void callTreeTest(String fileName) {
        Trees callTree = getCallTree(fileName);
        assertTrue(callTree != null);

        StringBuilder actual = new StringBuilder();
        for (TreeProtos.Tree tree : callTree.getTreesList()) {
            actual.append(tree.toString());
        }
        TestHelper.compare(actual.toString(),
                new File("src/test/resources/expected/" + fileName + ".txt"));
    }

    private Trees getCallTree(String fileName) {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/out/" + fileName + ".ser"));
        return treesSet.getCallTree(null);
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