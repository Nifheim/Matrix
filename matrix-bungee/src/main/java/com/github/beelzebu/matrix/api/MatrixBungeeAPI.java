package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.bungee.player.BungeePlayerManager;
import com.github.beelzebu.matrix.bungee.plugin.MatrixPluginBungee;
import com.github.beelzebu.matrix.bungee.util.BungeeMetaInjector;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Beelzebu
 */
public class MatrixBungeeAPI extends MatrixAPIImpl<ProxiedPlayer> {

    public static ServerInfo BUNGEE_SERVER_INFO;
    private final BungeeMetaInjector metaInjector;
    private final BungeePlayerManager bungeePlayerManager;

    public MatrixBungeeAPI(MatrixPluginBungee plugin) {
        super(plugin);
        metaInjector = new BungeeMetaInjector(this);
        plugin.setApi(this);
        bungeePlayerManager = new BungeePlayerManager(this);
        BUNGEE_SERVER_INFO = new ServerInfoImpl(ServerType.PROXY, ServerInfoImpl.PROXY_GROUP, null, null, false, null, false);
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

    @Override
    public ServerInfo getServerInfo() {
        return BUNGEE_SERVER_INFO;
    }
}
