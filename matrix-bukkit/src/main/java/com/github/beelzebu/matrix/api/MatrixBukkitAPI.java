package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.bukkit.player.BukkitPlayerManager;
import com.github.beelzebu.matrix.bukkit.plugin.MatrixPluginBukkit;
import com.github.beelzebu.matrix.bukkit.util.BukkitMetaInjector;
import com.github.beelzebu.matrix.player.AbstractPlayerManager;
import com.github.beelzebu.matrix.util.MetaInjector;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixAPIImpl {

    private final BukkitMetaInjector bukkitMetaInjector;
    private final BukkitPlayerManager playerManager;

    public MatrixBukkitAPI(MatrixPluginBukkit plugin) {
        super(plugin);
        this.playerManager = new BukkitPlayerManager(this);
        bukkitMetaInjector = new BukkitMetaInjector(this);
        plugin.setApi(this);
    }

    @Override
    public MatrixPluginBukkit getPlugin() {
        return (MatrixPluginBukkit) super.getPlugin();
    }

    @Override
    public AbstractPlayerManager<Player> getPlayerManager() {
        return playerManager;
    }

    @Override
    public MetaInjector<Player> getMetaInjector() {
        return bukkitMetaInjector;
    }
}
