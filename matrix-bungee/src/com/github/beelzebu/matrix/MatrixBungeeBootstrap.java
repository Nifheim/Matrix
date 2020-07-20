package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.messaging.message.ServerRequestMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.channels.Channel;
import com.github.beelzebu.matrix.command.BasicCommands;
import com.github.beelzebu.matrix.command.BungeeTPCommand;
import com.github.beelzebu.matrix.command.CountdownCommand;
import com.github.beelzebu.matrix.command.CrackedCommand;
import com.github.beelzebu.matrix.command.HelpOpCommand;
import com.github.beelzebu.matrix.command.MaintenanceCommand;
import com.github.beelzebu.matrix.command.MatrixCommand;
import com.github.beelzebu.matrix.command.MatrixServersCommand;
import com.github.beelzebu.matrix.command.PlayerInfoCommand;
import com.github.beelzebu.matrix.command.PluginsCommand;
import com.github.beelzebu.matrix.command.PremiumCommand;
import com.github.beelzebu.matrix.command.ReplyCommand;
import com.github.beelzebu.matrix.command.StaffListCommand;
import com.github.beelzebu.matrix.config.BungeeConfiguration;
import com.github.beelzebu.matrix.influencer.InfluencerManager;
import com.github.beelzebu.matrix.listener.AuthListener;
import com.github.beelzebu.matrix.listener.ChatListener;
import com.github.beelzebu.matrix.listener.LoginFieldUpdateListener;
import com.github.beelzebu.matrix.listener.LoginListener;
import com.github.beelzebu.matrix.listener.PermissionListener;
import com.github.beelzebu.matrix.listener.ServerListListener;
import com.github.beelzebu.matrix.listener.ServerRegisterListener;
import com.github.beelzebu.matrix.listener.ServerUnregisterListener;
import com.github.beelzebu.matrix.motd.MotdManager;
import com.github.beelzebu.matrix.scheduler.BungeeSchedulerAdapter;
import com.github.beelzebu.matrix.tablist.TablistManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class MatrixBungeeBootstrap extends Plugin implements MatrixBootstrap {

    public static final Map<String, Channel> CHANNELS = new HashMap<>();
    private MatrixBungeeAPI api;
    private MatrixPluginBungee matrixPlugin;
    private BungeeConfiguration config;
    private BungeeSchedulerAdapter scheduler;
    private InfluencerManager influencerManager;

    public static Channel getChannelFor(MatrixPlayer player) {
        return CHANNELS.get(player.getStaffChannel());
    }

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
        (api = new MatrixBungeeAPI(matrixPlugin = new MatrixPluginBungee(this))).setup();
        Matrix.setAPI(api);
    }

    @Override
    public void onEnable() {
        loadManagers();
        registerListener(new ChatListener(api));
        registerListener(new ServerListListener(config.getStringList("Motd Hover").toArray(new String[0])));
        registerListener(new LoginListener(this));
        registerListener(new AuthListener());
        registerCommand(new HelpOpCommand(this));
        registerCommand(new PlayerInfoCommand(this));
        registerCommand(new MaintenanceCommand(api));
        registerCommand(new ReplyCommand(this));
        registerCommand(new PluginsCommand());
        registerCommand(new CountdownCommand());
        registerCommand(new PremiumCommand(this));
        registerCommand(new CrackedCommand());
        registerCommand(new BungeeTPCommand());
        registerCommand(new MatrixServersCommand());
        registerCommand(new MatrixCommand(this));
        registerCommand(new StaffListCommand());
        new PermissionListener();
        MotdManager.onEnable();
        new BasicCommands(this);
        config.getKeys("Channels").forEach((channel) -> CHANNELS.put(channel, new Channel(channel, channel, config.getString("Channels." + channel + ".Permission"), ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register()));
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            for (MatrixPlayer matrixPlayer : api.getCache().getPlayers()) {
                if (api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false) || matrixPlayer.isLoggedIn()) {
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

        scheduler = new BungeeSchedulerAdapter(this);

        influencerManager = new InfluencerManager(this);
        influencerManager.loadInfluencers();

        for (ProxiedPlayer proxiedPlayer : getProxy().getPlayers()) {
            proxiedPlayer.setTabHeader(TablistManager.TAB_HEADER, TablistManager.TAB_FOOTER);
        }
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));
        api.getPlayers().forEach(MatrixPlayer::save);
        api.getPlayers().clear();
        api.getCache().shutdown();
        api.getMessaging().shutdown();
        api.getRedisManager().shutdown();
        api.getScheduler().shutdownExecutor();
        api.getScheduler().shutdownScheduler();
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

    public void setMatrixPlugin(MatrixPluginBungee matrixPlugin) {
        this.matrixPlugin = matrixPlugin;
    }

    public BungeeConfiguration getConfig() {
        return config;
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

    public InfluencerManager getInfluencerManager() {
        return influencerManager;
    }
}
