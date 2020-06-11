package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Beelzebu
 */
public class AuthListener implements Listener {

    @EventHandler(priority = 127)
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) e.getSender();
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(proxiedPlayer.getUniqueId());
        if (matrixPlayer.isPremium()) {
            return;
        }
        if (matrixPlayer.isLoggedIn()) {
            return;
        }
        if (e.getMessage().replaceFirst("/", "").startsWith("login") || e.getMessage().replaceFirst("/", "").startsWith("register")) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onServerSwitch(ServerConnectEvent e) {
        if (e.getTarget().getName().startsWith("auth")) {
            return;
        }
        if (e.getTarget().getName().startsWith("lobby")) {
            return;
        }
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (!matrixPlayer.isLoggedIn()) {
            e.setCancelled(true);
        }
    }
}
