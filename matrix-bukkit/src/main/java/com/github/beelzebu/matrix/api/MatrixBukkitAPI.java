package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.bukkit.player.BukkitPlayerManager;
import com.github.beelzebu.matrix.bukkit.plugin.MatrixPluginBukkit;
import com.github.beelzebu.matrix.bukkit.util.BukkitMetaInjector;
import com.github.beelzebu.matrix.player.AbstractPlayerManager;
import com.github.beelzebu.matrix.util.MetaInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixAPIImpl<Player> {

    private final BukkitMetaInjector bukkitMetaInjector;
    private final BukkitPlayerManager playerManager;

    public MatrixBukkitAPI(MatrixPluginBukkit plugin) {
        super(plugin);
        this.playerManager = new BukkitPlayerManager(this);
        bukkitMetaInjector = new BukkitMetaInjector(this);
        plugin.setApi(this);
    }

    @Override
    public AbstractPlayerManager<Player> getPlayerManager() {
        return playerManager;
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && Bukkit.getPlayer(player.getUniqueId()).hasPermission(permission);
    }

    @Override
    public MetaInjector<Player> getMetaInjector() {
        return bukkitMetaInjector;
    }

    @Override
    public MatrixPluginBukkit getPlugin() {
        return (MatrixPluginBukkit) super.getPlugin();
    }
}
