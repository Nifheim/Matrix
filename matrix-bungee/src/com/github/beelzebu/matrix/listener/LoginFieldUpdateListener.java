package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import java.util.Objects;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Beelzebu
 */
public class LoginFieldUpdateListener implements RedisMessageListener<FieldUpdate> {

    public LoginFieldUpdateListener(MatrixBungeeBootstrap bootstrap) {
    }

    @Override
    public void onMessage(FieldUpdate message) {
        if (message.getField().equalsIgnoreCase("loggedIn")) {
            ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(message.getPlayer());
            boolean value = Matrix.GSON.fromJson(message.getJsonValue(), boolean.class);
            if (!value) {
                return;
            }
            if (proxiedPlayer != null && proxiedPlayer.isConnected()) {
                proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo(getRandomLobby()));
            }
        }
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.FIELD_UPDATE;
    }

    private String getRandomLobby() {
        for (String serverName : ProxyServer.getInstance().getServers().keySet()) {
            serverName = serverName.toLowerCase();
            if (!Objects.equals(serverName, "lobby") && serverName.startsWith("lobby")) {
                return serverName;
            }
        }
        return null;
    }
}
