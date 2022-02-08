package dev.efnilite.commandfactory.command.plugin;

import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.FactoryMenu;
import dev.efnilite.commandfactory.command.CommandProcessor;
import dev.efnilite.fycore.chat.Message;
import dev.efnilite.fycore.command.FyCommand;
import dev.efnilite.fycore.util.Time;
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
public class FCommand extends FyCommand {

    private final CommandProcessor factory = CommandFactory.getProcessor();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Message.send(sender, "");
            Message.send(sender, "<dark_gray><strikethrough>-----------<reset> " + CommandFactory.NAME + " <dark_gray><strikethrough>-----------");
            Message.send(sender, "");
            Message.send(sender, "<gray>/cf <dark_gray>- The main command");
            Message.send(sender, "<gray>/cf permissions<dark_gray>- View all permissions");
            if (sender.hasPermission("cf.edit")) {
                Message.send(sender, "<gray>/cf add <dark_gray>- Add a command.");
            }
            if (sender.hasPermission("cf.edit")) {
                Message.send(sender, "<gray>/cf edit <dark_gray>- View all commands and edit them in the menu");
            }
            if (sender.hasPermission("cf.edit")) {
                Message.send(sender, "<gray>/cf remove <dark_gray>- Remove a command, example: /cf remove gmc");
            }
            if (sender.hasPermission("cf.reload")) {
                Message.send(sender, "<gray>/cf reload <dark_gray>- Reload the config and commands");
            }
            if (sender.hasPermission("cf.resetcooldowns")) {
                Message.send(sender, "<gray>/cf resetcooldowns <dark_gray>- Reset all cooldowns. <#A00000>This contains no confirm message.");
            }
            Message.send(sender, "");
            Message.send(sender, "<dark_gray><strikethrough>----------------------------------");
            Message.send(sender, "");
            return true;

        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "permissions":

                    Message.send(sender, "");
                    Message.send(sender, "<dark_gray><strikethrough>-----------<reset> <gradient:#7F00FF>Permissions</gradient:#007FFF> <dark_gray><strikethrough>-----------");
                    Message.send(sender, "");
                    Message.send(sender, "<gray>/cf add <dark_gray>- cf.edit");
                    Message.send(sender, "<gray>/cf edit <dark_gray>- cf.edit");
                    Message.send(sender, "<gray>/cf remove <dark_gray>- cf.edit");
                    Message.send(sender, "<gray>/cf reload <dark_gray>- cf.reload");
                    Message.send(sender, "<gray>/cf resetcooldowns <dark_gray>- cf.resetcooldowns");
                    Message.send(sender, "");
                    Message.send(sender, "<dark_gray><strikethrough>----------------------------------");
                    Message.send(sender, "");
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
                        Message.send(sender, CommandFactory.MESSAGE_PREFIX + "Please wait before using that again!");
                        return true;
                    }
                    if (!sender.hasPermission("cf.reload")) {
                        return true;
                    }
                    Time.timerStart("reload");

                    factory.unregisterAll();
                    CommandFactory.getConfiguration().reload(true);

                    Message.send(sender, CommandFactory.MESSAGE_PREFIX + "Reloaded " + CommandFactory.NAME + "<gray> in " + Time.timerEnd("reload") + "ms!");
                    return true;

                case "resetcooldowns":
                    if (!sender.hasPermission("cf.resetcooldowns")) {
                        return true;
                    }

                    CommandFactory.getProcessor().resetCooldowns();

                    Message.send(sender, CommandFactory.MESSAGE_PREFIX + "Reset all cooldowns!");
                    return true;

                default:
                    Message.send(sender, CommandFactory.MESSAGE_PREFIX + "Unknown command");
                    return true;
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "remove":
                    if (!sender.hasPermission("cf.edit")) {
                        return true;
                    }
                    String alias = args[1];
                    boolean success = factory.unregister(alias);
                    if (success) {
                        Message.send(sender, CommandFactory.MESSAGE_PREFIX + "Removed '" + alias + "'!");
                    } else {
                        Message.send(sender, CommandFactory.MESSAGE_PREFIX + "<red>Couldn't find '" + alias + "'!");
                    }
                    return true;

                default:
                    Message.send(sender, CommandFactory.MESSAGE_PREFIX + "Unknown command");
                    return true;
            }
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
            if (sender.hasPermission("cf.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("cf.resetcooldowns")) {
                completions.add("resetcooldowns");
            }
            completions.add("permissions");
            return completions(args[0], completions);
        } else {
            return Collections.emptyList();
        }
    }
}
