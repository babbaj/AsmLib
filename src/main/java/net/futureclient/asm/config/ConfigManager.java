package net.futureclient.asm.config;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {

    private final Config default_config = new Config("default", 0);

    private final Set<Config> configs = new HashSet<>();
    { configs.add(default_config); }

    public static final ConfigManager INSTANCE = new ConfigManager();

    private ConfigManager() {
    }

    public void addConfiguration(Config config) {
        this.configs.add(config);
    }

    public Collection<Config> getConfigs() {
        return Collections.unmodifiableCollection(configs.stream()
                .sorted(Config::compareTo)
                .collect(Collectors.toList()));
    }

    public Config getDefaultConfig() {
        return default_config;
    }

}
