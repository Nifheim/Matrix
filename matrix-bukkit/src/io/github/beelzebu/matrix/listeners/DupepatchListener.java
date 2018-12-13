package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.MatrixBukkit;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

public class DupepatchListener implements Listener {

    private final MatrixBukkit plugin;
    private final MatrixAPI core = Matrix.getAPI();
    private final Set<Player> cant = new HashSet<>();

    public DupepatchListener(MatrixBukkit matrixBukkit) {
        plugin = matrixBukkit;
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
