package com.github.beelzebu.matrix.api;

import com.destroystokyo.paper.PaperConfig;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.PlayerOptionChangeEvent;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.bukkit.command.staff.BungeeTPCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.CommandWatcherCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.CrackedCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.FreezeCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.MatrixReloadCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.MatrixServersCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.PlayerInfoCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.ReloadCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.ReplyCommand;
import com.github.beelzebu.matrix.bukkit.command.staff.StopCommand;
import com.github.beelzebu.matrix.bukkit.command.user.SpitCommand;
import com.github.beelzebu.matrix.bukkit.command.utils.AddLoreCommand;
import com.github.beelzebu.matrix.bukkit.command.utils.MatrixManagerCommand;
import com.github.beelzebu.matrix.bukkit.command.utils.RemoveLoreCommand;
import com.github.beelzebu.matrix.bukkit.command.utils.RenameCommand;
import com.github.beelzebu.matrix.bukkit.config.BukkitConfiguration;
import com.github.beelzebu.matrix.bukkit.listener.DupepatchListener;
import com.github.beelzebu.matrix.bukkit.listener.InternalListener;
import com.github.beelzebu.matrix.bukkit.listener.LoginListener;
import com.github.beelzebu.matrix.bukkit.listener.PlayerCommandPreprocessListener;
import com.github.beelzebu.matrix.bukkit.listener.PlayerDeathListener;
import com.github.beelzebu.matrix.bukkit.listener.VotifierListener;
import com.github.beelzebu.matrix.bukkit.menus.MatrixGUIManager;
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
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.bukkit.util.BukkitCoreUtils;
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

    private final BukkitCoreUtils bukkitCoreUtils = new BukkitCoreUtils();
    private MatrixBukkitAPI api;
    private boolean chatMuted = false;
    private BukkitConfiguration configuration;
    private MatrixPluginBukkit matrixPlugin;
    private PluginsUtility pluginsUtility;
    private ServerRegisterMessage serverRegisterMessage;
    private BukkitSchedulerAdapter scheduler;

    @Override
    public void onLoad() {
        if (Bukkit.getIp().isEmpty() || Bukkit.getIp().equals("0.0.0.0")) {
            if (System.getProperty("ILiveOnTheEdge") == null) {
                getLogger().warning("Server must not run on a public address.");
                Bukkit.shutdown();
            }
        }
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
            bukkitCoreUtils.init(this);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("Can't init a CompatUtil instance.");
            Bukkit.shutdown();
            return;
        }
        api = new MatrixBukkitAPI(matrixPlugin = new MatrixPluginBukkit(this));

        api.setup();

        // Load things
        loadManagers();

        // Register events
        registerEvents(new InternalListener());
        registerEvents(new PlayerCommandPreprocessListener(api));
        registerEvents(new PlayerDeathListener(api));
        if (api.getServerInfo().getServerType().equals(ServerType.SURVIVAL)) {
            registerEvents(new DupepatchListener(this));
        }
        if (isVotifier()) {
            registerEvents(new VotifierListener(this));
        }
        registerEvents(new LoginListener(api, this));
        // Register commands
        new CommandWatcherCommand();
        new FreezeCommand();
        new MatrixReloadCommand();
        new RemoveLoreCommand();
        new AddLoreCommand();
        new RenameCommand();
        new MatrixManagerCommand();
        new SpitCommand();
        new ReloadCommand();
        new StopCommand();
        new BungeeTPCommand();
        new CrackedCommand();
        new MatrixServersCommand();
        new PlayerInfoCommand();
        new ReplyCommand();

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
                    if (PaperConfig.enablePlayerCollisions) {
                        getLogger().warning("EnablePlayerCollisions is enabled in paper config, forcing it to false.");
                        PaperConfig.enablePlayerCollisions = false;
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
        new MatrixGUIManager(this);
    }

    @Override
    public void onDisable() {
        bukkitCoreUtils.disable();
        if (serverRegisterMessage != null) { // check if server was registered first
            api.getMessaging().sendMessage(new ServerUnregisterMessage(api.getServerInfo()));
        }
        getScheduler().shutdownExecutor();
        getScheduler().shutdownScheduler();
        api.shutdown();
        Bukkit.getScheduler().cancelTasks(this);
        CommandAPI.unregister(this);
    }

    public boolean isVotifier() {
        if (Bukkit.getPluginManager().getPlugin("Votifier") != null) {
            return Bukkit.getPluginManager().getPlugin("Votifier").isEnabled();
        }
        return false;
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
            Bukkit.shutdown();
        }
    }

    private void registerEvents(@NotNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
