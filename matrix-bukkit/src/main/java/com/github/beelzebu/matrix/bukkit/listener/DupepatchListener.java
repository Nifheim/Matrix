package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.jetbrains.annotations.NotNull;

public class DupepatchListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public DupepatchListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPortal(@NotNull EntityPortalEvent e) {
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
