package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.server.ServerType;
import java.util.concurrent.ExecutionException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ViewDistanceListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public ViewDistanceListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    public static int getViewDistance(Player p) {
        if (Matrix.getAPI().getServerInfo().getServerType() == ServerType.SURVIVAL) {
            if (p.hasPermission("matrix.mod")) {
                return 10;
            } else if (p.hasPermission("matrix.vip5")) {
                return 10;
            } else if (p.hasPermission("matrix.vip4")) {
                return 9;
            } else if (p.hasPermission("matrix.vip3")) {
                return 8;
            } else if (p.hasPermission("matrix.vip2")) {
                return 7;
            } else if (p.hasPermission("matrix.vip1")) {
                return 6;
            }
        }
        return Bukkit.getViewDistance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (e.getPlayer().isOnline()) {
                e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
            }
        }, 100L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.getPlayer().setViewDistance(2);
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (e.getTo().getY() <= 40) {
            e.getPlayer().setViewDistance(4);
        } else {
            e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
    }
}
