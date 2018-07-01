package net.futureclient.asm.transformer;

import net.futureclient.asm.internal.AsmLib;
import net.futureclient.asm.transformer.util.ObfUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

public abstract class ClassTransformer {

    private final List<MethodTransformer> methodTransformers = new ArrayList<>();

    private final String targetClassName;
    private final boolean remap;
    private final boolean required;

    public ClassTransformer(final String className, final boolean required, final boolean remap) {
        this.targetClassName = className;
        this.remap = remap;
        this.required = required;
    }

    public ClassTransformer(final String className, final boolean required) {
        this(className, required, true);
    }

    public ClassTransformer(final String className) {
        this(className, false, true);
    }

    protected void inject(ClassNode classNode) {}

    public final void transform(ClassNode classNode) {
        getMethodTransformers().forEach(methodTransformer ->
                classNode.methods.stream()
                        .filter(node -> areMethodsEqual(methodTransformer, node))
                        .findFirst()
                        .ifPresent(method -> {
                            try {
                                methodTransformer.inject(method, classNode);
                                AsmLib.LOGGER.info("Transformed method: {}::{}{}{}",
                                        targetClassName,
                                        methodTransformer.getMethodName(),
                                        methodTransformer.getMethodDesc(),
                                        Optional.ofNullable(methodTransformer.getDescription())
                                                .map(str -> " - " + "\"" + str + "\"").orElse(""));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }));

        this.inject(classNode);
    }

    // checks if remapped method name/desc matches MethodNode
    private boolean areMethodsEqual(MethodTransformer transformer, MethodNode methodNode) {
        if (remap) {
                    // check name
            return Objects.equals(transformer.getRuntimeMethodName(targetClassName), methodNode.name)
                    // check descriptor
                && Objects.equals(transformer.getRuntimeMethodDesc(), methodNode.desc);
        } else {
            return methodNode.name.equals(transformer.getMethodName()) &&
                   methodNode.desc.equals(transformer.getMethodDesc());
        }
    }


    protected void addMethodTransformers(MethodTransformer... methodTransformers) {
        this.methodTransformers.addAll(Arrays.asList(methodTransformers));
    }

    public List<MethodTransformer> getMethodTransformers() {
        return this.methodTransformers;
    }

    public String getTargetClassName() {
        return this.targetClassName;
    }

    public boolean isRequired() {
        return this.required;
    }

}
