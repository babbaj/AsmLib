package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;

public abstract class MethodTransformer {

    private final String methodName;
    private final String methodDesc;

    @Nullable private final String description;

    public MethodTransformer(final String methodName, final String methodDescriptor, @Nullable String description) {
        this.methodName = methodName;
        this.methodDesc = methodDescriptor;
        this.description = description;
    }

    public abstract void inject(MethodNode methodNode, ClassNode clazz);

    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }

    public String getDescription() {
        return this.description;
    }

}
