package dev.efnilite.commandfactory.util.config;

import dev.efnilite.commandfactory.CommandFactory;
import org.bukkit.configuration.file.FileConfiguration;

public class Option {

    public static boolean AUTO_UPDATER;

    public static void init() {
        FileConfiguration config = CommandFactory.getConfiguration().getFile("config");

        AUTO_UPDATER = config.getBoolean("auto-updater");
    }
}