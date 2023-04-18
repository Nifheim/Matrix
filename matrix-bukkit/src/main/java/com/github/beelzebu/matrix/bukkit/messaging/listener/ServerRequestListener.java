package com.github.beelzebu.matrix.bukkit.messaging.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;

/**
 * @author Jaime Suárez
 */
public class ServerRequestListener extends MessageListener {

    private final MatrixBukkitBootstrap plugin;

    public ServerRequestListener(MatrixBukkitBootstrap plugin) {
        super(MessageType.SERVER_REQUEST);
        this.plugin = plugin;
    }

    @Override
    public void onMessage(Message message) {
        Matrix.getLogger().info("Bungeecord is requesting all servers, sending info...");
        if (plugin.getServerRegisterMessage() != null) {
            Matrix.getLogger().info("Sending already created server register message.");
            plugin.getApi().getMessaging().sendMessage(plugin.getServerRegisterMessage());
        } else {
            Matrix.getLogger().info("Server register message is null in this server.");
        }
    }
}
