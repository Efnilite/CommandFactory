package dev.efnilite.fycore.chat;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum with all chat colours and their hex values
 *
 * @author Efnilite
 */
public enum ChatColour {

    DARK_RED("dark_red", "AA0000"),
    RED("red", "FF5555"),
    GOLD("gold", "FFAA00"),
    YELLOW("yellow", "FFFF55"),
    DARK_GREEN("dark_green", "00AA00"),
    GREEN("green", "55FF55"),
    AQUA("aqua", "55FFFF"),
    DARK_AQUA("dark_aqua", "00AAAA"),
    DARK_BLUE("dark_blue", "0000AA"),
    BLUE("blue", "5555FF"),
    LIGHT_PURPLE("light_purple", "FF55FF"),
    DARK_PURPLE("dark_purple", "AA00AA"),
    WHITE("white", "FFFFFF"),
    GRAY("gray", "AAAAAA"),
    DARK_GRAY("dark_gray", "555555"),
    BLACK("black", "000000");

    /**
     * The name of the colour
     */
    private final String name;

    /**
     * The hex code
     */
    private final String hex;


    private static final Map<String, ChatColour> BY_NAME = new HashMap<>();

    ChatColour(String name, String hex) {
        this.name = name;
        this.hex = hex;
    }


    /**
     * Gets a ChatFormat by its name
     *
     * @param   name
     *          The name
     *
     * @return the ChatFormat if found, null if n ot
     */
    public static @Nullable ChatColour getByName(String name) {
        if (BY_NAME.size() == 0) {
            for (ChatColour colour : values()) {
                BY_NAME.put(colour.name, colour);
            }
        }

        return BY_NAME.get(name.toLowerCase());
    }

    public String getName() {
        return name;
    }

    public String getHex() {
        return hex;
    }
}
