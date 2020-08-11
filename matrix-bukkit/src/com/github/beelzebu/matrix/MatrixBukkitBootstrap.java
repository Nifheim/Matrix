package com.github.beelzebu.matrix;

import com.destroystokyo.paper.PaperConfig;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.command.CommandAPI;
import com.github.beelzebu.matrix.api.menu.GUIManager;
import com.github.beelzebu.matrix.api.messaging.message.ServerRegisterMessage;
import com.github.beelzebu.matrix.api.messaging.message.ServerUnregisterMessage;
import com.github.beelzebu.matrix.api.player.PlayerOptionChangeEvent;
import com.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.server.powerup.tasks.PowerupSpawnTask;
import com.github.beelzebu.matrix.command.staff.CommandWatcherCommand;
import com.github.beelzebu.matrix.command.staff.FreezeCommand;
import com.github.beelzebu.matrix.command.staff.LaunchPadsCommand;
import com.github.beelzebu.matrix.command.staff.MatrixReloadCommand;
import com.github.beelzebu.matrix.command.staff.PluginsCommand;
import com.github.beelzebu.matrix.command.staff.PowerupsCommand;
import com.github.beelzebu.matrix.command.staff.ReloadCommand;
import com.github.beelzebu.matrix.command.staff.StopCommand;
import com.github.beelzebu.matrix.command.staff.VanishCommand;
import com.github.beelzebu.matrix.command.user.OptionsCommand;
import com.github.beelzebu.matrix.command.user.ProfileCommand;
import com.github.beelzebu.matrix.command.user.SpitCommand;
import com.github.beelzebu.matrix.command.utils.AddLoreCommand;
import com.github.beelzebu.matrix.command.utils.MatrixManagerCommand;
import com.github.beelzebu.matrix.command.utils.RemoveLoreCommand;
import com.github.beelzebu.matrix.command.utils.RenameCommand;
import com.github.beelzebu.matrix.command.utils.SyncCommand;
import com.github.beelzebu.matrix.config.BukkitConfiguration;
import com.github.beelzebu.matrix.listener.DupepatchListener;
import com.github.beelzebu.matrix.listener.GUIListener;
import com.github.beelzebu.matrix.listener.InternalListener;
import com.github.beelzebu.matrix.listener.LoginListener;
import com.github.beelzebu.matrix.listener.PlayerCommandPreprocessListener;
import com.github.beelzebu.matrix.listener.PlayerDeathListener;
import com.github.beelzebu.matrix.listener.ServerRequestListener;
import com.github.beelzebu.matrix.listener.VanishListener;
import com.github.beelzebu.matrix.listener.VotifierListener;
import com.github.beelzebu.matrix.listener.lobby.ItemListener;
import com.github.beelzebu.matrix.listener.lobby.LobbyListener;
import com.github.beelzebu.matrix.listener.stats.StatsListener;
import com.github.beelzebu.matrix.scheduler.BukkitSchedulerAdapter;
import com.github.beelzebu.matrix.util.CompatUtil;
import com.github.beelzebu.matrix.util.PluginsUtility;
import com.github.beelzebu.matrix.util.ReadURL;
import com.github.beelzebu.matrix.util.bungee.BungeeCleanupTask;
import com.github.beelzebu.matrix.util.bungee.BungeeServerTracker;
import com.github.beelzebu.matrix.util.placeholders.StatsPlaceholders;
import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.SpigotConfig;

/**
 * @author Beelzebu
 */
public class MatrixBukkitBootstrap extends JavaPlugin implements MatrixBootstrap {

