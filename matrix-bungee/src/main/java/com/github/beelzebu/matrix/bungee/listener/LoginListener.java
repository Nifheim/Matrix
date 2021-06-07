package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.bungee.listener.tasks.DisconnectTask;
import com.github.beelzebu.matrix.bungee.listener.tasks.LoginTask;
import com.github.beelzebu.matrix.bungee.listener.tasks.PostLoginTask;
import com.github.beelzebu.matrix.bungee.listener.tasks.PreLoginTask;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class LoginListener implements Listener {

    private final MatrixBungeeAPI api;

    public LoginListener(MatrixBungeeAPI api) {
        this.api = api;
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onPreLogin(@NotNull PreLoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(api.getPlugin().getBootstrap());
        api.getPlugin().getBootstrap().getScheduler().executeAsync(new PreLoginTask(api, e));
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onLogin(@NotNull LoginEvent e) {
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
