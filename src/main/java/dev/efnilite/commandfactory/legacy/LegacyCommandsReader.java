package dev.efnilite.commandfactory.legacy;

import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.util.Util;
import dev.efnilite.vilib.util.Task;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LegacyCommandsReader {

    public static void check() {
        File file = new File(CommandFactory.getPlugin().getDataFolder(), "commands.yml");

        if (file.exists()) {
            CommandFactory.logging().warn("Found legacy commands.yml, the data will now be transferred!");
            read(file);
        }
    }

    private static void read(File file) {
        Task.create(CommandFactory.getPlugin())
                .async()
                .execute(() -> {
                    FileConfiguration configuration = CommandFactory.getConfiguration().getFile(file.toString());
                    List<String> nodes = Util.getNode(configuration, "commands");

                    if (nodes == null) {
                        nodes = new ArrayList<>();
                    }

                    for (String id : nodes) {
                        String mainCommand = configuration.getString("commands." + id + ".command");
                        String aliasesRaw = configuration.getString("commands." + id + ".aliases");
                        String perm = configuration.getString("commands." + id + ".permission");
                        String permMsg = configuration.getString("commands." + id + ".permission-message");
                        String executableBy = configuration.getString("commands." + id + ".executable-by");
                        String executableByMessage = configuration.getString("commands." + id + ".executable-by-message");
                        String cooldown = configuration.getString("commands." + id + ".cooldown");
                        String cooldownMessage = configuration.getString("commands." + id + ".cooldown-message");
                        CommandFactory.getProcessor().register(aliasesRaw, mainCommand, perm, permMsg, executableBy,
                                executableByMessage, cooldown, cooldownMessage, true, null);
                    }

                    file.delete();
                    CommandFactory.logging().warn("All legacy data has been transferred!");
                })
                .run();
    }
}
