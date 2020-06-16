package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI api = Matrix.getAPI();

    public PlayerCommandPreprocessListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!e.getPlayer().hasPermission("matrix.admin")) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                MatrixPlayer matrixPlayer = api.getPlayer(player.getUniqueId());
                if (!matrixPlayer.isWatcher()) {
                    continue;
                }
                player.sendMessage(I18n.tl(Message.CHAT_COMMAND_WATCHER_FORMAT, matrixPlayer.getLastLocale()).replace("%player%", e.getPlayer().getName()).replace("%msg%", e.getMessage()));
            }
        });
    }
}
