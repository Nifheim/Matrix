package net.nifheim.matrix.paper;

import io.papermc.paper.configuration.GlobalConfiguration;
import java.util.Arrays;
import java.util.Date;
import net.kyori.adventure.audience.Audience;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.matrix.api.environment.Environment;
import net.nifheim.matrix.api.server.ServerType;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;
import net.nifheim.matrix.common.plugin.MatrixBootstrap;
import net.nifheim.matrix.common.plugin.MatrixPluginCommon;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.common.server.ServerPlatformInfo;
import net.nifheim.matrix.common.util.HTTPUtils;
import net.nifheim.matrix.paper.command.staff.BungeeTPCommand;
import net.nifheim.matrix.paper.command.staff.ReloadCommand;
import net.nifheim.matrix.paper.command.staff.StopCommand;
import net.nifheim.matrix.paper.command.user.SpitCommand;
import net.nifheim.matrix.paper.config.MatrixPaperConfiguration;
import net.nifheim.matrix.paper.messaging.ServerRequestListener;
import net.nifheim.matrix.paper.messaging.StaffChatListener;
import net.nifheim.matrix.paper.messaging.TargetedMessageListener;
import net.nifheim.matrix.paper.scheduler.PaperSchedulerAdapter;
import net.nifheim.matrix.paper.util.PaperPlayerMetaInjector;
import net.nifheim.matrix.paper.util.PluginsUtility;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spigotmc.SpigotConfig;

/**
 * @author Jaime Suárez
 */
public class MatrixPaper extends JavaPlugin implements MatrixBootstrap<Player> {


    private final ServerPlatformInfo serverPlatformInfo;
    private MatrixPaperConfiguration configuration;
    private MatrixPluginCommon<Player> matrixPlugin;
    private PluginsUtility pluginsUtility;
    private ServerRegisterMessage serverRegisterMessage;
    private PaperSchedulerAdapter scheduler;

    public MatrixPaper(ServerPlatformInfo serverPlatformInfo) {
        this.serverPlatformInfo = serverPlatformInfo;
    }

    @SuppressWarnings("removal")
    @Override
    public void onLoad() {
        GlobalConfiguration paperConfig = GlobalConfiguration.get();
        // set our default configurations
        paperConfig.proxies.velocity.enabled = true;
        paperConfig.proxies.velocity.onlineMode = true;
        paperConfig.console.enableBrigadierCompletions = false;
        paperConfig.console.enableBrigadierHighlighting = false;
        paperConfig.chunkLoadingAdvanced.autoConfigSendDistance = true;
        paperConfig.chunkSystem.ioThreads = 4;
        paperConfig.chunkSystem.workerThreads = -1;
        paperConfig.chunkSystem.genParallelism = "true";
        try {
            Class.forName("org.spigotmc.SpigotConfig").getField("debug");
            if (SpigotConfig.debug) {
                getPlatformLogger().warn("Debug is enabled in spigot config, forcing it to false.");
                SpigotConfig.debug = false;
            }
        } catch (@NotNull NoSuchFieldException | ClassNotFoundException ignore) {
        }
        saveResource("config.yml", false);
        configuration = new MatrixPaperConfiguration(this);
        matrixPlugin = new MatrixPluginCommon<>(this, configuration, new PaperPlayerMetaInjector(this), Bukkit::getPlayer);
        matrixPlugin.load();
    }

    @Override
    public void onEnable() {
        scheduler = new PaperSchedulerAdapter(this);
        try {
            matrixPlugin.enable();
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

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
        new SpitCommand(this);
        new ReloadCommand(this);
        new StopCommand(this, matrixPlugin.getServerInfo());
        new BungeeTPCommand(this, matrixPlugin.getServerManager());
        pluginsUtility = new PluginsUtility(getSLF4JLogger());

        serverRegisterMessage = new ServerRegisterMessage(matrixPlugin.getServerInfo());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            pluginsUtility.checkForPluginsToRemove();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPluginEnable(PluginEnableEvent e) {
                    pluginsUtility.checkForPluginsToRemove();
                }
            }, this);
            Bukkit.getOperators().forEach(op -> op.setOp(false)); // remove operators
            Arrays.stream(BanList.Type.values()).forEach(type -> Bukkit.getBanList(type).getEntries().forEach(banEntry -> banEntry.setExpiration(new Date()))); // expire vanilla bans
            if (getConfig().getString("vote.40servidoresmc") != null) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    try {
                        HTTPUtils.GET("http://40servidoresmc.es/api2.php?nombre=" + p.getName() + "&clave=" + getConfig().getString("vote.40servidoresmc"));
                    } catch (Exception ex) {
                        getPlatformLogger().warn("Can't send the vote for " + p.getName());
                    }
                });
            }
            if (serverRegisterMessage != null) {
                matrixPlugin.getMessaging().sendMessage(serverRegisterMessage);
            }
        });
        if (matrixPlugin.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
            try {
                Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
                if (GlobalConfiguration.get().collisions.enablePlayerCollisions) {
                    getPlatformLogger().warn("EnablePlayerCollisions is enabled in paper config, forcing it to false.");
                    GlobalConfiguration.get().collisions.enablePlayerCollisions = false;
                }
            } catch (ClassNotFoundException ignored) { // doesn't exists on spigot lol
            }
        }
        matrixPlugin.getMessaging().registerListener(new ServerRequestListener(this));
        matrixPlugin.getMessaging().registerListener(new StaffChatListener());
        matrixPlugin.getMessaging().registerListener(new TargetedMessageListener());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        if (!Bukkit.isStopping()) {
            Bukkit.shutdown();
        }
        matrixPlugin.disable();
        Bukkit.getScheduler().cancelTasks(this);
        CommandAPI.unregister(this);
    }

    public MatrixPaperConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Logger getPlatformLogger() {
        return getSLF4JLogger();
    }

    @Override
    public @NotNull SchedulerAdapter getScheduler() {
        return scheduler;
    }

    @Override
    public Environment getEnvironment() {
        return Environment.EnvironmentImpl.PAPER;
    }

    @Override
    public Audience getConsole() {
        return Bukkit.getConsoleSender();
    }

    @Override
    public String getVersion() {
        return getPluginMeta().getVersion();
    }

    @Override
    public MatrixPluginCommon<Player> getPlugin() {
        return matrixPlugin;
    }

    @Override
    public ServerPlatformInfo getServerPlatformInfo() {
        return serverPlatformInfo;
    }

    public ServerRegisterMessage getServerRegisterMessage() {
        return serverRegisterMessage;
    }

    private void loadManagers() {
        if (requirePlugin("ProtocolLib")) {
            getPlatformLogger().info("ProtocolLib found, hooking into it.");
            //new ProtocolLibManager(this);
        }
        if (requirePlugin("Vault")) {
            getPlatformLogger().info("Vault found, hooking into it.");
            //new VaultManager(this);
        }
        if (requirePlugin("PlaceholderAPI")) {
            getPlatformLogger().info("PlaceholderAPI found, hooking into it.");
        }
        if (requirePlugin("LuckPerms")) {
            getPlatformLogger().info("LuckPerms found, hooking into it.");
            //new LuckPermsManager(this);
        }
    }

    private boolean requirePlugin(String name) {
        if (Bukkit.getPluginManager().getPlugin(name) == null) {
            getPlatformLogger().error("Missing " + name);
            Bukkit.shutdown();
            return false;
        }
        return true;
    }

    private void registerEvents(@NotNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
