package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

public class DupepatchListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public DupepatchListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPortal(EntityPortalEvent e) {
        if (!e.isCancelled()) {
            if (plugin.getConfig().getBoolean("PortalFix.Strict Mode")) {
                if (e.getEntityType().equals(EntityType.PLAYER)) {
                    return;
                }
                e.setCancelled(true);
            } else {
                switch (e.getEntityType()) {
                    case EXPERIENCE_ORB:
                        
                }
            }
        }
    }
}
