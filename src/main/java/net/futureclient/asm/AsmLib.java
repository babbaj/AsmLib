package net.futureclient.asm;

import me.hugenerd.load.config.MemeConfig;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.transformer.ClassTransformer;
import net.futureclient.asm.transformer.util.TransformerGenerator;
import net.futureclient.asm.transformer.wrapper.LaunchWrapperTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

public final class AsmLib {
    private AsmLib() {}

    public static final Logger LOGGER = LogManager.getLogger("asmlib");
    private static final String VERSION = "0.1";

    private static ConfigManager configManager = new ConfigManager();

    // pre initialization
    static {
        LOGGER.info("AsmLib v{}", VERSION);
        AsmLib.getConfigManager().addConfiguration(new MemeConfig());
    }

    // to be called via reflection
    public static void initTransformerPatches() {
        getConfigManager().getConfigs().stream()
                .map(Config::getTransformerClasses)
                .flatMap(Set::stream)
                .map(AsmLib::loadClass)
                .filter(Objects::nonNull)
                .map(TransformerGenerator::fromClass)
                .filter(Objects::nonNull)
                .forEach(transformer -> {
                    getConfigManager().getConfigs().iterator().next().getClassTransformers().add(transformer); // TODO: do this properly
                });
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name, true, Launch.classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
