package net.futureclient.asm.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.futureclient.asm.obfuscation.MappingType;
import net.futureclient.asm.obfuscation.ObfuscatedRemapper;
import net.futureclient.asm.obfuscation.RuntimeState;
import org.objectweb.asm.Type;
import net.futureclient.asm.config.Config;
import net.futureclient.asm.config.ConfigManager;
import net.futureclient.asm.transformer.util.TransformerGenerator;
import net.futureclient.asm.transformer.wrapper.LaunchWrapperTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;

/**
 * This class and all other AsmLib classes should be loaded by the LaunchClassLoader except for
 * {@link net.futureclient.asm.AsmLibApi} which should be able to work in any class loader
 */
// TODO: maybe make this package-private
public final class AsmLib {
    private AsmLib() {}

    public static final Logger LOGGER = LogManager.getLogger("AsmLib");
    private static final String VERSION = "0.1";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean initialized;

    private static boolean isDevEnvironment;

    static {
        if (AsmLib.class.getClassLoader() != Launch.classLoader) {
            throw new IllegalStateException("AsmLib must be loaded by the LaunchClassLoader");
        }
    }

    public static void registerConfig(final String configResource) {
        assertInitialized();

        JsonObject root;
        try (final InputStream is = AsmLib.class.getClassLoader().getResourceAsStream(configResource)) {
            Objects.requireNonNull(is, "Failed to find config file: " + configResource);
            root = new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        final Config config = Config.fromJson(root);

        final String mappingsResource = root.get("mappings").getAsString();
        if (mappingsResource == null)
            throw new IllegalStateException("Config is missing mappings file");


        try (final InputStream is = AsmLib.class.getClassLoader().getResourceAsStream(mappingsResource)) {
            if (is != null) {
                JsonObject mappingsRoot = new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject();
                ObfuscatedRemapper.getInstance().addMappings(mappingsRoot);
            } else if (!isDevEnvironment) {
                // got the mapping type but failed to find mappings file
                throw new IllegalStateException("Failed to find mapping file \"" + mappingsResource + "\"");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        registerConfig(config);
    }

    private static void registerConfig(final Config config) {
        ConfigManager.INSTANCE.addConfiguration(config);
        applyTransformerPatches(config);
    }

    private static void getAndSetMappingType() {
        final Optional<MappingType> compiledMappingType = MappingType.getCompiledMappingType();
        if (!compiledMappingType.isPresent()) {
            LOGGER.info("Failed to get mapping type from resources, assuming dev environment");
            RuntimeState.setRuntimeMappingType(MappingType.MCP);
            isDevEnvironment = true;
        } else {
            //RuntimeState.setRuntimeMappingType(compiledMappingType.get());
            RuntimeState.setRuntimeMappingType(MappingType.NOTCH);
            isDevEnvironment = false;
        }
    }

    private static void assertInitialized() {
        if (!initialized)
            throw new IllegalStateException("AsmLib has not been initialized");
    }

    public static void init() {
        if (!initialized) {
            LOGGER.info("AsmLib v{}", VERSION);
            Launch.classLoader.addTransformerExclusion("net.futureclient.asm");

            Launch.classLoader.registerTransformer(TransformerPreProcessor.class.getName());
            Launch.classLoader.registerTransformer(LaunchWrapperTransformer.class.getName());

            getAndSetMappingType();
        }
        initialized = true;
    }

    // TODO: maybe do this lazily
    // TODO: make this work without delegate classes
    // TODO: manually load the classes so we don't rely on the launchwrapper
    private static void applyTransformerPatches(final Config config) {
        config.getTransformerClassNames().stream()
                .map(AsmLib::loadClass)
                .filter(Objects::nonNull)
                .map(AsmLib::createDelegate)
                .filter(Objects::nonNull)
                .map(delegate -> TransformerGenerator.fromClass(delegate, config))
                .filter(Objects::nonNull)
                .forEach(config.getClassTransformers()::add);
    }
    private static TransformerDelegate createDelegate(Class<?> source) {
        try {
            final Class<? extends TransformerDelegate> delegateClass = TransformerDelegate.DELEGATES.get(Type.getInternalName(source));
            Objects.requireNonNull(delegateClass, "Failed to get " + source.getName() + " from delegates (" + source.getClassLoader() + " )");
            final TransformerDelegate instance = delegateClass.newInstance();
            instance.setInstance(source.newInstance());
            return instance;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name, false, Launch.classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
