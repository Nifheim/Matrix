package com.github.beelzebu.matrix.bungee.util;

import java.util.Objects;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author Beelzebu
 */
public final class ServerUtil {

    public static ServerInfo getRandomLobby() {
        return getRandomLobby(null);
    }

    public static ServerInfo getRandomLobby(String exclude) {
        for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            if (exclude != null) {
                if (Objects.equals(serverInfo.getName(), exclude)) {
                    continue;
                }
            }
            if (!Objects.equals(serverInfo.getName().toLowerCase(), "lobby") && serverInfo.getName().toLowerCase().startsWith("lobby")) {
                return serverInfo;
            }
        }
        return null;
    }
}
