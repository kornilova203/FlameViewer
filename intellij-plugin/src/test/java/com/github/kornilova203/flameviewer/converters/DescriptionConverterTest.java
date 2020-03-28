package com.github.kornilova203.flameviewer.converters;

import org.junit.Test;

import static com.github.kornilova203.flameviewer.converters.trees.DescriptionConverter.getBeautifulDesc;
import static org.junit.Assert.assertEquals;

public class DescriptionConverterTest {

    @Test
    public void getBeautifulDescTest() {
        assertEquals("(boolean)void", getBeautifulDesc("(Z)V"));
        assertEquals("(boolean[][])void", getBeautifulDesc("([[Z)V"));
        assertEquals("(String, byte, Object)byte[]", getBeautifulDesc("(Ljava/lang/String;BLjava/lang/Object;)[B"));
        assertEquals("(String[][], byte, Object)byte[]", getBeautifulDesc("([[Ljava/lang/String;BLjava/lang/Object;)[B"));
        assertEquals("()byte[]", getBeautifulDesc("()[B"));
    }
}