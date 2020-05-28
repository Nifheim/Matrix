package io.github.beelzebu.matrix.util;

import io.github.beelzebu.matrix.api.Matrix;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class PluginsUtility {

    private final String[] pluginNames = {"TAB", "ClearLag", "Spartan", "NoCheatPlus"};

    public PluginsUtility() {

    }

    public void checkForPluginsToRemove() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            for (String name : pluginNames) {
                if (plugin.getName().equalsIgnoreCase(name)) {
                    Matrix.getLogger().info("&4" + plugin.getName() + "&7 should be removed.");
                }
            }
        }
    }
}
