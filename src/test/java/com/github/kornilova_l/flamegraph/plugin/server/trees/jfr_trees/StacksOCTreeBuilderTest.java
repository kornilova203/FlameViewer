package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StacksOCTreeBuilderTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getTree() throws Exception {
        doTest(new File("src/test/resources/test_data01.txt"), new File("src/test/resources/result01.txt"));

    }

    private void doTest(File data, File res) {
        Map<String, Integer> stacks;
        stacks = getStacksFromFile(data);
        TreeProtos.Tree tree = new StacksOCTreeBuilder(stacks).getTree();
        assertNotNull(tree);
        compare(tree, res);
    }

    private void compare(TreeProtos.Tree tree, File res) {
        try (FileInputStream inputStream = new FileInputStream(res)) {
            byte[] data = new byte[(int) res.length()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(data);
            String result = new String(data, "UTF-8");
            assertEquals(result,
                    tree.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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