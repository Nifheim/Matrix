package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.bungee.player.BungeePlayerManager;
import com.github.beelzebu.matrix.bungee.plugin.MatrixPluginBungee;
import com.github.beelzebu.matrix.bungee.util.BungeeMetaInjector;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.github.games647.craftapi.resolver.MojangResolver;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class MatrixBungeeAPI extends MatrixAPIImpl {

    public static final MojangResolver RESOLVER = new MojangResolver();
    public static ServerInfo BUNGEE_SERVER_INFO;
    private final @NotNull BungeePlayerManager bungeePlayerManager;

    public MatrixBungeeAPI(@NotNull MatrixPluginBungee plugin) throws Exception {
        super(plugin);
        plugin.setApi(this);
        bungeePlayerManager = new BungeePlayerManager(this, new BungeeMetaInjector(this));
        BUNGEE_SERVER_INFO = new ServerInfoImpl(ServerType.PROXY, ServerInfoImpl.PROXY_GROUP, null, null, false, null, false);
    }

    @Override
    public @NotNull MatrixPluginBungee getPlugin() {
        return (MatrixPluginBungee) super.getPlugin();
    }

    @Override
    public @NotNull BungeePlayerManager getPlayerManager() {
        return bungeePlayerManager;
    }

    @Override
    public @NotNull ServerInfo getServerInfo() {
        return BUNGEE_SERVER_INFO;
    }
}
