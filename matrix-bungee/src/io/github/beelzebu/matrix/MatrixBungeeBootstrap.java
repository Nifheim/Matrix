package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import io.github.beelzebu.matrix.channels.Channel;
import io.github.beelzebu.matrix.command.BasicCommands;
import io.github.beelzebu.matrix.command.BungeeTPCommand;
import io.github.beelzebu.matrix.command.CountdownCommand;
import io.github.beelzebu.matrix.command.HelpOpCommand;
import io.github.beelzebu.matrix.command.MaintenanceCommand;
import io.github.beelzebu.matrix.command.PlayerInfoCommand;
import io.github.beelzebu.matrix.command.PluginsCommand;
import io.github.beelzebu.matrix.command.PremiumCommand;
import io.github.beelzebu.matrix.command.ReplyCommand;
import io.github.beelzebu.matrix.config.BungeeConfiguration;
import io.github.beelzebu.matrix.listener.ChatListener;
import io.github.beelzebu.matrix.listener.LanguageListener;
import io.github.beelzebu.matrix.listener.LoginListener;
import io.github.beelzebu.matrix.listener.PermissionListener;
import io.github.beelzebu.matrix.listener.ServerListListener;
import io.github.beelzebu.matrix.motd.MotdManager;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public class MatrixBungeeBootstrap extends Plugin implements MatrixBootstrap {

    public final static BaseComponent[] TAB_HEADER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7¡Jugando en &6Nifheim&7!\n&7IP: &amc.nifheim.net\n"));
    public final static BaseComponent[] TAB_FOOTER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "\n&7Tienda: &enifheim.net/tienda &7Twitter: &e@NifheimNetwork\n&7Discord: &enifheim.net/discord &7Web: &enifheim.net"));
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
            try {
                Files.copy(getResourceAsStream(configFile.getName()), configFile.toPath());
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
        registerListener(new ServerListListener());
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
        new PermissionListener();
        MotdManager.onEnable();
        new BasicCommands(this);
        config.getKeys("Channels").forEach((channel) -> CHANNELS.put(channel, new Channel(channel, channel, config.getString("Channels." + channel + ".Permission"), ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register()));
        ProxyServer.getInstance().getPlayers().stream().peek(pp -> pp.setTabHeader(TAB_HEADER, TAB_FOOTER)).forEach(pp -> api.getPlugin().runAsync(() -> api.getPlayers().add(Optional.ofNullable(api.getPlayer(pp.getUniqueId())).orElse(new MongoMatrixPlayer(pp.getUniqueId(), pp.getName()).save()).save())));
        ProxyServer.getInstance().getScheduler().schedule(this, () -> api.getCache().getPlayers().stream().filter(matrixPlayer -> api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false)).forEach(matrixPlayer -> {
            if (!api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false)) { // player may be logged in again.
                api.getCache().removePlayer(matrixPlayer);
            }
        }), 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void onDisable() {
        api.getPlayers().forEach(MatrixPlayer::save);
        api.getPlayers().clear();
    }

    public boolean isMaintenance() {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            return (maintenance = jedis.exists(MAINTENANCE_KEY));
        } catch (Exception e) {
            Matrix.getLogger().debug(e);
        }
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
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
