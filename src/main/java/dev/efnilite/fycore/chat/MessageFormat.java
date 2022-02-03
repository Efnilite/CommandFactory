package dev.efnilite.fycore.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormat {

    private static final Pattern TAGS_PATTERN = Pattern.compile("<(\\S+?)>");
    private static final Pattern FORMAT_TAG_PATTERN = Pattern.compile("<([a-z]+)>");
    private static final Pattern COLOUR_TAG_PATTERN = Pattern.compile("<([a-z_]+)>");
    private static final Pattern HEX_TAG_PATTERN = Pattern.compile("<(#[a-fA-F0-9]{6})>");

    private static BaseComponent[] split(@NotNull String message) {
        message = ChatColor.translateAlternateColorCodes('&', message); // supports default &

        String remaining = message; // get components by eliminating them one by one
        List<String> components = new ArrayList<>();

        while (remaining.length() > 0) {
            String behind = TAGS_PATTERN.split(remaining, 2)[0]; // if there is a tag get the text in front
            remaining = remaining.substring(behind.length()); // remaining removes front text

            Matcher matcher = TAGS_PATTERN.matcher(remaining);
            String part;
            if (matcher.find()) { // if there is a tag, add the text in front and seperate tag
                part = matcher.group();
                components.add(behind);
            } else {  // if there is not a tag, just add the entire text until a new tag is found
                part = behind;
            }

            if (remaining.length() > part.length()) { // prevents substrings of -3, etc
                remaining = remaining.substring(part.length());
            } else {
                remaining = "";
            }

            components.add(part);
        }

        ComponentBuilder builder = new ComponentBuilder(); // builds all components one by one
        for (String c : components) {
            parseComponent(c, builder);
        }

        return null;
    }

    private static void parseComponent(@NotNull String string, @NotNull ComponentBuilder builder) {
        boolean isTag = string.startsWith("<") && string.endsWith(">");

        if (isTag) { // only check for formatting if tag is found
            boolean isKeyValue = string.contains(":");
            boolean isClosingTag = string.contains("/");
            String component = string.replaceAll("[/<>]", "");

            if (isKeyValue) { // if the tag contains a key and value
                String[] values = component.split(":");

                String key = values[0];
                String value = values[1];

                switch (key) {
                    case "fade":
                        builder.
                }
                return;
            }

            if (ChatFormat.getByName(string) != null) {
                parseFormat(component, isClosingTag, builder);
            } else if (ChatColour.getByName(string) != null) {
                builder.color(ChatColor.of(ChatColour.getByName(component).getHex()));
            }
        } else {
            builder.append(string); // if it isn't a tag just add the text
        }
    }

    /**
     * Parses a singular format
     *
     * @param   string
     *          The format string
     *
     * @param   current
     *          The current ComponentBuilder instance
     */
    private static void parseFormat(@NotNull String string, boolean isClosingTag, @NotNull ComponentBuilder current) {
        String tag = string.replace("/", "");
        isClosingTag = !isClosingTag;

        if (tag.equals(ChatFormat.OBFUSCATED.getName())) {
            current.obfuscated(isClosingTag);
        } else if (tag.equals(ChatFormat.BOLD.getName())) {
            current.bold(isClosingTag);
        } else if (tag.equals(ChatFormat.STRIKETHROUGH.getName())) {
            current.strikethrough(isClosingTag);
        } else if (tag.equals(ChatFormat.UNDERLINE.getName())) {
            current.underlined(isClosingTag);
        } else if (tag.equals(ChatFormat.ITALIC.getName())) {
            current.italic(isClosingTag);
        }
    }

    private static String formatFade(@NotNull String message) {
        Matcher matcher = HEX_TAG_PATTERN.matcher(message);

        while (matcher.find()) {
            String group = matcher.group(1); // inner group
            message = message.replace(matcher.group(), group); // outer group, todo add chatcolor.of
        }

        return message;
    }

    private static String formatHex(@NotNull String message) {
        Matcher matcher = HEX_TAG_PATTERN.matcher(message);

        while (matcher.find()) {
            String group = matcher.group(1); // inner group
            message = message.replace(matcher.group(), group); // outer group, todo add chatcolor.of
        }

        return message;
    }

    private static String formatColours(@NotNull String message) {
        Matcher matcher = COLOUR_TAG_PATTERN.matcher(message);

        while (matcher.find()) {
            String group = matcher.group(); // inner group
            ChatColour colour = ChatColour.getByName(group.replaceAll("[<>]", ""));
            if (colour != null) {
                message = message.replace(matcher.group(), "#" + colour.getHex()); // outer group, todo add chatcolor.of
            }
        }

        return message;
    }

    /**
     * Formats the formats
     *
     * @param   message
     *          The message
     *
     * @return the string but formatted with formats
     */
    private static String formatFormats(@NotNull String message) {
        Matcher matcher = FORMAT_TAG_PATTERN.matcher(message);

        while (matcher.find()) {
            String group = matcher.group(1); // inner group
            ChatFormat format = ChatFormat.getByName(group.replaceAll("[<>]", ""));
            if (format != null) {
                message = message.replace(matcher.group(), "&" + format.getCode()); // outer group, todo add chatcolor
            }
        }

        return message;
    }

}