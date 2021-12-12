package com.github.beelzebu.matrix.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class PluginsUtility {

    private final String[] pluginNames = {
            "Spartan",
            "NoCheatPlus",
            "40ServidoresMC",
            "IPWhiteList",
            "LuckPerms-GUI",
            "ChangeSlots",
            "Skript"
    };

    public PluginsUtility() {
    }

    public void checkForPluginsToRemove() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            for (String name : pluginNames) {
                if (plugin.getName().equalsIgnoreCase(name)) {
                    Bukkit.getPluginManager().disablePlugin(plugin);
                }
            }
        }
    }
}
