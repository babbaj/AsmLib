package net.futureclient.asm.transformer;

import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ClassTransformer implements Comparable<ClassTransformer> {

    private final List<MethodTransformer> methodTransformers = new ArrayList<>();
    private final List<FieldTransformer> fieldTransformers = new ArrayList<>();

    // TODO: support multiple target classes
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


    protected void inject(ClassNode classNode) {}

    public final void transform(ClassNode classNode) {
        getFieldTransformers().forEach(fieldTransformer ->
                classNode.fields.stream()
                        .filter(fieldNode -> fieldNode.name.equals(fieldTransformer.getFieldName()))
                        .findFirst()
                        .ifPresent(fieldTransformer::inject));

        getMethodTransformers().forEach(methodTransformer ->
                classNode.methods.stream()
                        .filter(methodNode ->
                            methodNode.name.equals(methodTransformer.getMethodName()) &&
                            methodNode.desc.equals(methodTransformer.getMethodDesc())
                        )
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
        return Integer.compare(getPriority(), ct.getPriority());
    }
}
