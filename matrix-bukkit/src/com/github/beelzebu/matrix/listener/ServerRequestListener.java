package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.messaging.RedisMessageListener;
import cl.indiopikaro.jmatrix.api.messaging.message.RedisMessageType;
import cl.indiopikaro.jmatrix.api.messaging.message.ServerRequestMessage;

/**
 * @author Beelzebu
 */
public class ServerRequestListener implements RedisMessageListener<ServerRequestMessage> {

    private final MatrixBukkitBootstrap plugin;

    public ServerRequestListener(MatrixBukkitBootstrap plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(ServerRequestMessage message) {
        Matrix.getLogger().info("Bungeecord is requesting all servers, sending info...");
        if (plugin.getServerRegisterMessage() != null) {
            Matrix.getLogger().info("Sending already created server register message.");
            plugin.getServerRegisterMessage().send();
        } else {
            Matrix.getLogger().info("Server register message is null in this server.");
        }
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.SERVER_REQUEST;
    }
}
