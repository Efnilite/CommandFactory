package dev.efnilite.commandfactory.util.config;

import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.util.Util;
import dev.efnilite.fycore.config.ConfigUpdater;
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
        } finally {
            reload(false);
        }
    }

    public void reload(boolean read) {
        files.put("commands", YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "commands.yml")));
        files.put("config", YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml")));

        if (read) {
            read();
        }
    }

    public void read() {
        Logging.verbose("Reading commands file");
        FileConfiguration file = getFile("commands");
        List<String> nodes = Util.getNode(file, "commands");
        if (nodes == null) {
            Logging.warn("No commands were found in commands.yml!");
            return;
        }
        for (String id : nodes) {
            String mainCommand = file.getString("commands." + id + ".command");
            String aliasesRaw = file.getString("commands." + id + ".aliases");
            String perm = file.getString("commands." + id + ".permission");
            String permMsg = file.getString("commands." + id + ".permission-message");
            String executableBy = file.getString("commands." + id + ".executable-by");
            String executableByMessage = file.getString("commands." + id + ".executable-by-message");
            String cooldown = file.getString("commands." + id + ".cooldown");
            String cooldownMessage = file.getString("commands." + id + ".cooldown-message");
            CommandFactory.getProcessor().register(aliasesRaw, mainCommand, perm, permMsg, executableBy,
                    executableByMessage, cooldown, cooldownMessage, false, id);
        }
    }

    public void save(String file) {
        Logging.verbose("Saving file " + file);

        file = file.replaceAll("(.|)yml", "");
        FileConfiguration config = getFile(file);
        try {
            config.save(new File(plugin.getDataFolder(), file + ".yml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logging.error("There was an error while trying to save file " + file);
        }
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

    /**
     * Gets a coloured string
     *
     * @param   file
     *          The file
     * @param   path
     *          The path
     *
     * @return a coloured string
     */
    public @Nullable
    List<String> getStringList(String file, String path) {
        List<String> string = getFile(file).getStringList(path);
        if (string.size() == 0) {
            return null;
        }
        return Util.colour(string);
    }

    /**
     * Gets a coloured string
     *
     * @param   file
     *          The file
     * @param   path
     *          The path
     *
     * @return a coloured string
     */
    public @Nullable String getString(String file, String path) {
        String string = getFile(file).getString(path);
        if (string == null) {
            return null;
        }
        return Util.colour(string);
    }
}