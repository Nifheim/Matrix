package com.github.beelzebu.matrix.bukkit.listener.stats;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.server.ServerType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Beelzebu
 */
public class StatsListener implements Listener {

    private final MatrixBukkitAPI api;
    private final Map<UUID, Long> kills = new HashMap<>();
    private final Map<UUID, Long> mobKills = new HashMap<>();
    private final Map<UUID, Long> deaths = new HashMap<>();
    private final Map<UUID, Long> placed = new HashMap<>();
    private final Map<UUID, Long> broken = new HashMap<>();

    public StatsListener(MatrixBukkitAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKill(PlayerDeathEvent e) {
        deaths.put(e.getEntity().getUniqueId(), deaths.getOrDefault(e.getEntity().getUniqueId(), 0L) + 1);
        if (e.getEntity().getKiller() == null) {
            return;
        }
        kills.put(e.getEntity().getKiller().getUniqueId(), kills.getOrDefault(e.getEntity().getKiller().getUniqueId(), 0L) + 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKill(EntityDeathEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            return;
        }
        if (e.getEntity().getKiller() == null) {
            return;
        }
        mobKills.put(e.getEntity().getKiller().getUniqueId(), mobKills.getOrDefault(e.getEntity().getKiller().getUniqueId(), 0L) + 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        placed.put(e.getPlayer().getUniqueId(), placed.getOrDefault(e.getPlayer().getUniqueId(), 0L) + 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        broken.put(e.getPlayer().getUniqueId(), broken.getOrDefault(e.getPlayer().getUniqueId(), 0L) + 1);

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (api.getServerInfo().getServerType() == ServerType.LOBBY || api.getServerInfo().getServerType() == ServerType.AUTH) {
            return;
        }
        api.getPlayerManager().getPlayer(e.getPlayer()).thenAccept(matrixPlayer -> {
            if (matrixPlayer == null) {
                return;
            }
            Map<Statistic, Long> stats = new HashMap<>();
            stats.put(Statistic.KILLS, kills.getOrDefault(matrixPlayer.getUniqueId(), 0L));
            stats.put(Statistic.MOB_KILLS, mobKills.getOrDefault(matrixPlayer.getUniqueId(), 0L));
            stats.put(Statistic.DEATHS, deaths.getOrDefault(matrixPlayer.getUniqueId(), 0L));
            stats.put(Statistic.BLOCKS_BROKEN, broken.getOrDefault(matrixPlayer.getUniqueId(), 0L));
            stats.put(Statistic.BLOCKS_PLACED, placed.getOrDefault(matrixPlayer.getUniqueId(), 0L));
            api.getDatabase().incrStatsById(matrixPlayer.getId(), api.getServerInfo().getGroupName(), stats);
        });

    }
}
