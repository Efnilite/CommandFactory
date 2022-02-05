package dev.efnilite.fycore.chat.tag;

import dev.efnilite.fycore.chat.tag.paired.GradientTag;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Super class for text format tags
 */
public abstract class TextTag {

    /**
     * The paragraph symbol, aka the colour symbol in Minecraft
     */
    protected final char COLOUR_CHAR = '\u00A7';

    /**
     * The default tag pattern
     */
    protected Pattern pattern = Pattern.compile("<(\\S+?)>");

    /**
     * Applies an instance of a tag to a message
     *
     * @param   message
     *          The message
     *
     * @return the message but tags applied
     */
    public abstract String apply(String message);

    /**
     * Parses a message with the current available tags.
     *
     * @param   message
     *          The message
     *
     * @return the message but tags parsed
     */
    public static String parse(@NotNull String message) {
        if (message.length() == 0) {
            return message;
        }

        String result = ChatColor.translateAlternateColorCodes('&', message); // support default colour codes

        result = new ColourTag().apply(result);
        result = new FormatTag().apply(result);
        result = new GradientTag().apply(result);

        return result;
    }

}
