package dev.efnilite.commandfactory.command;

import dev.efnilite.commandfactory.CommandFactory;
import dev.efnilite.commandfactory.command.wrapper.AliasedCommand;
import dev.efnilite.commandfactory.command.wrapper.BukkitCommand;
import dev.efnilite.commandfactory.util.CommandReflections;
import dev.efnilite.commandfactory.util.Util;
import dev.efnilite.vilib.chat.Message;
import dev.efnilite.vilib.util.Logging;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles registering and dealing with executed commands.
 * Also registers commands with the server, so they show up in auto-complete.
 */
public final class CommandProcessor implements CommandExecutor {

    private SimpleCommandMap map;
    private final Pattern replaceableArgumentPattern = Pattern.compile("%\\w+[-| ](\\d+)%");
    private final Map<String, AliasedCommand> register;
    private final Map<String, BukkitCommand> pluginRegister;
    private final Map<UUID, Map<AliasedCommand, Long>> lastExecuted;

    public CommandProcessor() {
        this.register = new HashMap<>();
        this.pluginRegister = new HashMap<>();
        this.lastExecuted = new HashMap<>();
        this.map = CommandReflections.retrieveMap();

        try {
            Path commands = Paths.get(CommandFactory.getInstance().getDataFolder().getPath(), "commands");

            if (!commands.toFile().exists()) { // if path does not exist, create it
                commands.toFile().mkdirs();
            }

            List<Path> paths = Files.list(commands)
                    .filter((file) -> file.getFileName().endsWith(".json")) // only read json files
                    .collect(Collectors.toList());

            for (Path path : paths) {
                AliasedCommand command = AliasedCommand.read(path.getFileName().toFile());
                if (command == null) {
                    continue;
                }
                register("", command.getMainCommand(), command.getPermission(), command.getPermissionMessage(),
                        command.getExecutableBy().name().toLowerCase(), command.getExecutableByMessage(),
                        command.getCooldownString(), command.getCooldownMessage(), false, command.getId());
            }
        } catch (Throwable throwable) {
            Logging.stack("Error while reading commands", "Please report this error to the developer!", throwable);
        }
    }

    public @Nullable RegisterNotification register(String aliasesRaw, String mainCommand) {
        return register(aliasesRaw, mainCommand, null, null, null, null, null, null, true, null);
    }

    /**
     * Registers a command
     *
     * @param   aliasesRaw
     *          The raw aliases
     *
     * @param   mainCommand
     *          The actual command
     *
     * @param   perm
     *          The permission required (optional)
     *
     * @param   permMsg
     *          The permission message
     *
     * @param   updateFile
     *          If it should update the file
     *
     * @param   id
     *          The id if it is already registered in the file
     */
    public @Nullable RegisterNotification register(String aliasesRaw, String mainCommand, @Nullable String perm, @Nullable String permMsg,
                            @Nullable String executableBy, @Nullable String executableByMessage, @Nullable String cooldown,
                            @Nullable String cooldownMessage, boolean updateFile, @Nullable String id) {
        if (mainCommand == null) {
            Logging.error("Main command of alias(es) '" + aliasesRaw + "' is null.");
            Logging.error("Please check if you have added a 'command:' section to your command.");
            return RegisterNotification.ARGUMENT_NULL;
        }
        if (aliasesRaw == null) {
            Logging.error("Alias(es) of main command '" + mainCommand + "' are null.");
            Logging.error("Please check if you have added a 'aliases:' section to your command.");
            return RegisterNotification.ARGUMENT_NULL;
        }
        this.map = CommandReflections.retrieveMap(); // update map

        Logging.verbose("Registering command " + mainCommand + " under alias(es) " + aliasesRaw);

        String[] aliases = aliasesRaw.replace(", ", ",").split(",");

        Matcher matcher = replaceableArgumentPattern.matcher(mainCommand); // check replaceable args
        if (id == null) {
            id = Util.randomDigits(9);
        }

        AliasedCommand command = new AliasedCommand(id, mainCommand, perm, permMsg, executableBy, executableByMessage, cooldown, cooldownMessage, matcher.find());
        for (String alias : aliases) { // check before registering
            if (register.containsKey(alias)) {
                command.setNotification(RegisterNotification.ALIAS_ALREADY_EXISTS);
                return RegisterNotification.ALIAS_ALREADY_EXISTS;
            }
        }

        RegisterNotification notification = null;
        for (String alias : aliases) {
            matcher = replaceableArgumentPattern.matcher(alias); // check replaceable args
            if (matcher.find()) {
                alias = alias.replaceAll(" " + replaceableArgumentPattern, "");
            }

            register.put(alias, command);

            if (alias.contains(" ")) { // /x y
                String main = alias.split(" ")[0]; // get x
                Command previous = registerToMap(main, command); // register x

                if (previous != null) { // if there was a previous command, warn the user for overwriting
                    notification = RegisterNotification.OVERRIDING_EXISTING;
                    command.setNotification(RegisterNotification.OVERRIDING_EXISTING);
                }
            } else { // /xy
                Command previous = registerToMap(alias, command);

                if (previous != null) {
                    notification = RegisterNotification.OVERRIDING_EXISTING;
                    command.setNotification(RegisterNotification.OVERRIDING_EXISTING);
                }
            }
        }

        if (updateFile) {
            command.save();
        }
        return notification;
    }

