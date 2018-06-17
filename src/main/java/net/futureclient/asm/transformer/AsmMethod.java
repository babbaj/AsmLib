package net.futureclient.asm.transformer;

import net.futureclient.asm.AsmLib;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.internal.LambdaManager;
import net.futureclient.asm.internal.TransformerUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;


/**
 * Created by Babbaj on 6/5/2018.
 *
 * Wrapper for MethodNode that provides helper functions.
 */
public class AsmMethod {

    public final MethodNode method;
    private AbstractInsnNode cursor;
    public final ClassNode parent;
    private final Config config;

    // Maps config name to index of last carrier class
    private static final Map<String, Integer> carrierClassIndex = new HashMap<>();

    public AsmMethod(MethodNode methodIn, ClassNode parentClass) {
        this(methodIn, parentClass, AsmLib.getConfigManager().getDefaultConfig());
    }

    public AsmMethod(MethodNode methodIn, ClassNode parentClass, Config configIn) {
        this.method = methodIn;
        this.parent = parentClass;
        this.cursor = method.instructions.getFirst();
        this.config = configIn;
        carrierClassIndex.putIfAbsent(configIn.getName(), 0);
    }

    public void invoke(Object instance) {
        String[] func = LambdaManager.lambdas.get(instance);
        if (func != null) {
            this.method.instructions.insertBefore(cursor,
                    new MethodInsnNode(INVOKESTATIC, func[0], func[1], func[2]));
        } else {
            assertValidFunc(instance);
            final Method abstractMethod = getMethod(instance);
            final Class iface = Stream.of(instance.getClass().getInterfaces())
                    .filter(clazz -> Stream.of(clazz.getDeclaredMethods())
                            .filter(m -> Modifier.isAbstract(m.getModifiers()))
                            .count() == 1
                    )
                    .findFirst()
                    .get();

            final String carrierClassName = newCarrierClass(instance, iface, abstractMethod);

            this.method.instructions.insertBefore(cursor,
                    new MethodInsnNode(INVOKESTATIC, carrierClassName, abstractMethod.getName(), Type.getMethodDescriptor(abstractMethod)));
        }
    }

    private String newCarrierClass(Object instance, Class iface, Method abstractMethod) {
        String className;
        for (int i = carrierClassIndex.get(config.getName()); ; i++) {
            className = String.format("#%s$carrier$%d", config.getName(), i);
            try {
                Class.forName(className);
            } catch (ClassNotFoundException ignored) {
                TransformerUtil.createCarrierClass(className, instance, iface, abstractMethod);
                carrierClassIndex.put(config.getName(), i);
                break;
            }
        }
        return className;
    }

    private Method getMethod(Object instance) {
        return getAbstractMethods(instance.getClass())
                .findFirst()
                .get();
    }
    
    private Stream<Method> getAbstractMethods(Class<?> clazz) {
        return Stream.of(clazz.getInterfaces())
                .filter(itf -> itf.getDeclaredMethods().length > 0)
                .flatMap(itf -> Stream.of(itf.getDeclaredMethods()))
                .filter(m -> Modifier.isAbstract(m.getModifiers()));
    }

    // throws an IllegalArgumentException if the object is not an instance of a functional interface
    private void assertValidFunc(Object instance){
        if (instance.getClass().getInterfaces().length == 0)
            throw new IllegalArgumentException("Object does not implement any interface");
        if (getAbstractMethods(instance.getClass()).count() > 1)
            throw new IllegalArgumentException("Object implements multiple or non functional interfaces");
    }


    public void run(Runnable r) {
        invoke(r);
    }

    // invoke a function that returns a value
    public <T> void get(Supplier<T> supplier) {
        invoke(supplier);
    }

    public <T> void consume(Consumer<T> consumer) {
        invoke(consumer);
    }

}
