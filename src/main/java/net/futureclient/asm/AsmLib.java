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
        getConfigManager().getClassTransformers().stream()
                .map(AsmLib::loadClass)
                .filter(Objects::nonNull)
                .map(TransformerGenerator::fromClass)
                .filter(Objects::nonNull)
                .forEach(transformer -> {
                    getConfigManager().getTransformerCache()
                            .getOrDefault(transformer.getClass().getName(), getConfigManager().getDefaultConfig()) // use default if mapped to nothing
                            .getClassTransformers().add(transformer);

                    getConfigManager().getTransformerCache().remove(transformer.getClassName());
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
