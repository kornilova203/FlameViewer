package com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class StacksOCTreeBuilderTest {

    @Before
    public void setUp() {

    }

    @Test
    public void getTree() {
        doTest(new File("src/test/resources/StacksOCTreeBuilderTest/test_data01.txt"), new File("src/test/resources/StacksOCTreeBuilderTest/result01.txt"));

    }

    private void doTest(File data, File res) {
        Map<String, Integer> stacks;
        stacks = getStacksFromFile(data);
        TreeProtos.Tree tree = new StacksOCTreeBuilder(stacks).getTree();
        assertNotNull(tree);
        TestHelper.compare(tree.toString(), res);
    }

    private Map<String, Integer> getStacksFromFile(File data) {
        Map<String, Integer> stacks = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(data))) {
            reader.lines()
                    .filter(line -> line.length() != 0)
                    .forEach(line -> {
                        String stack = line.substring(0, line.lastIndexOf(' '));
                        int count = Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1, line.length()));
                        stacks.put(stack, count);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stacks;
    }

}