package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.PlayerOptionChangeEvent;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.bukkit.command.staff.BungeeTPCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.MatrixReloadCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.MatrixServersCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.PlayerInfoCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.ReloadCommand;
import com.github.beelzebu.matrix.bukkit.command.user.SpitCommand;
import com.github.beelzebu.matrix.bukkit.command.utils.MatrixManagerCommand;
import com.github.beelzebu.matrix.bukkit.config.BukkitConfiguration;
import com.github.beelzebu.matrix.bukkit.messaging.listener.ServerRequestListener;
import com.github.beelzebu.matrix.bukkit.messaging.listener.StaffChatListener;
import com.github.beelzebu.matrix.bukkit.messaging.listener.TargetedMessageListener;
import com.github.beelzebu.matrix.bukkit.plugin.MatrixPluginBukkit;
import com.github.beelzebu.matrix.bukkit.scheduler.BukkitSchedulerAdapter;
import com.github.beelzebu.matrix.bukkit.util.PluginsUtility;
import com.github.beelzebu.matrix.bukkit.util.placeholders.OnlinePlaceholders;
import com.github.beelzebu.matrix.messaging.message.ServerRegisterMessage;
import com.github.beelzebu.matrix.messaging.message.ServerUnregisterMessage;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.github.beelzebu.matrix.util.ReadURL;
import io.papermc.paper.configuration.GlobalConfiguration;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.bukkit.util.CompatUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;

/**
 * @author Beelzebu
 */
public class MatrixBukkitBootstrap extends JavaPlugin implements MatrixBootstrap {

    private MatrixBukkitAPI api;
    private boolean chatMuted = false;
    private BukkitConfiguration configuration;
    private MatrixPluginBukkit matrixPlugin;
    private PluginsUtility pluginsUtility;
    private ServerRegisterMessage serverRegisterMessage;
    private BukkitSchedulerAdapter scheduler;

    @Override
    public void onLoad() {
        if (!SpigotConfig.bungee) {
            getLogger().warning("Bungee is disabled in spigot config, forcing it to true.");
            SpigotConfig.bungee = true;
        }
        try {
            Class.forName("org.spigotmc.SpigotConfig").getField("debug");
            if (SpigotConfig.debug) {
                getLogger().warning("Debug is enabled in spigot config, forcing it to false.");
                SpigotConfig.debug = false;
            }
        } catch (@NotNull NoSuchFieldException | ClassNotFoundException ignore) {
        }
        saveResource("config.yml", false);
        configuration = new BukkitConfiguration(new File(getDataFolder(), "config.yml"));
    }

    @Override
    public void onEnable() {
        scheduler = new BukkitSchedulerAdapter(this);
        try {
            api = new MatrixBukkitAPI(matrixPlugin = new MatrixPluginBukkit(this));
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

        api.setup();

        // Load things
        loadManagers();

        /*
        // Register events
        registerEvents(new InternalListener());
        registerEvents(new PlayerCommandPreprocessListener(api));
        registerEvents(new PlayerDeathListener(api));
        if (api.getServerInfo().getServerType().equals(ServerType.SURVIVAL)) {
            registerEvents(new DupepatchListener(this));
        }
        registerEvents(new LoginListener(api, this));
        */
        // Register commands
        /*
        new CommandWatcherCommand();
        new FreezeCommand();
        new RemoveLoreCommand();
        new AddLoreCommand();
        new RenameCommand();
        new CrackedCommand();
        new ReplyCommand();
        */
        new MatrixReloadCommand();
        new MatrixManagerCommand();
        new SpitCommand();
        new ReloadCommand();
        //new StopCommand();
        new BungeeTPCommand();
        new MatrixServersCommand();
        new PlayerInfoCommand();

        pluginsUtility = new PluginsUtility();

        serverRegisterMessage = new ServerRegisterMessage(api.getServerInfo(), Bukkit.getIp(), Bukkit.getPort());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            pluginsUtility.checkForPluginsToRemove();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPluginEnable(PluginEnableEvent e) {
                    pluginsUtility.checkForPluginsToRemove();
                }
            }, this);
            Bukkit.getOperators().forEach(op -> op.setOp(false)); // remove operators
            Stream.of(BanList.Type.values()).map(Bukkit::getBanList).forEach(banList -> banList.getBanEntries().forEach(banEntry -> banEntry.setExpiration(new Date()))); // expire vanilla bans
            Bukkit.getOnlinePlayers().forEach(p -> {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + p.getName() + "&clave=" + getConfig().getString("clave"));
                } catch (Exception ex) {
                    Logger.getLogger(MatrixBukkitBootstrap.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", p.getName());
                }
            });
            if (serverRegisterMessage != null) {
                api.getMessaging().sendMessage(serverRegisterMessage);
            }
        });
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_9)) {
                    if (GlobalConfiguration.get().collisions.enablePlayerCollisions) {
                        getLogger().warning("EnablePlayerCollisions is enabled in paper config, forcing it to false.");
                        GlobalConfiguration.get().collisions.enablePlayerCollisions = false;
                    }
                }
            } catch (ClassNotFoundException ignored) { // doesn't exists on spigot lol
            }
        }
        api.getMessaging().registerListener(new ServerRequestListener(this));
        api.getMessaging().registerListener(new StaffChatListener());
        api.getMessaging().registerListener(new TargetedMessageListener(this));
        new PlayerOptionChangeEvent.PlayerOptionChangeListener() {
            @Override
            public void onPlayerOptionChange(@NotNull PlayerOptionChangeEvent e) {
                Player player = Bukkit.getPlayer(e.getMatrixPlayer().getUniqueId());
                if (player != null && player.isOnline()) {
                    switch (e.getOptionType()) {
                        case FLY:
                            if (player.hasPermission("cmi.command.fly") || player.hasPermission("essentials.fly") || player.hasPermission("matrix.command.fly")) {
                                player.setAllowFlight(e.getState());
                                player.setFlying(e.getState());
                            }
                            break;
                        case SPEED:
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, false, false));
                            break;
                    }
                }
            }
        };
        ConfigurationSection servers = getConfig().getConfigurationSection("servers");
        if (servers != null) {
            servers.getKeys(false).forEach(server -> {
                Message message = new ServerRegisterMessage(new ServerInfoImpl(ServerType.SURVIVAL, "extra", server, GameMode.SURVIVAL, true), servers.getString(server + ".address"), servers.getInt(server + ".port"));
                Matrix.getAPI().getMessaging().sendMessage(message);
            });
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        if (serverRegisterMessage != null) { // check if server was registered first
            api.getMessaging().sendMessage(new ServerUnregisterMessage(api.getServerInfo()));
        }
        getScheduler().shutdownExecutor();
        getScheduler().shutdownScheduler();
        api.shutdown();
        Bukkit.getScheduler().cancelTasks(this);
        CommandAPI.unregister(this);
    }

    public MatrixBukkitAPI getApi() {
        return api;
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean chatMuted) {
        this.chatMuted = chatMuted;
    }

    public BukkitConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public @NotNull SchedulerAdapter getScheduler() {
        return scheduler;
    }

    public @NotNull MatrixPluginBukkit getMatrixPlugin() {
        return matrixPlugin;
    }

    public ServerRegisterMessage getServerRegisterMessage() {
        return serverRegisterMessage;
    }

    private void loadManagers() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Matrix.getLogger().info("PlaceholderAPI found, hooking into it.");
            new OnlinePlaceholders(api).register();
        } else {
            getLogger().severe("Missing PlaceholderAPI");
            Bukkit.shutdown();
        }
    }

    private void registerEvents(@NotNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
