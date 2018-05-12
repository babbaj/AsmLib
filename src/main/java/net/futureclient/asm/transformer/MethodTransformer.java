package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.MethodNode;

public abstract class MethodTransformer {

    private final String methodName;
    private final ClassTransformer classTransformer;

    public MethodTransformer(final String methodName, final ClassTransformer classTransformer) {
        this.methodName = methodName;
        this.classTransformer = classTransformer;
    }

    public abstract void inject(MethodNode methodNode);

    public String getMethodName() {
        return this.methodName;
    }

    public ClassTransformer getClassTransformer() {
        return this.classTransformer;
    }
}
