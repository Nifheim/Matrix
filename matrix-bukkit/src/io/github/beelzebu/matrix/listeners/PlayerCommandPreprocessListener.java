package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.MatrixBukkit;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    private final MatrixBukkit plugin;
    private final MatrixAPI core = Matrix.getAPI();

    public PlayerCommandPreprocessListener(MatrixBukkit matrixBukkit) {
        plugin = matrixBukkit;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!e.getPlayer().hasPermission("matrix.staff.admin")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (p.hasPermission("matrix.staff.mod") && core.getPlayer(p.getUniqueId()).isWatcher()) {
                        p.sendMessage((core.getString("Chat.Command Watcher.Format", p.getLocale())).replaceAll("%player%", e.getPlayer().getName()).replaceAll("%msg%", e.getMessage()));
                    }
                });
            }
        });
    }
}
