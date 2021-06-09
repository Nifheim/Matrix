package com.github.beelzebu.matrix.bungee.messaging.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.bungee.util.ServerUtil;
import com.github.beelzebu.matrix.messaging.message.FieldUpdateMessage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

/**
 * @author Beelzebu
 */
@Deprecated
public class LoginFieldUpdateListener extends MessageListener { // TODO: move to auth

    private final MatrixBungeeBootstrap bootstrap;

    public LoginFieldUpdateListener(MatrixBungeeBootstrap bootstrap) {
        super(MessageType.FIELD_UPDATE);
        this.bootstrap = bootstrap;
    }

    @Override
    public void onMessage(Message message) {
        if (FieldUpdateMessage.getField(message).equalsIgnoreCase("loggedIn")) {
            ProxiedPlayer proxiedPlayer = bootstrap.getApi().getPlayerManager().getPlatformPlayerById(FieldUpdateMessage.getPlayerId(message));
            boolean value = FieldUpdateMessage.getValue(message, boolean.class);
            if (!value) {
                return;
            }
            if (proxiedPlayer != null && proxiedPlayer.isConnected()) {
                proxiedPlayer.connect(ServerUtil.getRandomLobby(), ServerConnectEvent.Reason.LOBBY_FALLBACK);
            }
        }
    }
}
