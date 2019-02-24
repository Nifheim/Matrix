package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.server.ServerType;
import java.util.concurrent.ExecutionException;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ViewDistanceListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public ViewDistanceListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    public static int getViewDistance(Player p) {
        if (Matrix.getAPI().getServerInfo().getServerType() == ServerType.SURVIVAL) {
            LuckPermsApi luckPermsApi = LuckPerms.getApi();
            try {
                luckPermsApi.getStorage().loadUser(p.getUniqueId()).get();
                User user = luckPermsApi.getUser(p.getUniqueId());
                if (user != null) {
                    if (user.getCachedData().getMetaData(Contexts.global()).getMeta().containsKey("view-distance")) {
                        return 32;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
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
            e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
        }, 60L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        e.getPlayer().setViewDistance(getViewDistance(e.getPlayer()));
    }
}
