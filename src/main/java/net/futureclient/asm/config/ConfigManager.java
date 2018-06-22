package net.futureclient.asm.config;

import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {

    private final Config default_config = new Config("default", 0);

    private final Multiset<Config> configs = TreeMultiset.create(Config::compareTo);
    { configs.add(default_config); }

    public static final ConfigManager INSTANCE = new ConfigManager();

    private ConfigManager() {}

    public void addConfiguration(Config config) {
        this.configs.add(config);
    }

    public Collection<Config> getConfigs() {
        return configs;
    }

    public Config getDefaultConfig() {
        return default_config;
    }

}
