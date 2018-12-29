package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.channels.Channel;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    private final MatrixAPI api = Matrix.getAPI();

    @EventHandler
    public void onChatEvent(ChatEvent e) {
        Connection sender = e.getSender();
        if (sender instanceof ProxiedPlayer) {
            Channel channel = MatrixBungeeBootstrap.getChannelFor(Matrix.getAPI().getPlayer(((ProxiedPlayer) sender).getUniqueId()));
            if (channel != null && !e.isCommand()) {
                ((ProxiedPlayer) sender).chat("/" + channel.getName() + " " + e.getMessage());
                e.setCancelled(true);
            }
        }
    }
}
