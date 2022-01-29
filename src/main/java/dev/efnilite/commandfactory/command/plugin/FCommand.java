package dev.efnilite.commandfactory.command.plugin;

import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.FactoryMenu;
import dev.efnilite.commandfactory.command.CommandProcessor;
import dev.efnilite.commandfactory.util.Util;
import dev.efnilite.fycore.command.FyCommand;
import dev.efnilite.fycore.util.Time;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FCommand extends FyCommand {

    private final CommandProcessor factory = CommandFactory.getProcessor();
    public static final String MESSAGE_PREFIX = "&#711FDE» &7";

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Util.send(sender, "&8&m-----------&r &#711FDE&lCommandFactory &8&m-----------");
            Util.send(sender, "&#B88CF3/cf &f- &7The main command");
            if (sender.hasPermission("cf.edit")) {
                Util.send(sender, "&#B88CF3/cf add &f- &7Add a command.");
            }
            if (sender.hasPermission("cf.edit")) {
                Util.send(sender, "&#B88CF3/cf edit &f- &7View all commands and edit them in the menu");
            }
            if (sender.hasPermission("cf.edit")) {
                Util.send(sender, "&#B88CF3/cf remove <alias> &f- &7Remove a command, example: /cf remove gmc");
            }
            if (sender.hasPermission("cf.reload")) {
                Util.send(sender, "&#B88CF3/cf reload &f- &7Reload the config and commands");
            }
            if (sender.hasPermission("cf.resetcooldowns")) {
                Util.send(sender, "&#B88CF3/cf resetcooldowns &f- &7Reset all cooldowns. &cThis contains no confirm message.");
            }
            return true;
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
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
                        Util.send(sender, MESSAGE_PREFIX + "Please wait before using that again!");
                        return true;
                    }
                    if (!sender.hasPermission("cf.reload")) {
                        return true;
                    }
                    Time.timerStart("reload");

                    factory.unregisterAll();
                    CommandFactory.getConfiguration().reload(true);

                    Util.send(sender, MESSAGE_PREFIX + "Reloaded in " + Time.timerEnd("reload") + "ms!");
                    return true;

                case "resetcooldowns":
                    if (!sender.hasPermission("cf.resetcooldowns")) {
                        return true;
                    }

                    CommandFactory.getProcessor().resetCooldowns();

                    Util.send(sender, MESSAGE_PREFIX + "Reset all cooldowns!");
                    return true;

                default:
                    Util.send(sender, MESSAGE_PREFIX + "Unknown command. Trying to register a command? Example: /cf add gmc gamemode creative");
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
                        Util.send(sender, MESSAGE_PREFIX + "Removed '" + alias + "'!");
                    } else {
                        Util.send(sender, MESSAGE_PREFIX + "&cCouldn't find '" + alias + "'!");
                    }
                    return true;

                default:
                    Util.send(sender, MESSAGE_PREFIX + "Unknown command. Trying to register a command? Example: /cf add gmc gamemode creative");
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
            return completions(args[0], completions);
        } else {
            return Collections.emptyList();
        }
    }
}
