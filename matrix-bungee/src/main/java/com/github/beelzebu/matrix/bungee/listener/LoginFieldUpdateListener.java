package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.bungee.util.ServerUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

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
                proxiedPlayer.connect(ServerUtil.getRandomLobby(), ServerConnectEvent.Reason.LOBBY_FALLBACK);
            }
        }
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.FIELD_UPDATE;
    }
}
