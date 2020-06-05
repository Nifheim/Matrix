package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.scheduler.BukkitSchedulerAdapter;
import org.bukkit.Bukkit;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixAPIImpl {

    private final BukkitSchedulerAdapter schedulerAdapter;

    MatrixBukkitAPI(MatrixPlugin plugin, MatrixBukkitBootstrap bootstrap) {
        super(plugin);
        this.schedulerAdapter = new BukkitSchedulerAdapter(bootstrap);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && Bukkit.getPlayer(player.getUniqueId()).hasPermission(permission);
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return schedulerAdapter;
    }
}
