package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import io.github.beelzebu.matrix.channels.Channel;
import io.github.beelzebu.matrix.command.BasicCommands;
import io.github.beelzebu.matrix.command.CountdownCommand;
import io.github.beelzebu.matrix.command.HelpOpCommand;
import io.github.beelzebu.matrix.command.MaintenanceCommand;
import io.github.beelzebu.matrix.command.PlayerInfoCommand;
import io.github.beelzebu.matrix.command.PluginsCommand;
import io.github.beelzebu.matrix.command.ReplyCommand;
import io.github.beelzebu.matrix.config.BungeeConfiguration;
import io.github.beelzebu.matrix.listener.ChatListener;
import io.github.beelzebu.matrix.listener.LoginListener;
import io.github.beelzebu.matrix.listener.ServerListListener;
import io.github.beelzebu.matrix.motd.MotdManager;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class MatrixBungeeBootstrap extends Plugin implements MatrixBootstrap {

    public final static BaseComponent[] TAB_HEADER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7¡Jugando en &6Vulthur&7!\n&7IP: &amc.vulthur.cl\n"));
    public final static BaseComponent[] TAB_FOOTER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "\n&7Tienda: &evulthur.cl/tienda &7Twitter: &e@vulthurmc\n&7Discord: &evulthur.cl/discord &7Web: &evulthur.cl"));
    private static final Map<String, Channel> CHANNELS = new HashMap<>();
    private static final String MAINTENANCE_KEY = "matrix:maintenance";
    private MatrixAPIImpl api;
    private MatrixPluginBungee matrixPlugin;
    @Setter(AccessLevel.NONE)
    private BungeeConfiguration config;

    public static Channel getChannelFor(MatrixPlayer player) {
        return CHANNELS.get(player.getStaffChannel());
    }

    @Override
    public void onLoad() {
        config = new BungeeConfiguration(new File(getDataFolder(), "config.yml"));
        (api = new MatrixBungeeAPI(matrixPlugin = new MatrixPluginBungee(this))).setup();
        Matrix.setAPI(api);
    }

    @Override
    public void onEnable() {
        loadManagers();
        registerListener(new ChatListener());
        registerListener(new ServerListListener());
        registerListener(new LoginListener(this));
        registerCommand(new HelpOpCommand(this));
        registerCommand(new PlayerInfoCommand(this));
        registerCommand(new MaintenanceCommand(this));
        registerCommand(new ReplyCommand(this));
        registerCommand(new PluginsCommand());
        registerCommand(new CountdownCommand());
        MotdManager.onEnable();
        new BasicCommands(this);
        config.getKeys("Channels").forEach((channel) -> CHANNELS.put(channel, new Channel(channel, config.getString("Channels." + channel + ".Permission"), ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register()));
        ProxyServer.getInstance().getPlayers().stream().peek(pp -> pp.setTabHeader(TAB_HEADER, TAB_FOOTER)).forEach(pp -> api.getPlugin().runAsync(() -> Optional.ofNullable(api.getPlayer(pp.getUniqueId())).orElse(new MongoMatrixPlayer(pp.getUniqueId(), pp.getName()).save()).save()));
        ProxyServer.getInstance().getScheduler().schedule(this, () -> api.getCache().getPlayers().stream().filter(matrixPlayer -> api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false)).forEach(matrixPlayer -> {
            if (!api.getPlugin().isOnline(matrixPlayer.getUniqueId(), false)) { // player may be logged in again.
                api.getCache().removePlayer(matrixPlayer);
            }
        }), 0, 1, TimeUnit.HOURS);
    }

    public boolean isMaintenance() {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            return jedis.exists(MAINTENANCE_KEY);
        } catch (Exception e) {
            api.debug(e);
            return true;
        }
    }

    public void setMaintenance(boolean maintenance) {
        try (Jedis jedis = api.getRedis().getPool().getResource()) {
            if (maintenance) {
                jedis.set(MAINTENANCE_KEY, "0");
            } else {
                jedis.del(MAINTENANCE_KEY);
            }
        } catch (Exception e) {
            api.debug(e);
        }
    }

    private void loadManagers() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            api.log("LuckPerms found, hooking into it.");
        }
    }

    private void registerListener(Listener listener) {
        ProxyServer.getInstance().getPluginManager().registerListener(this, listener);
    }

    private void registerCommand(Command command) {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, command);
    }
}