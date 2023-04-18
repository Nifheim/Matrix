package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.bungee.command.HelpOpCommand;
import com.github.beelzebu.matrix.bungee.command.MaintenanceCommand;
import com.github.beelzebu.matrix.bungee.command.MatrixCommand;
import com.github.beelzebu.matrix.bungee.config.BungeeConfiguration;
import com.github.beelzebu.matrix.bungee.listener.ChatListener;
import com.github.beelzebu.matrix.bungee.listener.LocaleListener;
import com.github.beelzebu.matrix.bungee.listener.LoginListener;
import com.github.beelzebu.matrix.bungee.messaging.listener.ServerRegisterListener;
import com.github.beelzebu.matrix.bungee.messaging.listener.ServerUnregisterListener;
import com.github.beelzebu.matrix.bungee.plugin.MatrixPluginBungee;
import com.github.beelzebu.matrix.bungee.scheduler.BungeeSchedulerAdapter;
import com.github.beelzebu.matrix.bungee.util.ServerCleanupTask;
import com.github.beelzebu.matrix.messaging.message.ServerRequestMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class MatrixBungeeBootstrap extends Plugin implements MatrixBootstrap {

    private MatrixBungeeAPI api;
    private MatrixPluginBungee matrixPlugin;
    private BungeeConfiguration config;
    private BungeeSchedulerAdapter scheduler;

    @Override
    public void onLoad() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            try {
                Files.copy(getResourceAsStream("config.yml"), configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = new BungeeConfiguration(configFile);
        scheduler = new BungeeSchedulerAdapter(this);
        try {
            api = new MatrixBungeeAPI(matrixPlugin = new MatrixPluginBungee(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        api.getServerManager().addServer(api.getServerInfo()); // add proxy to server cache since it doesn't get registered
        api.setup();
    }

    @Override
    public void onEnable() {
        loadManagers();
        registerListener(new ChatListener(api));
        registerListener(new LoginListener(api));
        registerListener(new LocaleListener(api));
        registerCommand(new HelpOpCommand(this));
        registerCommand(new MaintenanceCommand(api));
        registerCommand(new MatrixCommand(this));
        ProxyServer.getInstance().getScheduler().schedule(this, () -> matrixPlugin.getLoggedInPlayers().thenAcceptAsync(matrixPlayers -> {
            for (MatrixPlayer matrixPlayer : matrixPlayers) {
                if (matrixPlayer.isLoggedIn()) {
                    continue;
                }
                try {
                    api.getDatabase().cleanUp(matrixPlayer);
                } catch (Exception e) {
                    Matrix.getLogger().warn("Error removing " + matrixPlayer.getName() + " from cache.");
                    e.printStackTrace();
                }
            }
        }), 0, 1, TimeUnit.HOURS);

        // set default listener
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));

        api.getMessaging().registerListener(new ServerRegisterListener());
        api.getMessaging().registerListener(new ServerUnregisterListener());
        api.getMessaging().sendMessage(new ServerRequestMessage());
        getScheduler().asyncRepeating(new ServerCleanupTask(api), 5, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));
        for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
            MatrixPlayer matrixPlayer = api.getPlayerManager().getPlayer(proxiedPlayer).join();
            matrixPlayer.setLoggedIn(false);
            api.getDatabase().cleanUp(matrixPlayer);
        }
        api.getDatabase().shutdown();
        api.getMessaging().shutdown();
        api.getRedisManager().shutdown();
        getScheduler().shutdownExecutor();
        getScheduler().shutdownScheduler();
    }

    public MatrixBungeeAPI getApi() {
        return api;
    }

    @Override
    public @NotNull SchedulerAdapter getScheduler() {
        return scheduler;
    }

    public @NotNull MatrixPluginBungee getMatrixPlugin() {
        return matrixPlugin;
    }

    public BungeeConfiguration getConfig() {
        return config;
    }

    private void loadManagers() {
    }

    private void registerListener(@NotNull Listener listener) {
        ProxyServer.getInstance().getPluginManager().registerListener(this, listener);
    }

    private void registerCommand(@NotNull Command command) {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, command);
    }
}
