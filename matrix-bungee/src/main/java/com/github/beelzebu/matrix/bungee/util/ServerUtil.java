package com.github.beelzebu.matrix.bungee.util;

import com.github.beelzebu.matrix.server.ServerInfoImpl;
import java.util.Objects;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class ServerUtil {

    public static @Nullable ServerInfo getRandomLobby() {
        return getRandomLobby(null);
    }

    public static @Nullable ServerInfo getRandomLobby(@Nullable String exclude) {
        for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            if (exclude != null) {
                if (Objects.equals(serverInfo.getName(), exclude)) {
                    continue;
                }
            }
            if (!Objects.equals(serverInfo.getName().toLowerCase(), "lobby") && serverInfo.getName().startsWith(ServerInfoImpl.MAIN_LOBBY_GROUP)) {
                return serverInfo;
            }
        }
        return null;
    }
}
