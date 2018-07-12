package io.github.beelzebu.matrix.listener;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.Main;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.channels.Channel;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    private final MatrixAPI core = MatrixAPI.getInstance();

    @EventHandler
    public void onChatEvent(ChatEvent e) {
        Connection sender = e.getSender();
        if (sender instanceof ProxiedPlayer) {
            Channel channel = Main.getChannelFor(((ProxiedPlayer) sender).getUniqueId());
            if (channel != null && !e.isCommand()) {
                RedisBungee.getApi().sendChannelMessage("Channel", channel.getName() + " -div- " + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "," + core.getDisplayName(((ProxiedPlayer) sender).getUniqueId(), true) + " -div- §" + channel.getColor() + e.getMessage());
                e.setCancelled(true);
            }
        }
    }
}
