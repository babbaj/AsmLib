package net.futureclient.asm.config;

import net.futureclient.asm.transformer.ClassTransformer;
import net.futureclient.asm.transformer.util.TransformerGenerator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class Config implements Comparable<Config> {

    private final List<ClassTransformer> classTransformers = new ArrayList<>();
    private final Set<String> transformerClassNames = new HashSet<>();

    private final String name;
    private final int priority;

    public Config(final String name, final int priority) {
        this.name = name;
        this.priority = priority;
    }

    public Config(final String name) {
        this(name, 1000);
    }

    //@Deprecated
    protected final void addClassTransformers(ClassTransformer... classTransformers) {
        this.classTransformers.addAll(Arrays.asList(classTransformers));
    }

    @Deprecated
    private void addClassTransformers(Class<?>... classes) {
        Stream.of(classes)
                .map(TransformerGenerator::fromClass)
                .filter(Objects::nonNull)
                .forEach(classTransformers::add);
    }

    // transformer preprocessor needs to know about these classes before they are loaded
    protected final void addClassTransformer(String className) {
        this.transformerClassNames.add(className);
    }

    public final Set<String> getTransformerClassNames() {
        return this.transformerClassNames;
    }

    public final List<ClassTransformer> getClassTransformers() {
        return this.classTransformers;
    }

    public final String getName() {
        return this.name;
    }

    public final int getPriority() {
        return this.priority;
    }

    @Override
    public final int compareTo(@Nonnull Config cf) {
        return Integer.compare(this.getPriority(), cf.getPriority());
    }
}
