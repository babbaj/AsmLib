package net.futureclient.asm;

import net.futureclient.asm.config.ConfigManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AsmLib {

    private static final Logger LOGGER = LogManager.getLogger("asmlib");
    private static final String VERSION = "0.1";

    private static AsmLib instance = new AsmLib();
    private ConfigManager configManager;

    private AsmLib() {
        LOGGER.log(Level.INFO, "Initializing AsmLib version {}.", VERSION);
        instance = this;
        configManager = new ConfigManager();
        LOGGER.log(Level.INFO, "AsmLib version {} initialised.", VERSION);
    }

    public static AsmLib getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
