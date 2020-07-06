package com.github.beelzebu.matrix.util;

import java.util.Objects;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author Beelzebu
 */
public final class ServerUtil {

    public static ServerInfo getRandomLobby() {
        for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            if (!Objects.equals(serverInfo.getName().toLowerCase(), "lobby") && serverInfo.getName().toLowerCase().startsWith("lobby")) {
                return serverInfo;
            }
        }
        return null;
    }
}
