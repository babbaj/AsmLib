package net.futureclient.asm.transformer.util;

import com.google.common.collect.ImmutableList;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.internal.TransformerDelegate;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.ClassTransformer;
import net.futureclient.asm.transformer.MethodTransformer;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by Babbaj on 5/17/2018.
 */
// TODO: make this not bad
public final class TransformerGenerator {
    private TransformerGenerator() {}

    // group 1 = method name;
    // group 2 = args/return type
    private static final Pattern DESCRIPTOR_PATTERN = Pattern.compile("(.+)(\\(.*\\).+)");

    private static final ImmutableList<Class<? extends Annotation>> METHOD_ANNOTATION_CLASSES = ImmutableList.of(
            Inject.class
    );
    private static final ImmutableList<Class<?>[]> INJECT_PAREMETER_TYPES = ImmutableList.of(
            new Class<?>[]{MethodNode.class},
            new Class<?>[]{AsmMethod.class}
    );


    /*@Deprecated
    public static ClassTransformer fromClass(Class<? extends TransformerDelegate> clazz) {
        return fromClass(clazz, null);
    }*/

    // TODO: calling getMethods causes all method argument type classes to be loaded which causes unwanted class loading
    // TODO: maybe still allow normal classes
    public static ClassTransformer fromClass(TransformerDelegate delegateInstance, @Nullable Config config) {
        checkClass(delegateInstance.getClass());

        Transformer info = delegateInstance.getClass().getAnnotation(Transformer.class);
        try {

            String targetClass = info.target();
            ClassTransformer transformer = new ClassTransformer(targetClass, info.required(), info.remap()) {};

            Stream.of(delegateInstance.getClass().getDeclaredMethods())
                    .filter(m -> !isConstructor(m))
                    .filter(m -> isValidMethodTransformer(m))
                    .map(m -> createMethodTransformer(m, delegateInstance, config != null ? config : ConfigManager.INSTANCE.getDefaultConfig()))
                    .forEach(transformer.getMethodTransformers()::add);
            return transformer;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // checks if a class is a valid transformer class
    private static void checkClass(Class<?> clazz) {
        if (!TransformerDelegate.class.isAssignableFrom(clazz))
            throw new ClassCastException("Class is not assignable from TransformerDelegate");
        if (!clazz.isAnnotationPresent(Transformer.class))
            throw new IllegalArgumentException("Missing @Transformer annotation");
        /*if (!hasValidConstructor(clazz))
            throw new IllegalArgumentException("Invalid constructor, expected 0 args");*/
    }

    // create a method transformer that encapsulates a java.lang.reflect.Method
    public static MethodTransformer createMethodTransformer(Method method, Object instance, Config config) {
        Inject info = method.getAnnotation(Inject.class);
        final String[] parsed = parseTarget(info.target());
        final String name = parsed[0];
        final String desc = parsed[1];

        final String description = !info.description().isEmpty() ? info.description() : null;

        final Class<?>[] params = method.getParameterTypes();
        if (INJECT_PAREMETER_TYPES.stream().noneMatch(legalTypes -> Arrays.deepEquals(legalTypes, params))) {
            throw new IllegalArgumentException("Invalid arguments for @Inject: expected MethodNode or AsmMethod");
        }

        return new MethodTransformer(name, desc, description) { // maybe don't use anonymous class?
            @Override
            public void inject(MethodNode methodNode, ClassNode clazz) {
                try {
                    if (params[0] == AsmMethod.class)
                        method.invoke(instance, new AsmMethod(methodNode, clazz, config));
                    else if (params[0] == MethodNode.class)
                        method.invoke(instance, methodNode);
                    else
                        throw new Error(); // shouldn't be possible

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }

    private static String[] parseTarget(String method) {
        final Matcher m = DESCRIPTOR_PATTERN.matcher(method);
        if (!m.matches()) throw new IllegalArgumentException(String.format("Invalid method descriptor: \"%s\"", method));
        return new String[] {m.group(1), m.group(2)};
    }

    /**
     * A method represents a MethodTransformer if and only if it is annotated with a single meme annotation such as @Inject
     */
    private static boolean isValidMethodTransformer(Method m) {
        return Stream.of(m.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .filter(METHOD_ANNOTATION_CLASSES::contains)
                .count() == 1;
    }

    private static boolean hasValidConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean isConstructor(Method m) {
        return m.getName().equals("<init>") || m.getName().equals("<clinit>");
    }
}
