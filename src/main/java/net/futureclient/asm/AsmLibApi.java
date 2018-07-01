package net.futureclient.asm;


import net.futureclient.asm.config.Config;
import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.Method;

/**
 * ClassLoader safe api for interacting with AsmLib.
 * Use this class if calling from a class loaded by system classloader like a tweaker,
 * otherwise if loaded by the LaunchClassLoader like with a forge coremod it is safe to directly call {@link net.futureclient.asm.internal.AsmLib}
 */
public final class AsmLibApi {
    private AsmLibApi() {};

    private static final Class<?> asmlib;
    private static final Method mAddConfig_String;
    private static final Method mAddConfig_Config;
    private static final Method mInit;

    static {
        try {
            asmlib = getClass("net.futureclient.asm.internal.AsmLib");
            mAddConfig_String = asmlib.getDeclaredMethod("addConfig", String.class);
            mAddConfig_String.setAccessible(true);
            Class<?> configClass = Class.forName(Config.class.getName(), false, Launch.classLoader);
            mAddConfig_Config = asmlib.getDeclaredMethod("addConfig", configClass);
            mAddConfig_Config.setAccessible(true);

            mInit = asmlib.getDeclaredMethod("init");
            mInit.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new Error(e);
        }

    }

    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name, true, Launch.classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        try {
            mInit.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public static void addConfig(String resourceFile) {
        try {
            mAddConfig_String.invoke(null, resourceFile);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // class must have a 0 argument constructor
    public static void addConfigByClass(String configClassName) {
        try {
            Class<?> clazz = getClass(configClassName);
            Object instance = clazz.newInstance();
            mAddConfig_Config.invoke(null, instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
