package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.bungee.tablist.TablistManager;
import com.github.beelzebu.matrix.bungee.tasks.DisconnectTask;
import com.github.beelzebu.matrix.bungee.tasks.LoginTask;
import com.github.beelzebu.matrix.bungee.tasks.PostLoginTask;
import com.github.beelzebu.matrix.bungee.tasks.PreLoginTask;
import com.github.beelzebu.matrix.bungee.util.ServerUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LoginListener implements Listener {

    private final MatrixBungeeAPI api;

    public LoginListener(MatrixBungeeAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent e) {
        if (e.getPlayer().isConnected()) {
            e.getPlayer().setTabHeader(TablistManager.getTabHeader(e.getPlayer()), TablistManager.getTabFooter(e.getPlayer()));
        }
    }

    @EventHandler(priority = 127)
    public void onPreLogin(PreLoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(api.getPlugin().getBootstrap());
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new PreLoginTask(api, e));
    }

    @EventHandler(priority = 127)
    public void onLogin(LoginEvent e) {
        e.registerIntent(api.getPlugin().getBootstrap());
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new LoginTask(api, e));
    }

    @EventHandler(priority = -128)
    public void onLogin(PostLoginEvent e) {
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new PostLoginTask(api, e));
    }

    @EventHandler(priority = -128)
    public void onConnect(ServerConnectEvent e) {
        if (e.getReason() == ServerConnectEvent.Reason.JOIN_PROXY && e.getPlayer().getPendingConnection().isOnlineMode()) {
            ServerInfo lobby = ServerUtil.getRandomLobby();
            if (lobby != null) {
                e.setTarget(lobby);
            }
        }
    }

    @EventHandler(priority = 127)
    public void onDisconnect(PlayerDisconnectEvent e) {
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new DisconnectTask(api, e));
    }
}
