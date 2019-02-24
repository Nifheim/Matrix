package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
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

    private final MatrixBukkitBootstrap plugin;
    private final MatrixAPI core = Matrix.getAPI();
    private final Set<Player> cant = new HashSet<>();

    public DupepatchListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
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
