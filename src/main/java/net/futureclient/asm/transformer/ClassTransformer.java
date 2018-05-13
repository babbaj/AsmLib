package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ClassTransformer implements Comparable<ClassTransformer> {

    private List<MethodTransformer> methodTransformers = new ArrayList<>();
    private List<FieldTransformer> fieldTransformers = new ArrayList<>();

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

    public ClassTransformer(final Class<?> klazz, final boolean required, final int priority) {
        this(klazz.getName(), required, priority);
    }

    public ClassTransformer(final Class<?> klazz, final boolean required) {
        this(klazz.getName(), required);
    }

    public ClassTransformer(final Class<?> klazz) {
        this(klazz.getName());
    }

    public void inject(ClassNode classNode) {}

    public final void transform(ClassNode classNode) {
        getFieldTransformers().forEach(fieldTransformer ->
                classNode.fields.stream()
                        .filter(fieldNode -> fieldNode.name.equals(fieldTransformer.getFieldName()))
                        .findFirst()
                        .ifPresent(fieldTransformer::inject));

        getMethodTransformers().forEach(methodTransformer ->
                classNode.methods.stream()
                        .filter(methodNode -> methodNode.name.equals(methodTransformer.getMethodName()))
                        .findFirst()
                        .ifPresent(methodTransformer::inject));

        this.inject(classNode);
    }

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

    @Override
    public int compareTo(ClassTransformer ct) {
        if (ct == null) return -1;

        return Integer.compare(getPriority(), ct.getPriority());
    }
}
