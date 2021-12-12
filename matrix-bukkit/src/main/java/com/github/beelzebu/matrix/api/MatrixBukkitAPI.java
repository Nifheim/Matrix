package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.bukkit.player.BukkitPlayerManager;
import com.github.beelzebu.matrix.bukkit.plugin.MatrixPluginBukkit;
import com.github.beelzebu.matrix.bukkit.util.BukkitMetaInjector;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixAPIImpl {

    private final @NotNull BukkitPlayerManager playerManager;

    public MatrixBukkitAPI(@NotNull MatrixPluginBukkit plugin) {
        super(plugin);
        this.playerManager = new BukkitPlayerManager(this, new BukkitMetaInjector(this));
        plugin.setApi(this);
    }

    @Override
    public @NotNull MatrixPluginBukkit getPlugin() {
        return (MatrixPluginBukkit) super.getPlugin();
    }

    @Override
    public @NotNull BukkitPlayerManager getPlayerManager() {
        return playerManager;
    }
}
