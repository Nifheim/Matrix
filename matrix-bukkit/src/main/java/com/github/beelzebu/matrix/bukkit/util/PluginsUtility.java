package com.github.beelzebu.matrix.bukkit.util;

import com.github.beelzebu.matrix.api.Matrix;
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
                    Matrix.getLogger().warn("Plugin " + name + " has been detected, it should be removed...");
                    //Bukkit.getPluginManager().disablePlugin(plugin);
                }
            }
        }
    }
}
