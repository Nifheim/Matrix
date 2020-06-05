package com.github.beelzebu.matrix.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class StatsListener implements Listener {

    private static final Map<UUID, Integer> placed = new HashMap<>();
    private static final Map<UUID, Integer> broken = new HashMap<>();

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
}
