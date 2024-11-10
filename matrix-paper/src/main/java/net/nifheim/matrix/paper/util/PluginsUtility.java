package net.nifheim.matrix.paper.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class PluginsUtility {

    private final String[] pluginNames = {
            "NoCheatPlus",
            "40ServidoresMC",
            "IPWhiteList",
            "LuckPerms-GUI",
            "ChangeSlots",
            "Skript"
    };

    private final Logger logger;

    public PluginsUtility(Logger logger) {
        this.logger = logger;
    }

    public void checkForPluginsToRemove() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            for (String name : pluginNames) {
                if (plugin.getName().equalsIgnoreCase(name)) {
                    logger.warn("Plugin {} is not compatible with Matrix, disabling it...", plugin.getName());
                    Bukkit.getPluginManager().disablePlugin(plugin);
                }
            }
        }
    }
}
