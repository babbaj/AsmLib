package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class MethodTransformer {

    private final String methodName;
    private final String methodDesc; // TODO: auto generate

    public MethodTransformer(final String methodName, final String methodDescriptor) {
        this.methodName = methodName;
        this.methodDesc = methodDescriptor;
    }

    public abstract void inject(MethodNode methodNode, ClassNode clazz);

    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }

}
