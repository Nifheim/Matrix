package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import net.md_5.bungee.api.ProxyServer;

/**
 * @author Beelzebu
 */
public class MatrixBungeeAPI extends MatrixCommonAPI {

    MatrixBungeeAPI(MatrixPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && ProxyServer.getInstance().getPlayer(player.getUniqueId()).hasPermission(permission);
    }
}
