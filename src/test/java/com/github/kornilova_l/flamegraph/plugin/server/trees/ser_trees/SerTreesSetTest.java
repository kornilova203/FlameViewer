package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class SerTreesSetTest {
    @Test
    public void getTree() throws Exception {
        assertTrue(true);
    }

    @Test
    public void getCallTree() throws Exception {
        SerTreesSet treesSet = new SerTreesSet(
                new File("src/test/resources/SerTreesSetTest/test_data01.ser"));
        Configuration configuration = new Configuration();
        configuration.addMethodConfig("*.fun1(*)", false);
        TreesProtos.Trees callTree = treesSet.getCallTree(configuration);
        assertTrue(callTree != null);
        TestHelper.compare(callTree.getTrees(0), new File("src/test/resources/SerTreesSetTest/result01.txt"));

        configuration.addMethodConfig("*.fun2(*)", false);
        callTree = treesSet.getCallTree(configuration);
        assertTrue(callTree != null);
        TestHelper.compare(callTree.getTrees(0),
                new File("src/test/resources/SerTreesSetTest/result02.txt"));

        // remove first node from call tree
        Configuration configuration2 = new Configuration();
        configuration2.addMethodConfig("*.fun3(*)", false);
        configuration2.addMethodConfig("*.fun2(*)", false);
        callTree = treesSet.getCallTree(configuration2);
        assertTrue(callTree != null);
        TestHelper.compare(callTree.getTrees(0),
                new File("src/test/resources/SerTreesSetTest/result03.txt"));

        // remove parent of children
        SerTreesSet treesSet2 = new SerTreesSet(
                new File("src/test/resources/SerTreesSetTest/test_data04.ser"));
        callTree = treesSet2.getCallTree(configuration2);
        assertTrue(callTree != null);
        TestHelper.compare(callTree.getTrees(0),
                new File("src/test/resources/SerTreesSetTest/result04.txt"));

        // check incoming calls
        TestHelper.compare(treesSet2.getTree(TreeManager.TreeType.INCOMING_CALLS, configuration2),
                new File("src/test/resources/SerTreesSetTest/result05.txt"));

        // remove parent of twins
        SerTreesSet treesSet3 = new SerTreesSet(
                new File("src/test/resources/SerTreesSetTest/test_data06.ser"));
        callTree = treesSet3.getCallTree(configuration2);
        assertTrue(callTree != null);
        TestHelper.compare(callTree.getTrees(0),
                new File("src/test/resources/SerTreesSetTest/result06.txt"));

        // check outgoing calls
        TestHelper.compare(treesSet3.getTree(TreeManager.TreeType.OUTGOING_CALLS, configuration2),
                new File("src/test/resources/SerTreesSetTest/result07.txt"));

        // remove parent will have twins
        SerTreesSet treesSet4 = new SerTreesSet(
                new File("src/test/resources/SerTreesSetTest/test_data08.ser"));
        callTree = treesSet4.getCallTree(configuration2);
        assertTrue(callTree != null);
        TestHelper.compare(callTree.getTrees(0),
                new File("src/test/resources/SerTreesSetTest/result08.txt"));

        TestHelper.compare(treesSet4.getTree(TreeManager.TreeType.OUTGOING_CALLS, configuration2),
                new File("src/test/resources/SerTreesSetTest/result09.txt"));
    }
}