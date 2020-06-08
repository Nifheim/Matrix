package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.MatrixAPIImpl;
import com.github.beelzebu.matrix.MatrixPluginBukkit;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import org.bukkit.Bukkit;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixAPIImpl {


    public MatrixBukkitAPI(MatrixPluginBukkit plugin) {
        super(plugin);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && Bukkit.getPlayer(player.getUniqueId()).hasPermission(permission);
    }
}
