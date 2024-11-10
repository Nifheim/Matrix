package net.nifheim.matrix.auth.paper.listener;

import net.nifheim.matrix.auth.paper.MatrixAuth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final MatrixAuth plugin;

    public PlayerListener(MatrixAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // TODO: send title for login / register
    }
}
