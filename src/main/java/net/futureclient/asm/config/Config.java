package net.futureclient.asm.config;

import net.futureclient.asm.transformer.ClassTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Config implements Comparable<Config> {

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

    protected void addClassTransformers(ClassTransformer... classTransformers) {
        this.classTransformers.addAll(Arrays.asList(classTransformers));
    }

    public List<ClassTransformer> getClassTransformers() {
        return classTransformers;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(Config cf) {
        if (cf == null) return -1;

        return Integer.compare(this.getPriority(), cf.getPriority());
    }
}
