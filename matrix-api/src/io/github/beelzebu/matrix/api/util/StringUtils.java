package io.github.beelzebu.matrix.api.util;

import java.util.List;
import java.util.stream.Collectors;
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
        return messages.stream().map(StringUtils::replace).collect(Collectors.toList());
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
