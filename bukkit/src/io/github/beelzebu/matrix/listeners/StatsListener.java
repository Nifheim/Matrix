package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class StatsListener implements Listener {

    @Getter
    private static final Map<Player, Integer> placed = new HashMap<>();
    @Getter
    private static final Map<Player, Integer> broken = new HashMap<>();
    private final MatrixAPI api = Matrix.getAPI();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.isCancelled()) {
            if (placed.containsKey(e.getPlayer())) {
                placed.replace(e.getPlayer(), placed.get(e.getPlayer()), placed.get(e.getPlayer()) + 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.isCancelled()) {
            if (broken.containsKey(e.getPlayer())) {
                broken.replace(e.getPlayer(), broken.get(e.getPlayer()), broken.get(e.getPlayer()) + 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlyChange(PlayerToggleFlightEvent e) {
        api.getPlayer(e.getPlayer().getUniqueId()).setOption(PlayerOptionType.FLY, e.getPlayer().getAllowFlight());
    }
}
