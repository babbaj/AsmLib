package net.futureclient.asm.config;

import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {

    private final Config default_config = new Config("default", 0); // for orphans

    private final List<Config> configs = Lists.newArrayList(default_config);

    // used to remember what config a transformer belongs to
    // TODO: maybe put somewhere else
    private final Map<String, Config> transformerCache = new HashMap<>();

    public ConfigManager() {}

    public void addConfiguration(Config config) {
        this.configs.add(config);
    }

    public void addTransformer(String className, Config config) {
        transformerCache.merge(className, config, (oldCfg, newCfg) -> oldCfg.compareTo(newCfg) > 0 ? oldCfg : newCfg);
    }

    // TODO: don't return new list
    public Collection<Config> getConfigs() {
        return Collections.unmodifiableCollection(
                this.configs.stream()
                .sorted(Config::compareTo)
                .collect(Collectors.toList()));
    }

    public Config getDefaultConfig() {
        return default_config;
    }

    public Map<String, Config> getTransformerCache() {
        return transformerCache;
    }

    public Set<String> getClassTransformers() {
        return transformerCache.keySet();
    }
}
