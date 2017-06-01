package Profiler;

import java.lang.instrument.Instrumentation;

public class ProfilingAgent {
    public static void premain(String args, Instrumentation inst) {
//        inst.addTransformer(new CallSpy());
        System.out.println("hello premain");
    }
}
//
//class ProfilingClassAdapter extends ClassVisitor {
//    ProfilingClassAdapter(ClassVisitor cv) {
//        super(Opcodes.ASM5, cv);
//    }
//
//    @Override
//    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//        if (name.equals("lambda$main$0")) {
//            System.out.println("found lambda$main$0");
//        }
//        return new MethodVisitor(Opcodes.ASM5) {
//            @Override
//            public void visitTypeInsn(int opcode, String name) {
//                if (opcode == Opcodes.NEW && name.equals("org/jetbrains/test/DummyApplication")) {
//                    System.out.println("found new Dummy Application");
//                }
//                super.visitTypeInsn(opcode, name);
//            }
//        };
////        return super.visitMethod(i, s, s1, s2, strings);
//    }
//}
