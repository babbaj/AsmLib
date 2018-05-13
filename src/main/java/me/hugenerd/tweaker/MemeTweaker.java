package me.hugenerd.tweaker;

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
        classLoader.registerTransformer(LaunchWrapperTransformer.class.getName());
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
