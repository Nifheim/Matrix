package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.bungee.player.BungeePlayerManager;
import com.github.beelzebu.matrix.bungee.plugin.MatrixPluginBungee;
import com.github.beelzebu.matrix.bungee.util.BungeeMetaInjector;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Beelzebu
 */
public class MatrixBungeeAPI extends MatrixAPIImpl<ProxiedPlayer> {

    private final BungeeMetaInjector metaInjector;
    private final BungeePlayerManager bungeePlayerManager;

    public MatrixBungeeAPI(MatrixPluginBungee plugin) {
        super(plugin);
        metaInjector = new BungeeMetaInjector(this);
        plugin.setApi(this);
        bungeePlayerManager = new BungeePlayerManager(this);
    }

    @Override
    public boolean hasPermission(MatrixPlayer player, String permission) {
        return getPlugin().isOnline(player.getUniqueId(), true) && ProxyServer.getInstance().getPlayer(player.getUniqueId()).hasPermission(permission);
    }

    @Override
    public BungeeMetaInjector getMetaInjector() {
        return metaInjector;
    }

    @Override
    public MatrixPluginBungee getPlugin() {
        return (MatrixPluginBungee) super.getPlugin();
    }

    @Override
    public BungeePlayerManager getPlayerManager() {
        return bungeePlayerManager;
    }
}