    private final String[] localAddresses = {"localhost", "127.0.0.1", "172.20.0.2", "172.20.0.3"};
    private MatrixAPIImpl api;
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
        if (SpigotConfig.debug) {
            getLogger().warning("Debug is enabled in spigot config, forcing it to false.");
            SpigotConfig.debug = false;
        }
        saveResource("config.yml", false);
        configuration = new BukkitConfiguration(new File(getDataFolder(), "config.yml"));
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            UUID inv = GUIManager.getOpenInventories().get(p.getUniqueId());
            if (inv != null) {
                p.closeInventory();
            }
        });
        if (serverRegisterMessage != null) { // check if server was registered first
            new ServerUnregisterMessage().send();
        }
        api.shutdown();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        Matrix.setAPI(api = new MatrixBukkitAPI(matrixPlugin = new MatrixPluginBukkit(this)));
        try {
            CompatUtil.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("Can't init a CompatUtil instance.");
            Bukkit.shutdown();
        }
        api.setup();

        scheduler = new BukkitSchedulerAdapter(this);

        if (Objects.equals(api.getServerInfo().getServerName(), "lobby")) {
            getLogger().warning("Invalid server name in config, lobby servers must be enumerated.");
            Bukkit.shutdown();
        }
        // Load things
        loadManagers();

        // Register events
        registerEvents(new GUIListener());
        registerEvents(new InternalListener());
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || api.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA)) {
            registerEvents(new ItemListener(this));
            registerEvents(new LobbyListener(this));
        }
        registerEvents(new PlayerCommandPreprocessListener(this));
        registerEvents(new PlayerDeathListener(this));
        if (api.getServerInfo().getServerType().equals(ServerType.SURVIVAL)) {
            registerEvents(new DupepatchListener(this));
        }
        registerEvents(new StatsListener(api));
        if (isVotifier()) {
            registerEvents(new VotifierListener(this));
        }
        registerEvents(new VanishListener(this));
        registerEvents(new LoginListener(api, this));
        // Register commands
        CommandAPI.registerCommand(this, new CommandWatcherCommand());
        CommandAPI.registerCommand(this, new FreezeCommand());
        CommandAPI.registerCommand(this, new MatrixReloadCommand());
        CommandAPI.registerCommand(this, new OptionsCommand());
        CommandAPI.registerCommand(this, new PowerupsCommand());
        CommandAPI.registerCommand(this, new LaunchPadsCommand());
        CommandAPI.registerCommand(this, new RemoveLoreCommand());
        CommandAPI.registerCommand(this, new AddLoreCommand());
        CommandAPI.registerCommand(this, new RenameCommand());
        CommandAPI.registerCommand(this, new MatrixManagerCommand());
        CommandAPI.registerCommand(this, new SpitCommand());
        CommandAPI.registerCommand(this, new SyncCommand());
        CommandAPI.registerCommand(this, new ReloadCommand());
        CommandAPI.registerCommand(this, new StopCommand());
        CommandAPI.registerCommand(this, new PluginsCommand());
        CommandAPI.registerCommand(this, new VanishCommand());
        CommandAPI.registerCommand(this, new ProfileCommand());

        pluginsUtility = new PluginsUtility();

        serverRegisterMessage = null;

        for (String ip : localAddresses) {
            if (Objects.equals(Bukkit.getIp(), ip)) {
                serverRegisterMessage = new ServerRegisterMessage(ip, Bukkit.getPort());
                break;
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            pluginsUtility.checkForPluginsToRemove();
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
                serverRegisterMessage.send();
            }
        });
        BungeeServerTracker.startTask(5);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BungeeCleanupTask(), 600, 600);
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
                    if (PaperConfig.savePlayerData) {
                        getLogger().warning("SavePlayerData is enabled in paper config, forcing it to false.");
                        PaperConfig.savePlayerData = false;
                    }
                }
                if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_9)) {
                    if (PaperConfig.enablePlayerCollisions) {
                        getLogger().warning("EnablePlayerCollisions is enabled in paper config, forcing it to false.");
                        PaperConfig.enablePlayerCollisions = false;
                    }
                }
            } catch (ClassNotFoundException ignored) { // doesn't exists on spigot lol
            }
            Bukkit.getScheduler().runTaskTimer(this, new PowerupSpawnTask(), 0, 1200);
        }
        api.getMessaging().registerListener(new ServerRequestListener(this));
        new PlayerOptionChangeEvent.PlayerOptionChangeListener() {
            @Override
            public void onPlayerOptionChange(PlayerOptionChangeEvent e) {
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
    }

    public boolean isVotifier() {
        if (Bukkit.getPluginManager().getPlugin("Votifier") != null) {
            return Bukkit.getPluginManager().getPlugin("Votifier").isEnabled();
        }
        return false;
    }

    public MatrixAPIImpl getApi() {
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
    public SchedulerAdapter getScheduler() {
        return scheduler;
    }

    public MatrixPluginBukkit getMatrixPlugin() {
        return matrixPlugin;
    }

    public ServerRegisterMessage getServerRegisterMessage() {
        return serverRegisterMessage;
    }

    private void loadManagers() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                Matrix.getLogger().info("LuckPerms found, hooking into it.");
            } else {
                Bukkit.shutdown();
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Matrix.getLogger().info("PlaceholderAPI found, hooking into it.");
            // Utils
            StatsPlaceholders statsPlaceholders = new StatsPlaceholders();
            PlaceholderAPI.registerExpansion(statsPlaceholders);
        }
    }

    private void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
