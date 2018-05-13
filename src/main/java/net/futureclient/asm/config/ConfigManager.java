package net.futureclient.asm.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final List<Config> configs = new ArrayList<>();

    public ConfigManager() {}

    public void addConfiguration(Config config) {
        this.configs.add(config);
    }

    // TODO: don't return new list
    public Collection<Config> getConfigs() {
        return Collections.unmodifiableCollection(
                this.configs.stream()
                .sorted(Config::compareTo)
                .collect(Collectors.toList()));
    }
}
