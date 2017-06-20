package com.github.kornilova_l.profiler.agent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.regex.Pattern;

class AddPrintNameMethodVisitor extends AdviceAdapter {
    private final String methodName;
    private final String className;
    private final static Pattern allParamsPattern = Pattern.compile("(\\(.*\\))");
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    private final static Pattern returnTypePattern = Pattern.compile("(?<=\\)).*"); // (?<=\)).*
    private final static String LOGGER_PACKAGE_NAME = "com/github/kornilova_l/profiler/logger/";

    AddPrintNameMethodVisitor(int access, String methodName, String desc,
                              MethodVisitor mv, String className) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    protected void onMethodEnter() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;");
        mv.visitLdcInsn(className + "." + methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V", false);
    }
}
