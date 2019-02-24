package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI core = Matrix.getAPI();

    public PlayerCommandPreprocessListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!e.getPlayer().hasPermission("matrix.admin")) {
                Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("matrix.mod")).filter(p -> Matrix.getAPI().getPlayer(p.getUniqueId()).isWatcher()).forEach(p -> p.sendMessage((core.getString("Chat.Command Watcher.Format", p.getLocale())).replaceAll("%player%", e.getPlayer().getName()).replaceAll("%msg%", e.getMessage())));
            }
        });
    }
}
