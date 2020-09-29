package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
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
            if (plugin.getConfig().getString("Death Titles.Mode", "Disabled").equalsIgnoreCase("disabled")) {
                return;
            }
            if (plugin.getConfig().getString("Death Titles.Mode").equalsIgnoreCase("pvp")) {
                if (p.getKiller() == null) {
                    return;
                }
            }
            p.sendTitle(I18n.tl(Message.DEATH_TITLES_TITLE, core.getPlayer(p.getUniqueId()).getLastLocale()), I18n.tl(Message.DEATH_TITLES_SUBTITLE, core.getPlayer(p.getUniqueId()).getLastLocale()), 30, stay, 30);
        }, 10L);
    }
}
