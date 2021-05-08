package com.github.beelzebu.matrix.bukkit.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerRequestMessage;

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