    public boolean unregister(String alias) {
        if (!register.containsKey(alias)) {
            return false;
        }

        Logging.verbose("Unregistering " + alias);

        unregisterToMap(alias);
        register.get(alias).delete();

        return register.remove(alias) != null;
    }

    /**
     * Unregisters all commands
     */
    public void unregisterAll() {
        for (String alias : new ArrayList<>(register.keySet())) {
            unregister(alias);
        }
    }

    /**
     * Resets all cooldowns
     */
    public void resetCooldowns() {
        lastExecuted.clear();
    }

    /**
     * Registers a command to the CommandMap, making it show up as a command
     *
     * @param   alias
     *          The name of the command
     *
     * @param   command
     *          The details associated with this aliased command.
     */
    public @Nullable Command registerToMap(String alias, AliasedCommand command) {
        alias = alias.replaceFirst("/", "");

        BukkitCommand pluginCommand = new BukkitCommand(alias, this);

        if (command.getPermission() != null) {
            pluginCommand.setPermission(command.getPermission());
        }
        if (command.getPermissionMessage() != null) {
            pluginCommand.setPermissionMessage(command.getPermissionMessage());
        }
        pluginRegister.put(alias, pluginCommand);

        return CommandReflections.addToKnown(alias, pluginCommand, map);
    }

    /**
     * Unregisters from map
     *
     * @param   alias
     *          The alias which it was registered
     */
    public void unregisterToMap(String alias) {
        BukkitCommand command = pluginRegister.get(alias);
        if (command != null) {
            command.unregister(map);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command sentCommand, @NotNull String label, @NotNull String[] args) {
        String textCommand = "/" + label.replace("cf:", "");
        AliasedCommand command = get(textCommand); // get entered command
        AliasedCommand original = command;
        if (command == null) { // if command is not found it might be a command with spaces
            String withSpaces = textCommand + " " + String.join(" ", args);

            command = get(withSpaces);
            if (command == null) { // if command with spaces is not found it might be with replaceable args
                return false;
            }
        }
        if (command.containsReplaceableArguments()) {
            command = get(textCommand);

            if (command == null) {
                return false;
            }
            command = command.clone();
            String mainCommand = command.getMainCommand();

            Matcher matcher = replaceableArgumentPattern.matcher(mainCommand);
            while (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1)) - 1;
                if (args.length <= index) {
                    Message.send(sender, CommandFactory.MESSAGE_PREFIX + "You don't have enough arguments or you have too many!");
                    return false;
                }
                String input = args[index];
                mainCommand = mainCommand.replaceFirst(replaceableArgumentPattern.toString(), input);
            }

            command.setMainCommand(mainCommand);
        }

