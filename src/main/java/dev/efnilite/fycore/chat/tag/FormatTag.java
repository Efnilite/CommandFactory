package dev.efnilite.fycore.chat.tag;

import dev.efnilite.fycore.chat.ChatFormat;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for formatting tags, e.g. bold, italic, etc.
 * Can be in pairs or on its own.
 * Priority should be first.
 */
public class FormatTag extends TextTag {

    private final Pattern DOUBLE_TAG_PATTERN = Pattern.compile("<(\\S+?)>(.+)</(\\S+?)>");

    @Override
    public String apply(String message) {
        Matcher matcher = DOUBLE_TAG_PATTERN.matcher(message);
        String result = message;

        // Double tags first to prevent single tags replacing opening tag
        while (matcher.find()) {
            String full = matcher.group();
            String startTag = matcher.group(1);
            String betweenText = matcher.group(2);
            String closeTag = matcher.group(3);

            ChatFormat startFormat = ChatFormat.getByName(startTag);
            ChatFormat closeFormat = ChatFormat.getByName(closeTag);
            if (closeFormat == null || startFormat != closeFormat) { // if tags dont match it means another closing tag has been found
                continue;
            }

            result = result.replace(full,
                    ChatColor.of(startFormat.getName()) +
                    betweenText + ChatColor.of(closeFormat.getName()));
        }

        matcher = pattern.matcher(result);

        // Single tags last
        while (matcher.find()) {
            String full = matcher.group();
            String colour = matcher.group(1);

            ChatFormat defFormat = ChatFormat.getByName(colour);
            if (defFormat == null) {
                continue;
            }

            result = result.replace(full, ChatColor.of(defFormat.getName()).toString());
        }

        return result;
    }
}
