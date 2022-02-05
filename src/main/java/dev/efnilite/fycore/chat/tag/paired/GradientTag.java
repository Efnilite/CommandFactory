package dev.efnilite.fycore.chat.tag.paired;

import dev.efnilite.fycore.chat.tag.TextTag;
import dev.efnilite.fycore.util.Logging;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tag for gradients.
 * Priority should be last to add support for formatting between the gradients.
 */
public class GradientTag extends TextTag {

    public GradientTag() {
        /*
         * The pattern for the fade tag
         * Features 3 groups:
         * 1: the starting colour
         * 2: the text
         * 3: the closing colour
         */
        pattern = Pattern.compile("<gradient:(#[0-9a-fA-F]{6})>(.*)</gradient:(#[0-9a-fA-F]{6})>");
    }

    @Override
    public String apply(String message) {
        Matcher matcher = pattern.matcher(message);
        String result = message;

        while (matcher.find()) {
            String full = matcher.group();
            String startHex = matcher.group(1);
            String betweenText = matcher.group(2);
            String closeHex = matcher.group(3);

            String coloured = getGradients(betweenText, startHex, closeHex);
            result = result.replace(full, coloured);
        }

        return result;
    }

    /*
     * Applies the gradients to the message between the tags
     */
    private String getGradients(String message, String startHex, String stopHex) {
        double[] startRgb = fromHex(startHex);
        double[] closeRgb = fromHex(stopHex);

        double r = startRgb[0];
        double g = startRgb[1];
        double b = startRgb[2];

        double incrementR = (closeRgb[0] - r) / message.length(); // (end r - start r) / message length
        double incrementG = (closeRgb[1] - g) / message.length();
        double incrementB = (closeRgb[2] - b) / message.length();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) == COLOUR_CHAR || (i > 0 && message.charAt(i - 1) == COLOUR_CHAR)) { // if characters are color codes skip them
                result.append(message.charAt(i));
                continue;
            }

            String character = String.valueOf(message.charAt(i));
            String last = org.bukkit.ChatColor.getLastColors(result.toString());

            // increment rgb values
            r += incrementR;
            g += incrementG;
            b += incrementB;

            String code = toHex(r) + toHex(g) + toHex(b);

            result
                    .append(last)
                    .append(ChatColor.of("#" + code))
                    .append(character);
        }

        return result.toString();
    }

    /*
     * Returns an array of doubles (actually ints) from a hex colour.
     * Doubles are used to make division not integer-based
     */
    private double[] fromHex(String hex) {
        hex = hex.replace("#", "");

        if (hex.length() != 6) {
            throw new IllegalArgumentException("Hex code is more or less than 6");
        }

        // Hex is formatted like RRGGBB, thus parsing the corresponding part of the string works fine
        int r = Integer.parseInt(hex.substring(0, 2), 16); // 0, 1
        int g = Integer.parseInt(hex.substring(2, 4), 16); // 2, 3
        int b = Integer.parseInt(hex.substring(4, 6), 16); // 4, 5

        return new double[]{r, g, b};
    }

    private String toHex(double value) {
        String hex = Integer.toHexString((int) value);
        if (hex.length() == 1) { // to make sure length is always 2
            hex = "0" + hex;
        }

        return hex;
    }
}