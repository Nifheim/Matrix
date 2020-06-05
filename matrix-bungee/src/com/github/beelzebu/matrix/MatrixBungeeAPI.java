package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.scheduler.BungeeSchedulerAdapter;
import net.md_5.bungee.api.ProxyServer;

/**
 * @author Beelzebu
 */
public class MatrixBungeeAPI extends MatrixAPIImpl {

    private final BungeeSchedulerAdapter schedulerAdapter;

    MatrixBungeeAPI(MatrixPlugin plugin, MatrixBungeeBootstrap bootstrap) {
        super(plugin);
        this.schedulerAdapter = new BungeeSchedulerAdapter(bootstrap);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && ProxyServer.getInstance().getPlayer(player.getUniqueId()).hasPermission(permission);
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return schedulerAdapter;
    }
}
