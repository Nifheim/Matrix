package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class StatsListener implements Listener {

    private static final Map<UUID, Integer> placed = new HashMap<>();
    private static final Map<UUID, Integer> broken = new HashMap<>();
    private final MatrixAPI api = Matrix.getAPI();

    public static Map<UUID, Integer> getPlaced() {
        return StatsListener.placed;
    }

    public static Map<UUID, Integer> getBroken() {
        return StatsListener.broken;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.isCancelled()) {
            placed.put(e.getPlayer().getUniqueId(), placed.getOrDefault(e.getPlayer().getUniqueId(), 0) + 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.isCancelled()) {
            broken.put(e.getPlayer().getUniqueId(), broken.getOrDefault(e.getPlayer().getUniqueId(), 0) + 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlyChange(PlayerToggleFlightEvent e) {
        if (e.isCancelled()) {
            return;
        }
        api.getPlayer(e.getPlayer().getUniqueId()).setOption(PlayerOptionType.FLY, e.getPlayer().getAllowFlight());
    }
}
