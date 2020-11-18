package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.messaging.message.ServerRequestMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.bungee.command.BasicCommands;
import com.github.beelzebu.matrix.bungee.command.BungeeTPCommand;
import com.github.beelzebu.matrix.bungee.command.CountdownCommand;
import com.github.beelzebu.matrix.bungee.command.CrackedCommand;
import com.github.beelzebu.matrix.bungee.command.HelpOpCommand;
import com.github.beelzebu.matrix.bungee.command.HubCommand;
import com.github.beelzebu.matrix.bungee.command.MaintenanceCommand;
import com.github.beelzebu.matrix.bungee.command.MatrixCommand;
import com.github.beelzebu.matrix.bungee.command.PlayerInfoCommand;
import com.github.beelzebu.matrix.bungee.command.PremiumCommand;
import com.github.beelzebu.matrix.bungee.command.ReplyCommand;
import com.github.beelzebu.matrix.bungee.config.BungeeConfiguration;
import com.github.beelzebu.matrix.bungee.influencer.InfluencerManager;
import com.github.beelzebu.matrix.bungee.listener.AuthListener;
import com.github.beelzebu.matrix.bungee.listener.ChatListener;
import com.github.beelzebu.matrix.bungee.listener.LocaleListener;
import com.github.beelzebu.matrix.bungee.listener.LoginFieldUpdateListener;
import com.github.beelzebu.matrix.bungee.listener.LoginListener;
import com.github.beelzebu.matrix.bungee.listener.PermissionListener;
import com.github.beelzebu.matrix.bungee.listener.ServerListListener;
import com.github.beelzebu.matrix.bungee.listener.ServerRegisterListener;
import com.github.beelzebu.matrix.bungee.listener.ServerUnregisterListener;
import com.github.beelzebu.matrix.bungee.motd.MotdManager;
import com.github.beelzebu.matrix.bungee.plugin.MatrixPluginBungee;
import com.github.beelzebu.matrix.bungee.scheduler.BungeeSchedulerAdapter;
import com.github.beelzebu.matrix.bungee.tablist.TablistManager;
import com.github.beelzebu.matrix.task.ServerCleanupTask;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class MatrixBungeeBootstrap extends Plugin implements MatrixBootstrap {

    private MatrixBungeeAPI api;
    private MatrixPluginBungee matrixPlugin;
    private BungeeConfiguration config;
    private BungeeSchedulerAdapter scheduler;
    private InfluencerManager influencerManager;

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
        api = new MatrixBungeeAPI(matrixPlugin = new MatrixPluginBungee(this));
        api.getCache().addServer(api.getServerInfo()); // add proxy to server cache since it doesn't get registered
        api.setup();
    }

    @Override
    public void onEnable() {
        loadManagers();
        registerListener(new ChatListener(api));
        registerListener(new ServerListListener(this, config.getStringList("Motd Hover").toArray(new String[0])));
        registerListener(new LoginListener(this));
        registerListener(new AuthListener());
        registerListener(new LocaleListener());
        registerCommand(new HelpOpCommand(this));
        registerCommand(new PlayerInfoCommand(this));
        registerCommand(new MaintenanceCommand(api));
        registerCommand(new ReplyCommand(this));
        registerCommand(new CountdownCommand());
        registerCommand(new PremiumCommand(this));
        registerCommand(new CrackedCommand());
        registerCommand(new BungeeTPCommand());
        //registerCommand(new MatrixServersCommand());
        registerCommand(new MatrixCommand(this));
        //0registerCommand(new StaffListCommand());
        registerCommand(new HubCommand());
        new PermissionListener();
        MotdManager.onEnable();
        new BasicCommands(this);
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            for (MatrixPlayer matrixPlayer : api.getCache().getPlayers()) {
                if (matrixPlayer.isLoggedIn()) {
                    continue;
                }
                try {
                    api.getCache().removePlayer(matrixPlayer);
                } catch (Exception e) {
                    Matrix.getLogger().warn("Error removing " + matrixPlayer.getName() + " from cache.");
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.HOURS);

        // set default listener
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));

        api.getMessaging().registerListener(new ServerRegisterListener());
        api.getMessaging().registerListener(new ServerUnregisterListener());
        api.getMessaging().registerListener(new LoginFieldUpdateListener(this));
        new ServerRequestMessage().send();

        influencerManager = new InfluencerManager(this);
        influencerManager.loadInfluencers();

        TablistManager.init();

        for (ProxiedPlayer proxiedPlayer : getProxy().getPlayers()) {
            proxiedPlayer.setTabHeader(TablistManager.getTabHeader(proxiedPlayer), TablistManager.getTabFooter(proxiedPlayer));
        }

        getScheduler().asyncRepeating(new ServerCleanupTask(api), 5, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        api.getCache().removeServer(api.getServerInfo());
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));
        ProxyServer.getInstance().getPlayers().stream().map(proxiedPlayer -> Matrix.getAPI().getPlayer(proxiedPlayer.getUniqueId())).peek(matrixPlayer -> matrixPlayer.setLoggedIn(false)).forEach(MatrixPlayer::save);
        api.getPlayers().clear();
        api.getCache().shutdown();
        api.getMessaging().shutdown();
        api.getRedisManager().shutdown();
        getScheduler().shutdownExecutor();
        getScheduler().shutdownScheduler();
    }

    public MatrixAPIImpl getApi() {
        return api;
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return scheduler;
    }

    public MatrixPluginBungee getMatrixPlugin() {
        return matrixPlugin;
    }

    public BungeeConfiguration getConfig() {
        return config;
    }

    public InfluencerManager getInfluencerManager() {
        return influencerManager;
    }

    private void loadManagers() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            Matrix.getLogger().info("LuckPerms found, hooking into it.");
        }
    }

    private void registerListener(Listener listener) {
        ProxyServer.getInstance().getPluginManager().registerListener(this, listener);
    }

    private void registerCommand(Command command) {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, command);
    }
}
