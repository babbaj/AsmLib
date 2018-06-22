package me.hugenerd.tweaker;

import me.hugenerd.load.config.MemeConfig;
import net.futureclient.asm.AsmLib;
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
        //AsmLib.addConfig(new MemeConfig());
        AsmLib.addConfig("test_config.json");
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
