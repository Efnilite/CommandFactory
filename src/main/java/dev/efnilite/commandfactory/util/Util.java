package dev.efnilite.commandfactory.util;

import dev.efnilite.vilib.chat.Message;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Util {

    private static final char[] RANDOM_DIGITS = "1234567890".toCharArray();

    /**
     * Colors strings
     *
     * @param   strings
     *          The strings
     *
     * @return the strings
     */
    public static String[] colour(String... strings) {
        String[] ret = new String[strings.length];
        int i = 0;
        for (String string : strings) {
            ret[i++] = Util.colour(string);
        }
        return ret;
    }

    /**
     * Colours a list
     *
     * @param   strings
     *          The strings
     *
     * @return the coloured string
     */
    public static List<String> colour(List<String> strings) {
        return strings.stream()
                .map(Util::colour)
                .collect(Collectors.toList());
    }

    /**
     * Random digits
     *
     * @return a string with an amount of random digits
     */
    public static String randomDigits(int amount) {
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            random.append(RANDOM_DIGITS[ThreadLocalRandom.current().nextInt(RANDOM_DIGITS.length - 1)]);
        }
        return random.toString();
    }

    /**
     * Color something
     */
    public static String colour(String string) {
        if (string.equals("")) {
            return string;
        }
        return ChatColor.translateAlternateColorCodes('&', Message.parseFormatting(string));
    }

    /**
     * Gets the size of a ConfigurationSection
     *
     * @param   file
     *          The file
     *
     * @param   path
     *          The path
     */
    public static @Nullable List<String> getNode(FileConfiguration file, String path) {
        ConfigurationSection section = file.getConfigurationSection(path);
        if (section == null) {
            return null;
        }
        return new ArrayList<>(section.getKeys(false));
    }
}