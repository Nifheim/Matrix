package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.bungee.listener.tasks.DisconnectTask;
import com.github.beelzebu.matrix.bungee.listener.tasks.LoginTask;
import com.github.beelzebu.matrix.bungee.listener.tasks.PostLoginTask;
import com.github.beelzebu.matrix.bungee.listener.tasks.PreLoginTask;
import com.github.beelzebu.matrix.util.LoginState;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class LoginListener implements Listener {

    private final MatrixBungeeAPI api;
    private final Map<String, LoginState> loginStateMap = new HashMap<>();
    private final Map<String, Boolean> profile = new HashMap<>();

    public LoginListener(MatrixBungeeAPI api) {
        this.api = api;
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onPreLogin(@NotNull PreLoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(api.getPlugin().getBootstrap());
        PreLoginTask preLoginTask = new PreLoginTask(api, e);
        api.getPlugin().getBootstrap().getScheduler().makeFuture(preLoginTask).thenRun(() -> {
            loginStateMap.put(e.getConnection().getName(), LoginState.PRE_LOGIN);
            profile.put(e.getConnection().getName(), preLoginTask.getProfile() != null);
            Matrix.getLogger().info("Pre login state for " + e.getConnection().getName());
        });
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onLogin(@NotNull LoginEvent e) {
        if (e.isCancelled()) {
            return;
        }
        e.registerIntent(api.getPlugin().getBootstrap());
        api.getPlugin().getBootstrap().getScheduler().makeFuture(new LoginTask(api, e)).thenRun(() -> {
            loginStateMap.put(e.getConnection().getName(), LoginState.LOGIN);
            Matrix.getLogger().info("Login state for " + e.getConnection().getName());
        });
    }

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onPostLogin(PostLoginEvent e) {
        Boolean premiumProfile = profile.remove(e.getPlayer().getName());
        api.getPlugin().getBootstrap().getScheduler().makeFuture(new PostLoginTask(api, e, premiumProfile != null && premiumProfile)).thenRun(() -> {
            loginStateMap.put(e.getPlayer().getName(), LoginState.POST_LOGIN);
            Matrix.getLogger().info("Post login state for " + e.getPlayer().getName());
        });
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onDisconnect(PlayerDisconnectEvent e) {
        api.getPlugin().getBootstrap().getScheduler().makeFuture(new DisconnectTask(api, e)).thenRun(() -> {
            loginStateMap.remove(e.getPlayer().getName());
            Matrix.getLogger().info("Removed state for " + e.getPlayer().getName());
        });
    }
}
