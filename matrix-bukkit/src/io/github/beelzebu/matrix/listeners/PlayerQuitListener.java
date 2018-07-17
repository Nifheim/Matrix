package io.github.beelzebu.matrix.listeners;

import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Main plugin;
    private final MatrixAPI core = Matrix.getAPI();

    public PlayerQuitListener(Main main) {
        plugin = main;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> { // TODO: stats
            core.getPlayer(p.getUniqueId()).save();
            /*
            core.getRedis().saveStats(core.getPlayer(e.getPlayer().getUniqueId()), core.getServerInfo().getServerName(), core.getServerInfo().getServerType(), new Statistics(p.getStatistic(Statistic.PLAYER_KILLS), p.getStatistic(Statistic.MOB_KILLS), p.getStatistic(Statistic.DEATHS), StatsListener.getBroken().get(p) == null ? 0 : StatsListener.getBroken().get(p), StatsListener.getPlaced().get(p) == null ? 0 : StatsListener.getPlaced().get(p), System.currentTimeMillis()));
            if (StatsListener.getBroken().containsKey(e.getPlayer())) {
                StatsListener.getBroken().remove(e.getPlayer());
            }
            if (StatsListener.getPlaced().containsKey(e.getPlayer())) {
                StatsListener.getPlaced().remove(e.getPlayer());
            }
            */
        });
    }
}
