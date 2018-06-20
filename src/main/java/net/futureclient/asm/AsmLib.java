package net.futureclient.asm;

import me.hugenerd.load.config.MemeConfig;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.internal.TransformerPreProcessor;
import net.futureclient.asm.transformer.util.TransformerGenerator;
import net.futureclient.asm.transformer.wrapper.LaunchWrapperTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Random;

public final class AsmLib {
    private AsmLib() {}

    public static final Logger LOGGER = LogManager.getLogger("AsmLib");
    private static final String VERSION = "0.1";


    static void init() {
        LOGGER.info("AsmLib v{}", VERSION);

        Launch.classLoader.registerTransformer(TransformerPreProcessor.class.getName());
        Launch.classLoader.registerTransformer(LaunchWrapperTransformer.class.getName());

        AsmLib.getConfigManager().addConfiguration(new MemeConfig()); // TODO: get configs

        initTransformerPatches();
    }

    private static void initTransformerPatches() {
        getConfigManager().getConfigs().forEach(config -> {
            config.getTransformerClassNames().stream()
                    .map(AsmLib::loadClass)
                    .filter(Objects::nonNull)
                    .map(clazz -> TransformerGenerator.fromClass(clazz, config))
                    .filter(Objects::nonNull)
                    .forEach(config.getClassTransformers()::add);
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
        return ConfigManager.INSTANCE;
    }
}
