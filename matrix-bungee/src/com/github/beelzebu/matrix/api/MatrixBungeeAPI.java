package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.MatrixAPIImpl;
import com.github.beelzebu.matrix.MatrixPluginBungee;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import net.md_5.bungee.api.ProxyServer;

/**
 * @author Beelzebu
 */
public class MatrixBungeeAPI extends MatrixAPIImpl {

    public MatrixBungeeAPI(MatrixPluginBungee plugin) {
        super(plugin);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && ProxyServer.getInstance().getPlayer(player.getUniqueId()).hasPermission(permission);
    }
}
