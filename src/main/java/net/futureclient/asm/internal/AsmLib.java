package net.futureclient.asm.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.transformer.util.TransformerGenerator;
import net.futureclient.asm.transformer.wrapper.LaunchWrapperTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * This class and all other AsmLib classes should be loaded by the LaunchClassLoader except for
 * {@link net.futureclient.asm.AsmLibApi} which should be able to work in any class loader
 */
final class AsmLib {
    private AsmLib() {}

    public static final Logger LOGGER = LogManager.getLogger("AsmLib");
    private static final String VERSION = "0.1";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean initialized;

    static {
        if (AsmLib.class.getClassLoader() != Launch.classLoader) {
            throw new IllegalStateException("AsmLib must be loaded by the LaunchClassLoader");
        }
    }

    public static void addConfig(final String configResource) {
        final InputStream is = AsmLib.class.getClassLoader().getResourceAsStream(configResource);
        Objects.requireNonNull(is, "Failed to find config file: " + configResource);
        final JsonObject root = new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject();

        final Config config = Config.fromJson(root);
        addConfig(config);
    }

    public static void addConfig(final Config config) {
        ConfigManager.INSTANCE.addConfiguration(config);
        applyTransformerPatches(config); // TODO: do this lazily
    }

    public static void init() {
        if (!initialized) {
            LOGGER.info("AsmLib v{}", VERSION);
            Launch.classLoader.addTransformerExclusion("net.futureclient.asm");

            Launch.classLoader.registerTransformer(TransformerPreProcessor.class.getName());
            Launch.classLoader.registerTransformer(LaunchWrapperTransformer.class.getName());
        }
        initialized = true;
    }

    // TODO: maybe do this lazily
    private static void applyTransformerPatches(final Config config) {
        config.getTransformerClassNames().stream()
                .map(AsmLib::loadClass)
                .filter(Objects::nonNull)
                .map(clazz -> TransformerGenerator.fromClass(clazz, config))
                .filter(Objects::nonNull)
                .forEach(config.getClassTransformers()::add);
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name, true, Launch.classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}