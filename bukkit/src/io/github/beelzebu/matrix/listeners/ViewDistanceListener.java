package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ViewDistanceListener implements Listener {

    private final Main plugin;

    public ViewDistanceListener(Main main) {
        plugin = main;
    }

    public static int getViewDistance(Player p) {
        if (true) {
            return 5;
        }
        if (p.hasPermission("matrix.staff")) {
            return 10;
        } else if (p.hasPermission("matrix.vip.king")) {
            return 10;
        } else if (p.hasPermission("matrix.vip.prince")) {
            return 9;
        } else if (p.hasPermission("matrix.vip.duke")) {
            return 8;
        } else if (p.hasPermission("matrix.vip.sir")) {
            return 7;
        } else if (p.hasPermission("matrix.vip.count")) {
            return 6;
        }
        return Bukkit.getViewDistance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
        }, 60L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
    }
}
