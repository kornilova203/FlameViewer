package com.github.kornilova203.flameviewer.flight_parser;

import org.openjdk.jmc.common.IMCFrame;
import org.openjdk.jmc.common.IMCStackTrace;
import org.openjdk.jmc.common.item.IItem;
import org.openjdk.jmc.common.item.IMemberAccessor;
import org.openjdk.jmc.common.util.FormatToolkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is located in flightrecorder's
 * parser package to have access to some classes and fields
 */
public class JfrFrameAccessor {
    /**
     * A3_2 object allows us to assess stacks (field name value 2) in Item5 objects
     * (Item5 class extends Item3 class)
     */
    private IMemberAccessor<Object, IItem> A3_2;

    public JfrFrameAccessor() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> itemBuilderClass = Class.forName("org.openjdk.jmc.flightrecorder.internal.parser.ItemBuilder");
        Field f = itemBuilderClass.getDeclaredField("A3_2");
        f.setAccessible(true);
        //noinspection unchecked
        A3_2 = (IMemberAccessor<Object, IItem>) f.get(null);
    }

    public String getStack(IItem iItem) {
        IMCStackTrace member = (IMCStackTrace) A3_2.getMember(iItem);
        List<? extends IMCFrame> frames = member.getFrames();
        List<String> methodCalls = new ArrayList<>(20);
        for (int i = frames.size() - 1; i >= 0; i--) {
            IMCFrame frame = frames.get(i);
            methodCalls.add(FormatToolkit.getHumanReadable(frame.getMethod()));
        }
        return String.join(";", methodCalls);
    }
}
