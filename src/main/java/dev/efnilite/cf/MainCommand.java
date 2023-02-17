package dev.efnilite.cf;

import dev.efnilite.cf.command.CommandProcessor;
import dev.efnilite.cf.util.Util;
import dev.efnilite.vilib.command.ViCommand;
import dev.efnilite.vilib.util.Time;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CommandFactory main command.
 */
@ApiStatus.Internal
public class MainCommand extends ViCommand {

    private final CommandProcessor processor = CommandFactory.getProcessor();
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Util.send(sender, "");
            Util.send(sender, "<dark_gray><strikethrough>-----------<reset> " + CommandFactory.NAME + " <dark_gray><strikethrough>-----------");
            Util.send(sender, "");
            Util.send(sender, "<gray>/cf <dark_gray>- The main command");
            if (sender.hasPermission("cf.edit")) {
                Util.send(sender, "<gray>/cf menu/edit <dark_gray>- View all commands and edit them in the menu");
            }
            if (sender.hasPermission("cf.edit")) {
                Util.send(sender, "<gray>/cf add <dark_gray>- Add a command.");
            }
            if (sender.hasPermission("cf.edit")) {
                Util.send(sender, "<gray>/cf remove <dark_gray>- Remove a command, example: /cf remove gmc");
            }
            Util.send(sender, "<gray>/cf permissions<dark_gray>- View all permissions");
            if (sender.hasPermission("cf.reload")) {
                Util.send(sender, "<gray>/cf reload <dark_gray>- Reload the config and commands");
            }
            if (sender.hasPermission("cf.resetcooldowns")) {
                Util.send(sender, "<gray>/cf resetcooldowns <dark_gray>- Reset all cooldowns. <#A00000>This has no confirm message.");
            }
            Util.send(sender, "");
            Util.send(sender, "<dark_gray><strikethrough>----------------------------------");
            Util.send(sender, "");
            return true;

        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "permissions":

                    Util.send(sender, "");
                    Util.send(sender, "<dark_gray><strikethrough>-----------<reset> <gradient:#7F00FF>Permissions</gradient:#007FFF> <dark_gray><strikethrough>-----------");
                    Util.send(sender, "");
                    Util.send(sender, "<gray>/cf add <dark_gray>- cf.edit (for adding commands)");
                    Util.send(sender, "<gray>/cf edit <dark_gray>- cf.edit (for editing commands)");
                    Util.send(sender, "<gray>/cf remove <dark_gray>- cf.edit (for removing commands)");
                    Util.send(sender, "<gray>/cf reload <dark_gray>- cf.reload (for reloading the files)");
                    Util.send(sender, "<gray>/cf resetcooldowns <dark_gray>- cf.resetcooldowns (for resetting cooldowns)");
                    Util.send(sender, "");
                    Util.send(sender, "<dark_gray><strikethrough>------------------------------------");
                    Util.send(sender, "");
                    return true;
                case "add":
                    if (sender instanceof Player && sender.hasPermission("cf.edit")) {
                        FactoryMenu.initNew((Player) sender);
                    }
                    return true;
                case "edit":
                case "menu":
                    if (sender instanceof Player && sender.hasPermission("cf.edit")) {
                        FactoryMenu.openMain((Player) sender);
                    }
                    return true;
                case "reload":
                    if (!cooldown(sender, "reload", 500)) {
                        Util.send(sender, CommandFactory.MESSAGE_PREFIX + "Please wait before using that again!");
                        return true;
                    }
                    if (!sender.hasPermission("cf.reload")) {
                        return true;
                    }
                    Time.timerStart("reloadCF");

                    processor.unregisterAll();
                    CommandFactory.getConfiguration().reload();
                    processor.registerAll();

                    Util.send(sender, CommandFactory.MESSAGE_PREFIX + "Reloaded CommandFactory in " + Time.timerEnd("reloadCF") + "ms!");
                    return true;

                case "resetcooldowns":
                    if (!sender.hasPermission("cf.resetcooldowns")) {
                        return true;
                    }

                    CommandFactory.getProcessor().resetCooldowns();

                    Util.send(sender, CommandFactory.MESSAGE_PREFIX + "Reset all cooldowns!");
                    return true;

                default:
                    Util.send(sender, CommandFactory.MESSAGE_PREFIX + "Unknown command");
                    return true;
            }
        } else if (args.length == 2) {
            if ("remove".equalsIgnoreCase(args[0])) {
                if (!sender.hasPermission("cf.edit")) {
                    return true;
                }
                String alias = args[1];
                boolean success = processor.unregister(alias);
                if (success) {
                    Util.send(sender, CommandFactory.MESSAGE_PREFIX + "Removed '" + alias + "'!");
                } else {
                    Util.send(sender, CommandFactory.MESSAGE_PREFIX + "<red>Couldn't find '" + alias + "'!");
                }
                return true;
            }
            Util.send(sender, CommandFactory.MESSAGE_PREFIX + "Unknown command");
            return true;
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("cf.edit")) {
                completions.add("add");
            }
            if (sender.hasPermission("cf.edit")) {
                completions.add("edit");
            }
            completions.add("permissions");
            if (sender.hasPermission("cf.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("cf.resetcooldowns")) {
                completions.add("resetcooldowns");
            }
            return completions(args[0], completions);
        } else {
            return Collections.emptyList();
        }
    }
}
