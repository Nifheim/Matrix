package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.ServerRequestMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.channels.Channel;
import com.github.beelzebu.matrix.command.BasicCommands;
import com.github.beelzebu.matrix.command.BungeeTPCommand;
import com.github.beelzebu.matrix.command.CountdownCommand;
import com.github.beelzebu.matrix.command.HelpOpCommand;
import com.github.beelzebu.matrix.command.MaintenanceCommand;
import com.github.beelzebu.matrix.command.MatrixServersCommand;
import com.github.beelzebu.matrix.command.PlayerInfoCommand;
import com.github.beelzebu.matrix.command.PluginsCommand;
import com.github.beelzebu.matrix.command.PremiumCommand;
import com.github.beelzebu.matrix.command.ReplyCommand;
import com.github.beelzebu.matrix.config.BungeeConfiguration;
import com.github.beelzebu.matrix.listener.ChatListener;
import com.github.beelzebu.matrix.listener.LanguageListener;
import com.github.beelzebu.matrix.listener.LoginListener;
import com.github.beelzebu.matrix.listener.PermissionListener;
import com.github.beelzebu.matrix.listener.ServerListListener;
import com.github.beelzebu.matrix.listener.ServerRegisterListener;
import com.github.beelzebu.matrix.listener.ServerUnregisterListener;
import com.github.beelzebu.matrix.motd.MotdManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public class MatrixBungeeBootstrap extends Plugin implements MatrixBootstrap {

    //public final static BaseComponent[] TAB_HEADER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7¡Jugando en &6Nifheim&7!\n&7IP: &amc.nifheim.net\n"));
    //public final static BaseComponent[] TAB_FOOTER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "\n&7Tienda: &enifheim.net/tienda &7Twitter: &e@NifheimNetwork\n&7Discord: &enifheim.net/discord &7Web: &enifheim.net"));
    private static final Map<String, Channel> CHANNELS = new HashMap<>();
    private static final String MAINTENANCE_KEY = "matrix:maintenance";
    private MatrixAPIImpl api;
    private MatrixPluginBungee matrixPlugin;
    private BungeeConfiguration config;
    private boolean maintenance;

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
        (api = new MatrixBungeeAPI(matrixPlugin = new MatrixPluginBungee(this), this)).setup();
        Matrix.setAPI(api);
    }

    @Override
    public void onEnable() {
        loadManagers();
        registerListener(new ChatListener(api));
        registerListener(new ServerListListener(config.getStringList("Motd Hover").toArray(new String[0])));
        registerListener(new LoginListener(this));
        registerListener(new LanguageListener());
        registerCommand(new HelpOpCommand(this));
        registerCommand(new PlayerInfoCommand(this));
        registerCommand(new MaintenanceCommand(this));
        registerCommand(new ReplyCommand(this));
        registerCommand(new PluginsCommand());
        registerCommand(new CountdownCommand());
        registerCommand(new PremiumCommand(this));
        registerCommand(new BungeeTPCommand());
        registerCommand(new MatrixServersCommand());
        new PermissionListener();
        MotdManager.onEnable();
        new BasicCommands(this);
        config.getKeys("Channels").forEach((channel) -> CHANNELS.put(channel, new Channel(channel, channel, config.getString("Channels." + channel + ".Permission"), ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register()));
        //ProxyServer.getInstance().getPlayers().stream().peek(pp -> pp.setTabHeader(TAB_HEADER, TAB_FOOTER)).forEach(pp -> api.getPlugin().runAsync(() -> api.getPlayers().add(Optional.ofNullable(api.getPlayer(pp.getUniqueId())).orElse(new MongoMatrixPlayer(pp.getUniqueId(), pp.getName()).save()).save())));
        ProxyServer.getInstance().getScheduler().schedule(this, () -> api.getCache().getPlayers().stream().filter(matrixPlayer -> api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false)).forEach(matrixPlayer -> {
            if (!api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false)) { // player may be logged in again.
                matrixPlayer.setLoggedIn(false);
                api.getCache().removePlayer(matrixPlayer);
            }
        }), 0, 1, TimeUnit.HOURS);

        // set default listener
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));

        api.getMessaging().registerListener(new ServerRegisterListener());
        api.getMessaging().registerListener(new ServerUnregisterListener());
        new ServerRequestMessage().send();
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().set(0, "lobby"));
        api.getPlayers().forEach(MatrixPlayer::save);
        api.getPlayers().clear();
    }

    public boolean isMaintenance() {
        try (Jedis jedis = api.getMessaging().getPool().getResource()) {
            return (maintenance = jedis.exists(MAINTENANCE_KEY));
        } catch (Exception e) {
            Matrix.getLogger().debug(e);
        }
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        try (Jedis jedis = api.getMessaging().getPool().getResource()) {
            if (maintenance) {
                jedis.set(MAINTENANCE_KEY, "0");
            } else {
                jedis.del(MAINTENANCE_KEY);
            }
        } catch (Exception e) {
            Matrix.getLogger().debug(e);
            setMaintenance(maintenance);
            return;
        }
        this.maintenance = maintenance;
    }

    public MatrixAPIImpl getApi() {
        return api;
    }

    public void setApi(MatrixAPIImpl api) {
        this.api = api;
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

    public void setConfig(BungeeConfiguration config) {
        this.config = config;
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
