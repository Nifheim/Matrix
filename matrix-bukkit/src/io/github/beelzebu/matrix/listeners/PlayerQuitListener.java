package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.MatrixBukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    public PlayerQuitListener(MatrixBukkit matrixBukkit) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }
}
