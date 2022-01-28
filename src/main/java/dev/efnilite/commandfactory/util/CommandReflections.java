package dev.efnilite.commandfactory.util;

import dev.efnilite.fycore.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public class CommandReflections {

    public static @Nullable SimpleCommandMap retrieveMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (SimpleCommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            Logging.error("Error while trying to access the command map.");
            Logging.error("Commands will not show up on completion.");
            ex.printStackTrace();
            return null;
        }
    }

    public static Command addToKnown(String alias, Command command, CommandMap map) {
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);

            Map<String, Command> knownCommands = (Map<String, Command>) field.get(map);

            Command prev1 = knownCommands.put("cf:" + alias, command);
            Command prev2 = knownCommands.put(alias, command);

            field.set(map, knownCommands);

            return prev1 == null ? prev2 : prev1;
        } catch (IllegalAccessException  | NoSuchFieldException ex) {
            ex.printStackTrace();
            Logging.error("There was an error while trying to register your command to the Command Map");
            Logging.error("It might not show up in-game in the auto-complete, but it does work.");
            return null;
        }
    }

}
