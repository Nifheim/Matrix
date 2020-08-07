package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.util.ServerUtil;
import java.util.Iterator;
import java.util.List;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Beelzebu
 */
public class AuthListener implements Listener {

    private final String[] allowedCommands = {"login", "register", "premium"};

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
        for (String command : allowedCommands) {
            if (e.getMessage().replaceFirst("/", "").startsWith(command)) {
                return;
            }
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = 127)
    public void onChat(TabCompleteEvent e) {
        removeSuggestionsFromTabComplete(e.getSender(), e.getSuggestions());
    }

    @EventHandler(priority = 127)
    public void onChat(TabCompleteResponseEvent e) {
        removeSuggestionsFromTabComplete(e.getReceiver(), e.getSuggestions());
    }

    @EventHandler
    public void onServerSwitchLoggedOut(ServerConnectEvent e) {
        if (e.getTarget().getName().startsWith("auth")) {
            return;
        }
        if (e.getTarget().getName().startsWith("lobby")) {
            return;
        }
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (!matrixPlayer.isLoggedIn()) {
            e.setTarget(ProxyServer.getInstance().getServerInfo("auth1"));
        }
    }

    @EventHandler
    public void onServerSwitchLoggedIn(ServerConnectEvent e) {
        if (!e.getTarget().getName().startsWith("auth")) {
            return;
        }
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (matrixPlayer.isLoggedIn()) {
            e.setTarget(ServerUtil.getRandomLobby());
            Matrix.getLogger().info("Server connect for: " + matrixPlayer.getName() + " old server: " + e.getPlayer().getServer().getInfo() + " reason: " + e.getReason());
        }
    }

    private void removeSuggestionsFromTabComplete(Connection receiver, List<String> suggestions) {
        if (!(receiver instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) receiver;
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(proxiedPlayer.getUniqueId());
        if (matrixPlayer.isPremium()) {
            return;
        }
        if (matrixPlayer.isLoggedIn()) {
            return;
        }
        Iterator<String> it = suggestions.iterator();
        while (it.hasNext()) {
            String suggestion = it.next().replaceFirst("/", "").toLowerCase();
            for (String command : allowedCommands) {
                if (suggestion.startsWith(command)) {
                    break;
                }
                it.remove();
            }
        }
    }
}
