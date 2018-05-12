package net.futureclient.asm.transformer;

import net.futureclient.asm.AsmLib;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
        this.addDeclaredTransformers();
    }

    private void addDeclaredTransformers() {
        Stream.of(getClass().getDeclaredClasses())
                .filter(this::isValidTransformerClass)
                .map(clazz -> {
                    try {
                        return clazz.newInstance();
                    } catch (Exception e) {
                        AsmLib.LOGGER.error(e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(instance -> {
                    if (instance instanceof MethodTransformer)
                        methodTransformers.add((MethodTransformer)instance);
                    if (instance instanceof FieldTransformer)
                        fieldTransformers.add((FieldTransformer)instance);
                });
    }
    private boolean isValidTransformerClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(RegisterTransformer.class) &&
                (MethodTransformer.class.isInstance(clazz) || FieldTransformer.class.isInstance(clazz)) &&
                clazz.getDeclaredMethods()[0].getParameterCount() == 0;
    }

    public ClassTransformer(final String className, final boolean required) {
        this(className, required, 1000);
    }

    public ClassTransformer(final String className) {
        this(className, false, 1000);
    }

    protected void inject(ClassNode classNode) {/* override to transform ClassNode*/}

    //TODO: handle in the LaunchWrapperTransformer
    public final void transform(ClassNode classNode) {
        this.inject(classNode);
        this.fieldTransformers.forEach(fieldTransformer ->
                classNode.fields.stream()
                        .filter(fieldNode -> fieldNode.name.equals(fieldTransformer.getFieldName()))
                        .findFirst()
                        .ifPresent(fieldTransformer::inject));

        this.methodTransformers.forEach(methodTransformer ->
                classNode.methods.stream()
                        .filter(methodNode -> methodNode.name.equals(methodTransformer.getMethodName()))
                        .findFirst()
                        .ifPresent(methodTransformer::inject));
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
