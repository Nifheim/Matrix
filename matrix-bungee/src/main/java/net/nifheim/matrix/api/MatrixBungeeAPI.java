package net.nifheim.matrix.api;

import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerType;
import net.nifheim.matrix.bungee.player.BungeePlayerManager;
import net.nifheim.matrix.bungee.plugin.MatrixPluginBungee;
import net.nifheim.matrix.bungee.util.BungeeMetaInjector;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.server.ServerInfoImpl;
import com.github.games647.craftapi.resolver.MojangResolver;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class MatrixBungeeAPI extends MatrixCommon {

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
