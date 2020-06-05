package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import org.bukkit.Bukkit;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixAPIImpl {

    MatrixBukkitAPI(MatrixPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && Bukkit.getPlayer(player.getUniqueId()).hasPermission(permission);
    }
}