        process(command, original, sender);
        return true;
    }

    /**
     * Executes a command which was previously done by an alias.
     *
     * @param   command
     *          The {@link AliasedCommand} with the details of the alias command
     *
     * @param   sender
     *          The sender which will execute the command if they have permissions.
     */
    public void process(AliasedCommand command, AliasedCommand original, CommandSender sender) {
        Logging.verbose("Processing " + command.getMainCommand());

        if (command.getExecutableBy() != null) {
            Executor executor = command.getExecutableBy();
            if (executor == Executor.CONSOLE && !(sender instanceof ConsoleCommandSender)) { // if only console can execute & sender is not console
                if (command.getExecutableByMessage() != null) {
                    sender.sendMessage(Util.colour(command.getExecutableByMessage()));
                }
                return;
            }
            if (executor == Executor.PLAYER && !(sender instanceof Player)) {
                if (command.getExecutableByMessage() != null) {
                    sender.sendMessage(Util.colour(command.getExecutableByMessage()));
                }
                return;
            }
        }

        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            if (command.getPermissionMessage() != null) {
                sender.sendMessage(Util.colour(command.getPermissionMessage()));
            }
            return;
        }

        if (sender instanceof Player) { // check cooldown for players only
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            Map<AliasedCommand, Long> lastPlayer = new HashMap<>();

            if (lastExecuted.containsKey(uuid)) {
                lastPlayer = lastExecuted.get(uuid);
                Long lastExecution = lastPlayer.get(original);
                if (lastExecution != null) {
                    long lastUsedAgo = System.currentTimeMillis() - lastExecution; // 100 - 90
                    long cooldown = command.getCooldownMs();
                    if (lastUsedAgo < cooldown) { // 101 < 100??
                        if (command.getCooldownMessage() != null) {
                            sender.sendMessage(Util.colour(command.getCooldownMessage()
                                    .replace("%cooldown%", DurationFormatUtils.formatDurationWords(cooldown, true, false))
                                    .replace("%time%", DurationFormatUtils.formatDurationWords(cooldown - lastUsedAgo, true, false))));
                        }
                        return;
                    }
                }
            }
            lastPlayer.put(original, System.currentTimeMillis());
            lastExecuted.put(uuid, lastPlayer);
        }

        try {
            boolean foundCommand = Bukkit.dispatchCommand(sender, command.getMainCommand().replaceFirst("/", "").replace("%player%", sender.getName()));
            if (!foundCommand) {
                Message.send(sender, "<#711FDE>» &7Unknown main command: '" + command.getMainCommand() + "'");
            }
        } catch (StackOverflowError overflow) {
            Message.send(sender, CommandFactory.MESSAGE_PREFIX + "<red>Found infinite loop while processing command '" + command.getMainCommand() + "'");
            Message.send(sender, "<gray>Please make sure your alias and main command are not the same.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Message.send(sender, CommandFactory.MESSAGE_PREFIX + "<gray>Unhandled exception while processing command '" + command.getMainCommand() + "'");
            Message.send(sender, "<gray>(This is <underline>not</underline><gray> a problem with CommandFactory; contact the plugin developer and check the console)");
            Message.send(sender, "<gray>Exception: " + throwable.getMessage());
        }
    }

    /**
     * Edits the permission of an alias
     *
     * @param   alias
     *          The alias
     *
     * @param   permission
     *          The permission
     *
     * @return true if the command is found, false if not
     */
    public boolean editPermission(String alias, @NotNull String permission) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        Logging.verbose("Permission of " + alias + " changed to " + permission);
        command.setPermission(permission);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Edits the main command of an alias
     *
     * @param   alias
     *          The alias
     *
     * @param   mainCommand
     *          The main command
     *
     * @return true if the command is found, false if not
     */
    public boolean editMainCommand(String alias, @NotNull String mainCommand) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        Logging.verbose("Main command of " + alias + " changed to " + mainCommand);
        command.setMainCommand(mainCommand);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Edits the possible executors of an alias
     *
     * @param   alias
     *          The alias
     *
     * @param   executor
     *          The executor
     *
     * @return true if the command is found, false if not
     */
    public boolean editExecutableBy(String alias, Executor executor) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        String name = executor.name().toLowerCase();
        Logging.verbose("Executor of " + alias + " changed to " + name);
        command.setExecutableBy(executor);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Edits the message a player gets when they can't execute the command
     *
     * @param   alias
     *          The alias
     *
     * @param   message
     *          The message
     *
     * @return true if the command is found, false if not
     */
    public boolean editExecutableByMessage(String alias, @NotNull String message) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        Logging.verbose("Executor message of " + alias + " changed to " + message);
        command.setExecutableByMessage(message);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Edits the cooldown.
     *
     * @param   alias
     *          The alias
     *
     * @param   ms
     *          The cooldown in millis
     *
     * @return true if the command is found, false if not
     */
    public boolean editCooldown(String alias, long ms) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        command.setCooldownMs(ms);
        String cooldown = command.getCooldownString();

        Logging.verbose("Cooldown of " + alias + " changed to " + cooldown);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Edits the message a player gets when the cooldown is still counting down.
     *
     * @param   alias
     *          The alias
     *
     * @param   message
     *          The message
     *
     * @return true if the command is found, false if not
     */
    public boolean editCooldownMessage(String alias, @NotNull String message) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        Logging.verbose("Cooldown message of " + alias + " changed to " + message);
        command.setCooldownMessage(message);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Edits the permission message of an alias
     *
     * @param   alias
     *          The alias
     *
     * @param   permissionMessage
     *          The permission message
     *
     * @return true if the command is found, false if not
     */
    public boolean editPermissionMessage(String alias, @NotNull String permissionMessage) {
        AliasedCommand command = register.get(alias);
        if (command == null) {
            return false;
        }

        Logging.verbose("Permission message of " + alias + " changed to " + permissionMessage);
        command.setPermissionMessage(permissionMessage);
        register.put(alias, command); // update local

        command.save();
        return true;
    }

    /**
     * Returns the size of the registered commands
     *
     * @return the mapped size
     */
    public int getMappedSize() {
        return register.size();
    }

    public List<String> getAliases() {
        return new ArrayList<>(register.keySet());
    }

    /**
     * Gets an {@link AliasedCommand} from an alias
     *
     * @param   alias
     *          The alias
     *
     * @return the command associated with the alias
     */
    public @Nullable AliasedCommand get(String alias) {
        return register.get(alias);
    }
}