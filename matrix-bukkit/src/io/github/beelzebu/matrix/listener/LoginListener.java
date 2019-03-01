package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.api.Matrix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Beelzebu
 */
public class LoginListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Matrix.getAPI().getPlayers().add(Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Matrix.getAPI().getPlayers().remove(Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId()));
    }
}
