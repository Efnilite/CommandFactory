package dev.efnilite.commandfactory.util.config;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.util.Util;
import dev.efnilite.fycore.util.Logging;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * An utilities class for the Configuration
 */
public class Configuration {

    private final Plugin plugin;
    private final HashMap<String, FileConfiguration> files;

    /**
     * Create a new instance
     */
    public Configuration(Plugin plugin) {
        this.plugin = plugin;
        files = new HashMap<>();

        String[] defaultFiles = new String[]{"config.yml", "commands.yml"};

        File folder = plugin.getDataFolder();
        if (!new File(folder, defaultFiles[0]).exists() || !new File(folder, defaultFiles[1]).exists()) {
            plugin.getDataFolder().mkdirs();

            for (String file : defaultFiles) {
                plugin.saveResource(file, false);
            }
            Logging.info("Saved all config files");
        }

        // Update config
        try {
            ConfigUpdater.update(plugin, "config.yml", new File(plugin.getDataFolder(), "config.yml"), new ArrayList<>());
            ConfigUpdater.update(plugin, "commands.yml", new File(plugin.getDataFolder(), "commands.yml"), Collections.singletonList("commands"));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logging.error("Error while trying to update config");
        }
        reload(false);
    }

    public void reload(boolean read) {
        files.put("commands", YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "commands.yml")));
        files.put("config", YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml")));
    }

    /**
     * Get a file
     */
    public FileConfiguration getFile(String file) {
        FileConfiguration config;
        if (files.get(file) == null) {
            config = YamlConfiguration.loadConfiguration(new File(file));
            files.put(file, config);
        } else {
            config = files.get(file);
        }
        return config;
    }
}