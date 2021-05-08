package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.level.LevelProvider;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerManager;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerManager;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.cache.CacheProviderImpl;
import com.github.beelzebu.matrix.database.MatrixDatabaseImpl;
import com.github.beelzebu.matrix.database.StorageProvider;
import com.github.beelzebu.matrix.dependency.DependencyManager;
import com.github.beelzebu.matrix.dependency.DependencyRegistry;
import com.github.beelzebu.matrix.dependency.classloader.ReflectionClassLoader;
import com.github.beelzebu.matrix.logger.MatrixLoggerImpl;
import com.github.beelzebu.matrix.api.messaging.RedisMessaging;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.github.beelzebu.matrix.server.ServerManagerImpl;
import com.github.beelzebu.matrix.task.HeartbeatTask;
import com.github.beelzebu.matrix.util.FileManager;
import com.github.beelzebu.matrix.util.MaintenanceManager;
import com.github.beelzebu.matrix.util.MetaInjector;
import com.github.beelzebu.matrix.util.RedisManager;
import com.github.beelzebu.matrix.util.adapter.ChatColorTypeAdapter;
import com.github.beelzebu.matrix.util.adapter.ServerInfoTypeAdapter;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public abstract class MatrixAPIImpl <P> extends MatrixAPI<P> {

    public static final String DOMAIN_NAME = "mc.indiopikaro.net";
    public static final Set<String> DOMAIN_NAMES = ImmutableSet.of(DOMAIN_NAME, ".net", ".cl", ".com");
    private final MatrixPlugin plugin;
    private final MatrixDatabaseImpl database;
    private final RedisManager redisManager;
    private final RedisMessaging messaging;
    private final ServerInfo serverInfo;
    private final MaintenanceManager maintenanceManager;
    private final ServerManager serverManager;
    private final Map<String, AbstractConfig> messagesMap = new HashMap<>();
    private LevelProvider levelProvider;

    public MatrixAPIImpl(MatrixPlugin plugin) {
        GsonBuilder gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization()
                .registerTypeAdapter(ChatColor.class, new ChatColorTypeAdapter())
                .registerTypeAdapter(ServerInfoImpl.class, new ServerInfoTypeAdapter())
                .registerTypeAdapter(ServerInfo.class, new ServerInfoTypeAdapter())
                .setDateFormat(DateFormat.LONG);
        if (plugin.getConfig().getBoolean("Debug")) {
            gsonBuilder.setPrettyPrinting();
        }
        Matrix.GSON = gsonBuilder.create();
        this.plugin = plugin;
        plugin.getDataFolder().mkdirs();
        DependencyManager dependencyManager = new DependencyManager(plugin, new ReflectionClassLoader(plugin.getBootstrap()), new DependencyRegistry());
        dependencyManager.loadInternalDependencies();
        Matrix.setLogger(new MatrixLoggerImpl(plugin.getConsole(), plugin.getConfig().getBoolean("Debug")));
        Matrix.getLogger().info("Initializing redis manager...");
        redisManager = new RedisManager(getConfig().getString("Redis.Host"), getConfig().getInt("Redis.Port"), getConfig().getString("Redis.Password"));
        Matrix.getLogger().info("Redis manager initialized!");
        Matrix.getLogger().info("Initializing database manager..");
        database = new MatrixDatabaseImpl(new StorageProvider(this), new CacheProviderImpl(this), plugin.getBootstrap().getScheduler());
        Matrix.getLogger().info("Database manager initialized!");
        Matrix.getLogger().info("Initializing messaging service...");
        messaging = new RedisMessaging(redisManager, r -> plugin.getBootstrap().getScheduler().executeAsync(r));
        Matrix.getLogger().info("Messaging service initialized!");
        Matrix.getLogger().info("Initializing maintenance manager...");
        maintenanceManager = new MaintenanceManager(redisManager);
        Matrix.getLogger().info("Maintenance manager initialized!");
        Matrix.getLogger().info("Providing Matrix API instance...");
        Matrix.setAPI(this);
        Matrix.getLogger().info("Initializing server manager...");
        serverManager = new ServerManagerImpl(this);
        Matrix.getLogger().info("Server manager initialized!");
        Matrix.getLogger().info("Creating server info for current server...");
        if (plugin.getConfig().getString("server-info.game-mode") == null) {
            Matrix.getLogger().info("server-info.game-mode config option is missing, please add it to config.yml");
        }
        serverInfo = new ServerInfoImpl(
                ServerType.valueOf(plugin.getConfig().getString("server-info.server-type", plugin.getConfig().getString("Server Type")).toUpperCase()),
                plugin.getConfig().getString("server-info.group", null),
                plugin.getConfig().getString("server-info.name", null),
                plugin.getConfig().get("server-info.game-mode") != null ? GameMode.valueOf(plugin.getConfig().getString("server-info.game-mode").toUpperCase()) : null,
                plugin.getConfig().getBoolean("server-info.unique", false)
        );
        Matrix.getLogger().info("Server info created!");
        Matrix.getLogger().info("Matrix API is now fully initialized.");
    }

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }

    @Override
    public RedisMessaging getMessaging() {
        return messaging;
    }

    @Override
    public MatrixDatabaseImpl getDatabase() {
        return database;
    }

    public MatrixPlugin getPlugin() {
        return plugin;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public ServerManager getServerManager() {
        return serverManager;
    }

    @Override
    public abstract PlayerManager<P> getPlayerManager();

    @Override
    public abstract boolean hasPermission(MatrixPlayer player, String permission);

    @Override
    protected void initI18n() {
        new I18n(messagesMap);
    }

    @Override
    public void reload() {
        getConfig().reload();
        loadMessages();
        ((MatrixLoggerImpl) Matrix.getLogger()).setDebug(getConfig().getBoolean("Debug"));
        Matrix.getLogger().info("Reloaded config and messages.");
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public abstract MetaInjector<P> getMetaInjector();

    /**
     * Setup this api instance
     */
    protected void setup() {
        loadMessages();
        motd();
        plugin.getBootstrap().getScheduler().asyncRepeating(new HeartbeatTask(this), 1, TimeUnit.MINUTES);
    }

    private void motd() {
        plugin.getConsole().sendMessage("");
        plugin.getConsole().sendMessage(StringUtils.replace("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
        plugin.getConsole().sendMessage(StringUtils.replace("        &4Matrix &fBy: &7Beelzebu"));
        plugin.getConsole().sendMessage("");
        StringBuilder version = new StringBuilder();
        int spaces = (48 - ("v: " + plugin.getVersion()).length()) / 2;
        for (int i = 0; i < spaces; i++) {
            version.append(" ");
        }
        version.append(StringUtils.replace("&4v: &f" + plugin.getVersion()));
        plugin.getConsole().sendMessage(StringUtils.replace(version.toString()));
        plugin.getConsole().sendMessage(StringUtils.replace("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
        Matrix.getLogger().info("&7Server Info:");
        Matrix.getLogger().info("&7Group: &6" + getServerInfo().getGroupName() + " &7Name: &6" + getServerInfo().getServerName());
        Matrix.getLogger().info("&7ServerType: &6" + getServerInfo().getServerType() + " &7GameMode: &6" + getServerInfo().getDefaultGameMode() + " &7Lobby: &6" + getServerInfo().getLobbyServer().join());
    }

    private void loadMessages() {
        messagesMap.clear();
        try {
            FileManager fileManager = new FileManager(plugin);
            fileManager.generateFiles();
            fileManager.updateMessages();
            // move messages files to right folder
            File messagesFolder = new File(plugin.getDataFolder(), "messages");
            if (!messagesFolder.exists()) {
                messagesFolder.mkdirs();
            }
            if (!messagesFolder.isDirectory()) {
                messagesFolder.delete();
                messagesFolder.mkdirs();
            }
            for (File file : plugin.getDataFolder().listFiles()) {
                if (!file.getName().startsWith("messages_")) {
                    continue;
                }
                try {
                    Files.move(file.toPath(), new File(messagesFolder, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // load messages translations
            for (File file : messagesFolder.listFiles()) {
                if (!file.getName().startsWith("messages_")) {
                    continue;
                }
                String locale = file.getName().split("_")[1].replaceFirst("\\.yml", "");
                messagesMap.put(locale, plugin.getFileAsConfig(file));
            }
            initI18n();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdown this api instance.
     */
    void shutdown() {
        motd();
    }

    @Override
    public LevelProvider getLevelProvider() {
        if (levelProvider == null) {
            throw new UnsupportedOperationException();
        }
        return levelProvider;
    }

    @Override
    public void setLevelProvider(LevelProvider levelProvider) {
        this.levelProvider = levelProvider;
    }
}
