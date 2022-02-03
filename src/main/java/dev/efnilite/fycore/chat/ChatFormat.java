package dev.efnilite.fycore.chat;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum of all chat formatting options
 *
 * @author Efnilite
 */
public enum ChatFormat {

    OBFUSCATED("obfuscated", 'k'),
    BOLD("bold", 'l'),
    STRIKETHROUGH("strikethrough", 'm'),
    UNDERLINE("underline", 'n'),
    ITALIC("italic", 'o'),
    RESET("reset", 'r');

    /**
     * The name of the format
     */
    private final String name;

    /**
     * The code used for this format
     */
    private final char code;

    private static final Map<String, ChatFormat> BY_NAME = new HashMap<>();

    ChatFormat(String name, char code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Gets a ChatFormat by its name
     *
     * @param   name
     *          The name
     *
     * @return the ChatFormat if found, null if n ot
     */
    public static @Nullable ChatFormat getByName(String name) {
        if (BY_NAME.size() == 0) {
            for (ChatFormat format : values()) {
                BY_NAME.put(format.name, format);
            }
        }

        return BY_NAME.get(name.toLowerCase());
    }

    public String getName() {
        return name;
    }

    public char getCode() {
        return code;
    }
}
