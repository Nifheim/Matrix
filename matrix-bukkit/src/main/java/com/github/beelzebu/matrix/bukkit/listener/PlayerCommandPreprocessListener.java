package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.bukkit.command.staff.CommandWatcherCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandPreprocessListener implements Listener {

    private final MatrixBukkitAPI api;

    public PlayerCommandPreprocessListener(MatrixBukkitAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission("matrix.admin")) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            api.getPlayerManager().getPlayer(player).thenAccept(matrixPlayer -> {
                if (!matrixPlayer.isWatcher()) {
                    return;
                }
                if (!player.hasPermission(CommandWatcherCommand.PERMISSION)) {
                    matrixPlayer.setWatcher(false);
                    return;
                }
                if (!matrixPlayer.isLoggedIn()) {
                    return;
                }
                player.sendMessage(I18n.tl(Message.CHAT_COMMAND_WATCHER_FORMAT, matrixPlayer.getLastLocale()).replace("%player%", e.getPlayer().getName()).replace("%msg%", e.getMessage()));
            });
        }
    }
}
