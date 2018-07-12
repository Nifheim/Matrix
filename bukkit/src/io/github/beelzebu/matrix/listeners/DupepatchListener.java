package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

public class DupepatchListener implements Listener {

    private final Main plugin;
    private final MatrixAPI core = MatrixAPI.getInstance();
    private final Set<Player> cant = new HashSet<>();

    public DupepatchListener(Main main) {
        plugin = main;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortal(EntityPortalEvent e) {
        if (!e.isCancelled()) {
            if (plugin.getConfig().getBoolean("PortalFix.Strict Mode")) {
                if (!e.getEntityType().equals(EntityType.PLAYER)) {
                    e.setCancelled(true);
                }
            } else if (e.getEntityType().equals(EntityType.EXPERIENCE_ORB)) {
                e.setCancelled(true);
            }
        }
    }
}
