package com.github.beelzebu.matrix.api.util;

import java.util.List;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public final class StringUtils {


    /**
     * Add colors to the message and replace default placeholders.
     *
     * @param message string to replace color codes and placeholders.
     * @return message with colors and replaced placeholders.
     */
    public static String replace(String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", "&a&lMatrix &8&l>&7"));
    }

    /**
     * Add colors to the messages and replace default placeholders.
     *
     * @param messages strings to replace color codes and placeholders.
     * @return messages with colors and replaced placeholders.
     */
    public static List<String> replace(List<String> messages) {
        for (int i = 0; i < messages.size(); i++) {
            messages.set(i, replace(messages.get(i)));
        }
        return messages;
    }

    /**
     * Add colors to the messages and replace default placeholders.
     *
     * @param messages strings to replace color codes and placeholders.
     * @return messages with colors and replaced placeholders.
     */
    public static String[] replace(String[] messages) {
        for (int i = 0; i < messages.length; i++) {
            messages[i] = replace(messages[i]);
        }
        return messages;
    }

    /**
     * Remove colors from a message and "Debug: "
     *
     * @param message message to remove colors.
     * @return a plain message without colors.
     */
    public static String removeColor(String message) {
        return ChatColor.stripColor(message).replaceAll("Debug: ", "");
    }

}
