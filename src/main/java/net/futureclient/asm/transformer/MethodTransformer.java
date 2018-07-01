package net.futureclient.asm.transformer;

import net.futureclient.asm.transformer.util.ObfUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;

public abstract class MethodTransformer {

    private final String methodName;
    private final String methodDesc;

    // cache lookup results to avoid log spam
    @Nullable private String runtimeMethodName;
    @Nullable private String runtimeMethodDesc;

    @Nullable private final String description;

    public MethodTransformer(final String methodName, final String methodDescriptor, @Nullable String description) {
        this.methodName = methodName;
        this.methodDesc = methodDescriptor;
        this.description = description;
    }

    public abstract void inject(MethodNode methodNode, ClassNode clazz);

    public String getDescription() {
        return this.description;
    }


    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }


    public String getRuntimeMethodName(String parentClassName) {
        return runtimeMethodName != null ? runtimeMethodName
                : (runtimeMethodName = ObfUtils.remapMethodName(parentClassName.replace(".", "/"), getMethodName(), getMethodDesc()));
    }

    public String getRuntimeMethodDesc() {
        return runtimeMethodDesc != null ? runtimeMethodDesc
                : (runtimeMethodDesc = ObfUtils.remapMethodDesc(getMethodDesc()));
    }
}
