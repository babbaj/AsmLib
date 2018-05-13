package net.futureclient.asm.config;

import net.futureclient.asm.transformer.ClassTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config implements Comparable<Config> {

    private final List<ClassTransformer> classTransformers = new ArrayList<>();

    private final String name;
    private final int priority;

    public Config(final String name, final int priority) {
        this.name = name;
        this.priority = priority;
    }

    public Config(final String name) {
        this(name, 1000);
    }

    protected final void addClassTransformers(ClassTransformer... classTransformers) {
        this.classTransformers.addAll(Arrays.asList(classTransformers));
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
    public final int compareTo(Config cf) {
        if (cf == null) return -1;

        return Integer.compare(this.getPriority(), cf.getPriority());
    }
}
