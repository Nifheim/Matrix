package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.Titles;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI core = Matrix.getAPI();

    public PlayerDeathListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        int stay = plugin.getConfig().getInt("Death Titles.Stay", 60);
        Player p = e.getEntity();
        try {
            Bukkit.getScheduler().runTaskLater(plugin, () -> p.spigot().respawn(), 1L);
        } catch (Exception ignore) { // Doesn't work in 1.8 or earlier
        }
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!plugin.getConfig().getString("Death Titles.Mode", "Disabled").equalsIgnoreCase("disabled")) {
                if (plugin.getConfig().getString("Death Titles.Mode").equalsIgnoreCase("pvp") && p.getKiller() != null) {
                    Titles.sendTitle(p, 30, stay, 30, core.getString("Death Titles.Title", p.getLocale()), core.getString("Death Titles.Subtitle", p.getLocale()));
                } else {
                    Titles.sendTitle(p, 30, stay, 30, core.getString("Death Titles.Title", p.getLocale()), core.getString("Death Titles.Subtitle", p.getLocale()));
                }
            }
        }, 10L);
    }
}
