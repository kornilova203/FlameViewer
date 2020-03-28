package com.github.kornilova203.flameviewer.converters.trees;

import org.jetbrains.annotations.NotNull;

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

    private static int parseToken(StringBuilder output, String desc, int startIndex) {
        if (desc.charAt(startIndex) == '[') {
            int endPos = parseToken(output, desc, startIndex + 1);
            output.append("[]");
            return endPos;
        } else {
            char c = desc.charAt(startIndex);
            if (c == 'L') {
                int slashPos = -1;
                int endPos = -1;
                for (int i = startIndex + 1; i < desc.length(); i++) {
                    char maybeSlash = desc.charAt(i);
                    if (maybeSlash == '/') {
                        slashPos = i;
                    } else if (maybeSlash == ';') {
                        endPos = i + 1;
                        break;
                    }
                }
                if (slashPos == -1) { // if does not contain package
                    output.append(desc, 1, endPos - 1);
                } else {
                    output.append(desc, slashPos + 1, endPos - 1);

                }
                return endPos;
            } else {
                output.append(jvmTypeToParam(c));
                return startIndex + 1;
            }
        }
    }

    @NotNull
    private static String jvmTypeToParam(char primitiveTypeDesc) {
        switch (primitiveTypeDesc) {
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'Z':
                return "boolean";
            case 'C':
                return "char";
            case 'S':
                return "short";
            case 'B':
                return "byte";
            case 'F':
                return "float";
            case 'D':
                return "double";
            case 'V':
                return "void";
            default:
                throw new IllegalArgumentException("Not known primitive type: " + primitiveTypeDesc);
        }
    }
}
