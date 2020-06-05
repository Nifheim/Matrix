package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerUnregisterMessage;
import net.md_5.bungee.api.ProxyServer;

/**
 * @author Beelzebu
 */
public class ServerUnregisterListener implements RedisMessageListener<ServerUnregisterMessage> {

    @Override
    public void onMessage(ServerUnregisterMessage message) {
        Matrix.getLogger().info("Received unregister message for server: " + message.getName());
        ProxyServer.getInstance().getServers().remove(message.getName());
        Matrix.getAPI().getCache().removeServer(message.getName());
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.SERVER_UNREGISTER;
    }
}
