package profiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ProfilingClassVisitor extends ClassVisitor {
    ProfilingClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null && !name.equals("toString") && !name.equals("main")) {
            return new ProfilingMethodVisitor(access, name, desc, mv);
        }
        return mv;
    }
}
