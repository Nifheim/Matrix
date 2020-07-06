package com.github.beelzebu.matrix.tablist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
public final class TablistManager {

    public final static BaseComponent[] TAB_HEADER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
            "&7¡Jugando en &6IndioPikaro&7!\n" +
                    "\n" +
                    "&7Vota con &6/vote\n" +
                    "&7IP: &amc.indiopikaro.cl\n"));
    public final static BaseComponent[] TAB_FOOTER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
            "\n" +
                    "&7Tienda: &eindiopikaro.cl/tienda &7Twitter: &e@IndioPikaroMc\n" +
                    "&7Discord: &ediscord.gg/GXcsFU9 &7Web: &eindiopikaro.cl"));
}
