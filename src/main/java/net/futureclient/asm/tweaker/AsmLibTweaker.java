package net.futureclient.asm.tweaker;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Babbaj on 6/20/2018.
 */
public class AsmLibTweaker implements ITweaker {

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {}

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addTransformerExclusion("net.futureclient.asm.");

        try {
            Class<?> asmlib = Class.forName("net.futureclient.asm.internal.AsmLib", true, classLoader);
            Method init = asmlib.getDeclaredMethod("init");
            init.setAccessible(true);
            init.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLaunchTarget() {
        return "me.hugenerd.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
