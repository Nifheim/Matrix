package io.github.beelzebu.matrix;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.channels.Channel;
import io.github.beelzebu.matrix.command.BasicCommands;
import io.github.beelzebu.matrix.command.HelpOP;
import io.github.beelzebu.matrix.command.Maintenance;
import io.github.beelzebu.matrix.command.PlayerInfo;
import io.github.beelzebu.matrix.command.Plugins;
import io.github.beelzebu.matrix.command.Responder;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import io.github.beelzebu.matrix.listener.ChatListener;
import io.github.beelzebu.matrix.listener.InternalListener;
import io.github.beelzebu.matrix.listener.LoginListener;
import io.github.beelzebu.matrix.listener.PubSubMessageListener;
import io.github.beelzebu.matrix.utils.BungeeConfiguration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class Main extends Plugin {

    private static final MatrixAPI core = MatrixAPI.getInstance();
    public static final BaseComponent[] TAB_HEADER = TextComponent.fromLegacyText(core.rep("&7¡Jugando en &6Vulthur&7!\n&7IP: &amc.vulthur.cl\n"));
    public static final BaseComponent[] TAB_FOOTER = TextComponent.fromLegacyText(core.rep("\n&7Tienda: &evulthur.cl/tienda &7Twitter: &e@vulthurmc\n&7Discord: &evulthur.cl/discord &7Web: &evulthur.cl"));
    private final static Set<Channel> channels = Sets.newHashSet();
    private final static Map<UUID, Channel> pchannels = Maps.newHashMap();
    private static Main instance;
    private static boolean maintenance = false;
    private final CommandSender console = ProxyServer.getInstance().getConsole();
    private BungeeConfiguration config;

    public static Main getInstance() {
        return instance;
    }

    public static Set<Channel> getChannels() {
        return channels;
    }

    public static Channel getChannelFor(UUID player) {
        return pchannels.get(player);
    }

    public static void setChannelFor(UUID player, Channel channel) {
        if (pchannels.containsKey(player) && pchannels.get(player).equals(channel)) {
            pchannels.remove(player);
            ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(player);
            if (pp != null) {
                pp.sendMessage(new ComponentBuilder("Tu chat vuelve a la normalidad. ").color(ChatColor.YELLOW).create());
            }
        } else {
            pchannels.put(player, channel);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new BungeeConfiguration();
        core.setup(new BungeeMethods());
        loadManagers();
        RedisBungee.getApi().registerPubSubChannels("NifheimHelpop", "Channel", "Maintenance");
        registerListener(new ChatListener());
        registerListener(new InternalListener());
        registerListener(new LoginListener(this));
        registerListener(new PubSubMessageListener());
        registerCommand(new HelpOP());
        registerCommand(new PlayerInfo("pinfo"));
        registerCommand(new Maintenance());
        registerCommand(new Plugins());
        registerCommand(new Responder());
        new BasicCommands(this);

        config.getKeys("Channels").forEach((channel) -> {
            String command = config.getString("Channels." + channel + ".Command");
            String perm = config.getString("Channels." + channel + ".Permission");
            String color = "§" + (config.getString("Channels." + channel + ".Color") != null ? config.getString("Channels." + channel + ".Color") : "f");
            channels.add(new Channel(channel, new Command(command) {
                @Override
                public void execute(CommandSender sender, String[] args) {
                    core.getMethods().runAsync(() -> {
                        if (sender.hasPermission(perm)) {
                            if (args.length == 0 && sender instanceof ProxiedPlayer) {
                                RedisBungee.getApi().sendChannelMessage("Channel", "set ," + channel + "," + ((ProxiedPlayer) sender).getUniqueId());
                            } else {
                                String msg = "";
                                for (String arg : args) {
                                    msg += arg + " ";
                                }
                                RedisBungee.getApi().sendChannelMessage("Channel", channel + " -div- " + (sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer().getInfo().getName() + "," + MatrixAPI.getInstance().getDisplayName(((ProxiedPlayer) sender).getUniqueId(), true) : sender.getName()) + " -div- " + color + msg);
                            }
                        }
                    });
                }
            }, perm, color));
        });
        ProxyServer.getInstance().getPlayers().forEach(pp -> {
            if (pp.getServer().getInfo().getName().startsWith("towny")) {
                String serverName = pp.getServer().getInfo().getName();
                pp.setTabHeader(TAB_HEADER, TAB_FOOTER);
            }
        });
    }

    private void loadManagers() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            core.log("LuckPerms found, hooking into it.");
        }
    }

    public IConfiguration getConfig() {
        return config;
    }

    public CommandSender getConsole() {
        return console;
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
