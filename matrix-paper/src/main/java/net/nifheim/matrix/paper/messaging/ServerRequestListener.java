package net.nifheim.matrix.paper.messaging;

import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerRequestMessage;
import net.nifheim.matrix.paper.MatrixPaper;

/**
 * @author Jaime Suárez
 */
public class ServerRequestListener extends MessageListener<ServerRequestMessage> {

    private final MatrixPaper plugin;

    public ServerRequestListener(MatrixPaper plugin) {
        super(StandardChannel.SERVER_REQUEST);
        this.plugin = plugin;
    }

    @Override
    public void onMessage(ServerRequestMessage message) {
        plugin.getPlatformLogger().info("Proxy is requesting all servers, sending info...");
        if (plugin.getServerRegisterMessage() != null) {
            plugin.getPlatformLogger().info("Sending already created server register message.");
            plugin.getPlugin().getApi().getMessaging().sendMessage(plugin.getServerRegisterMessage());
        } else {
            plugin.getPlatformLogger().info("Server register message is null in this server.");
        }
    }
}
