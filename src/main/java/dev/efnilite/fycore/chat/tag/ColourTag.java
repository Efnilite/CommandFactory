package dev.efnilite.fycore.chat.tag;

import dev.efnilite.fycore.chat.ChatColour;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tag for colours.
 * Priority should be first.
 */
public class ColourTag extends TextTag {

    private final Pattern HEX_PATTERN = Pattern.compile("<#([0-9a-fA-F]{6})>");

    @Override
    public String apply(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        String result = message;

        // Match hex colours
        while (matcher.find()) {
            String full = matcher.group();
            String hex = matcher.group(1);

            result = result.replace(full, ChatColor.of("#" + hex).toString());
        }

        matcher = pattern.matcher(result);

        // Default color second
        while (matcher.find()) {
            String full = matcher.group();
            String colour = matcher.group(1);

            ChatColour defColour = ChatColour.getByName(colour);
            if (defColour == null) {
                continue;
            }

            result = result.replace(full, ChatColor.of("#" + defColour.getHex()).toString());
        }

        return result;
    }
}
