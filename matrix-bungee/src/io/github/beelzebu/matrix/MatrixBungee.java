package io.github.beelzebu.matrix;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.channels.Channel;
import io.github.beelzebu.matrix.command.BasicCommands;
import io.github.beelzebu.matrix.command.HelpOP;
import io.github.beelzebu.matrix.command.Maintenance;
import io.github.beelzebu.matrix.command.PlayerInfo;
import io.github.beelzebu.matrix.command.Plugins;
import io.github.beelzebu.matrix.command.Responder;
import io.github.beelzebu.matrix.config.BungeeConfiguration;
import io.github.beelzebu.matrix.listener.ChatListener;
import io.github.beelzebu.matrix.listener.InternalListener;
import io.github.beelzebu.matrix.listener.LoginListener;
import io.github.beelzebu.matrix.listener.PubSubMessageListener;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class MatrixBungee extends Plugin {

    public final static BaseComponent[] TAB_HEADER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&7¡Jugando en &6Vulthur&7!\n&7IP: &amc.vulthur.cl\n"));
    public final static BaseComponent[] TAB_FOOTER = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "\n&7Tienda: &evulthur.cl/tienda &7Twitter: &e@vulthurmc\n&7Discord: &evulthur.cl/discord &7Web: &evulthur.cl"));
    private static final Map<String, Channel> CHANNELS = new HashMap<>();
    private static MatrixCommonAPI api;
    private boolean maintenance = false;
    private BungeeConfiguration config;

    public static Channel getChannelFor(MatrixPlayer player) {
        return CHANNELS.get(player.getStaffChannel());
    }

    @Override
    public void onLoad() {
        config = new BungeeConfiguration(new File(getDataFolder(), "config.yml"));
        (api = new MatrixBungeeAPI(new BungeeMethods(this))).setup();
        Matrix.setAPI(api);
    }

    @Override
    public void onEnable() {
        loadManagers();
        RedisBungee.getApi().registerPubSubChannels("NifheimHelpop", "Channel", "Maintenance");
        registerListener(new ChatListener());
        registerListener(new InternalListener());
        registerListener(new LoginListener(this));
        registerListener(new PubSubMessageListener(this));
        registerCommand(new HelpOP());
        registerCommand(new PlayerInfo("pinfo"));
        registerCommand(new Maintenance(this));
        registerCommand(new Plugins());
        registerCommand(new Responder());
        new BasicCommands(this);

        config.getKeys("Channels").forEach((channel) -> {
            String perm = config.getString("Channels." + channel + ".Permission");
            CHANNELS.put(channel, new Channel(channel, perm, ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register());
        });
        ProxyServer.getInstance().getPlayers().stream().peek(pp -> pp.setTabHeader(TAB_HEADER, TAB_FOOTER)).forEach(pp -> api.getPlugin().runAsync(() -> Optional.ofNullable(api.getPlayer(pp.getUniqueId())).orElse(new MongoMatrixPlayer(pp.getUniqueId(), pp.getName())).save()));
    }

    private void loadManagers() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            api.log("LuckPerms found, hooking into it.");
        }
    }

    public AbstractConfig getConfig() {
        return config;
    }

    private void registerListener(Listener listener) {
        ProxyServer.getInstance().getPluginManager().registerListener(this, listener);
    }

    private void registerCommand(Command command) {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, command);
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean value) {
        maintenance = value;
    }
}
