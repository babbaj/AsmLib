package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ClassTransformer {

    private List<FieldTransformer> fieldTransformers = new ArrayList<>();
    private List<MethodTransformer> methodTransformers = new ArrayList<>();

    private final String className;
    private final boolean required;
    private final int priority;

    public ClassTransformer(final String className, final boolean required, final int priority) {
        this.className = className;
        this.required = required;
        this.priority = priority;
    }

    public ClassTransformer(final String className, final boolean required) {
        this(className, required, 1000);
    }

    public ClassTransformer(final String className) {
        this(className, false, 1000);
    }

    public void inject(ClassNode classNode) {}

    //TODO: handle in the LaunchWrapperTransformer
    /*public void inject(ClassNode classNode) {
        this.fieldTransformers.forEach(fieldTransformer -> classNode.fields.stream().filter(fieldNode -> fieldNode.name.equals(fieldTransformer.getFieldName())).findFirst().ifPresent(fieldTransformer::inject));
        this.methodTransformers.forEach(methodTransformer -> classNode.methods.stream().filter(methodNode -> methodNode.name.equals(methodTransformer.getMethodName())).findFirst().ifPresent(methodTransformer::inject));
    }*/

    protected void addFieldTransformers(FieldTransformer... fieldTransformers) {
        this.fieldTransformers.addAll(Arrays.asList(fieldTransformers));
    }

    protected void addMethodTransformers(MethodTransformer... methodTransformers) {
        this.methodTransformers.addAll(Arrays.asList(methodTransformers));
    }

    public List<FieldTransformer> getFieldTransformers() {
        return this.fieldTransformers;
    }

    public List<MethodTransformer> getMethodTransformers() {
        return this.methodTransformers;
    }

    public String getClassName() {
        return this.className;
    }

    public boolean isRequired() {
        return this.required;
    }

    public int getPriority() {
        return priority;
    }
}
