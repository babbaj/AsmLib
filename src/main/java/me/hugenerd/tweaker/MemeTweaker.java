package me.hugenerd.tweaker;

import net.futureclient.asm.AsmLib;
import net.futureclient.asm.internal.transformer.TransformerPreProcessor;
import net.futureclient.asm.transformer.wrapper.LaunchWrapperTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

// this class MUST be in a separate package
public final class MemeTweaker implements ITweaker {

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {}

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        // TODO: proper initialization
        //classLoader.addTransformerExclusion("net.futureclient.");

        Class<?> asmLibClass = null;
        try {
            asmLibClass = Class.forName("net.futureclient.asm.AsmLib", true, classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        classLoader.registerTransformer(TransformerPreProcessor.class.getName()); // TODO: move to AsmLib initialization
        classLoader.registerTransformer(LaunchWrapperTransformer.class.getName());

        try {
            asmLibClass.getDeclaredMethod("initTransformerPatches").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
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
