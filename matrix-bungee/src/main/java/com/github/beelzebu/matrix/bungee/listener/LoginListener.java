package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.bungee.tablist.TablistManager;
import com.github.beelzebu.matrix.bungee.tasks.DisconnectTask;
import com.github.beelzebu.matrix.bungee.tasks.LoginTask;
import com.github.beelzebu.matrix.bungee.tasks.PostLoginTask;
import com.github.beelzebu.matrix.bungee.tasks.PreLoginTask;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {

    private final MatrixBungeeAPI api;

    public LoginListener(MatrixBungeeAPI api) {
        this.api = api;
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onServerSwitch(ServerSwitchEvent e) {
        if (e.getPlayer().isConnected()) {
            e.getPlayer().setTabHeader(TablistManager.getTabHeader(e.getPlayer()), TablistManager.getTabFooter(e.getPlayer()));
        }
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onPreLogin(PreLoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(api.getPlugin().getBootstrap());
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new PreLoginTask(api, e));
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onLogin(LoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(api.getPlugin().getBootstrap());
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new LoginTask(api, e));
    }

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onPostLogin(PostLoginEvent e) {
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new PostLoginTask(api, e));
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onDisconnect(PlayerDisconnectEvent e) {
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new DisconnectTask(api, e));
    }
}
