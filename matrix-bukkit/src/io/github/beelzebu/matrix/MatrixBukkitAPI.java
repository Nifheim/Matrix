package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import org.bukkit.Bukkit;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixCommonAPI {

    MatrixBukkitAPI(MatrixPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && Bukkit.getPlayer(player.getUniqueId()).hasPermission(permission);
    }
}
