package dev.efnilite.commandfactory.util.config;

import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.fycore.config.ConfigOption;
import org.bukkit.configuration.file.FileConfiguration;

public class Option {

    public static ConfigOption<Boolean> UPDATER;
    public static ConfigOption<Boolean> VERBOSE;

    public static void init() {
        FileConfiguration config = CommandFactory.getConfiguration().getFile("config");

        UPDATER = new ConfigOption<>(config, "updater");
        VERBOSE = new ConfigOption<>(config, "verbose");
    }
}