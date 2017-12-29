package com.github.kornilova_l.flamegraph.plugin.server.trees;

import org.jetbrains.annotations.NotNull;

import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.parseToken;

public class DescriptionConverter {
    /**
     * @param desc file specific description
     * @return (Ljava...lang...String...Z)V -> (String, boolean)void
     */
    @NotNull
    public static String getBeautifulDesc(String desc) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('(');
        int closeBracketIndex = desc.indexOf(')');
        int index = 1;
        while (index != closeBracketIndex) {
            index = parseToken(stringBuilder, desc, index);
            stringBuilder.append(", ");
        }
        if (index != 1) {
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }
        stringBuilder.append(')');
        parseToken(stringBuilder, desc, closeBracketIndex + 1);
        return stringBuilder.toString();
    }
}
