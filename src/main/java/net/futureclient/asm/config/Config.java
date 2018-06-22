package net.futureclient.asm.config;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.futureclient.asm.transformer.ClassTransformer;
import net.futureclient.asm.transformer.util.TransformerGenerator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config implements Comparable<Config> {

    private final List<ClassTransformer> classTransformers = new ArrayList<>();
    private final Set<String> transformerClassNames = new HashSet<>();

    private final String name;
    private final int priority;
    private final boolean required;

    public Config(final String name, final int priority, boolean required) {
        this.name = name;
        this.priority = priority;
        this.required = required;
    }

    public Config(final String name, final int priority) {
        this(name, priority, false);
    }

    public Config(final String name) {
        this(name, 1000, false);
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


    // TODO: check for elements that don't do anything
    public static Config fromJson(JsonObject root) {
        final String name = Optional.ofNullable(root.getAsJsonPrimitive("name")).map(JsonElement::getAsString)
                .orElseThrow(() -> new IllegalArgumentException("Config name is required"));
        final boolean required = Optional.ofNullable(root.getAsJsonPrimitive("required")).map(JsonPrimitive::getAsBoolean)
                .orElse(false);
        final int priority = Optional.ofNullable(root.getAsJsonPrimitive("priority")).map(JsonElement::getAsInt)
                .orElse(1000);

        JsonObject transformers = root.getAsJsonObject("transformers");
        final List<String> fullClassNames = transformers.entrySet()
                .stream()
                .collect(ArrayList::new,
                        (list, entry) -> {
                            final String fullPackage = entry.getKey();
                            Streams.stream(entry.getValue().getAsJsonArray().iterator())
                                    .map(JsonElement::getAsString)
                                    .map(clazz -> (fullPackage + '.' + clazz).replaceAll("\\.{2,}", "."))
                                    .forEach(list::add);
                        },
                        List::addAll
                );

        final Config out = new Config(name, priority, required);
        out.getTransformerClassNames().addAll(fullClassNames);
        return out;
    }
}
