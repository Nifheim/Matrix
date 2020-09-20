package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.cache.CacheProviderImpl;
import com.github.beelzebu.matrix.database.StorageImpl;
import com.github.beelzebu.matrix.dependency.DependencyManager;
import com.github.beelzebu.matrix.dependency.DependencyRegistry;
import com.github.beelzebu.matrix.dependency.classloader.ReflectionClassLoader;
import com.github.beelzebu.matrix.logger.MatrixLoggerImpl;
import com.github.beelzebu.matrix.messaging.RedisMessaging;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.github.beelzebu.matrix.task.HeartbeatTask;
import com.github.beelzebu.matrix.util.FileManager;
import com.github.beelzebu.matrix.util.MaintenanceManager;
import com.github.beelzebu.matrix.util.RedisManager;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
public abstract class MatrixAPIImpl extends MatrixAPI {

    public static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().registerTypeAdapter(ChatColor.class, new TypeAdapter<ChatColor>() {
        public void write(JsonWriter out, ChatColor value) throws IOException {
            out.value(value.name());
        }

        public ChatColor read(JsonReader in) throws IOException {
            return ChatColor.valueOf(in.nextString());
        }
    }).registerTypeAdapter(ServerInfo.class, new TypeAdapter<ServerInfo>() {
        @Override
        public void write(JsonWriter out, ServerInfo value) throws IOException {
            out.beginObject().name("groupName").value(value.getGroupName())
                    .name("serverName").value(value.getServerName())
                    .name("gameType").value(value.getGameType().toString())
                    .name("serverType").value(value.getServerType().name())
                    .name("gameMode").value(value.getDefaultGameMode().toString())
                    .endObject();
        }

        @Override
        public ServerInfo read(JsonReader in) throws IOException {
            JsonObject jsonObject = new JsonParser().parse(in).getAsJsonObject();
            return new ServerInfoImpl(
                    GameType.valueOf(jsonObject.get("gameType").getAsString()),
                    ServerType.valueOf(jsonObject.get("serverType").getAsString()),
                    jsonObject.get("groupName").getAsString(),
                    jsonObject.get("serverName").getAsString(),
                    GameMode.valueOf(jsonObject.get("gameMode").getAsString())
            );
        }
    }).setDateFormat("MMM dd, yyyy h:mm:ss aa").create();
    public static final String DOMAIN_NAME = "mc.indiopikaro.net";
    public static final Set<String> DOMAIN_NAMES = ImmutableSet.of("indiopikaro.cl", "indiopikaro.net", "indiopikaro.com", "mineperu.net", "mineperu.com", "pixelnetwork.net", "osornorp.cl", "onyxmc.net", "mag3.gg", "chiletumare.cl");
    private final MatrixPlugin plugin;
    private final StorageImpl database;
    private final RedisManager redisManager;
    private final RedisMessaging messaging;
    private final CacheProviderImpl cache;
    private final ServerInfo serverInfo;
    private final MaintenanceManager maintenanceManager;

    public MatrixAPIImpl(MatrixPlugin plugin) {
        Matrix.GSON = GSON;
        this.plugin = plugin;
        plugin.getDataFolder().mkdirs();
        DependencyManager dependencyManager = new DependencyManager(plugin, new ReflectionClassLoader(plugin.getBootstrap()), new DependencyRegistry());
        dependencyManager.loadInternalDependencies();
        database = new StorageImpl(this);
        //database = new MongoStorage(plugin.getConfig().getString("Database.Host"), 27017, "admin", "matrix", plugin.getConfig().getString("Database.Password"), "admin");
        redisManager = new RedisManager(getConfig().getString("Redis.Host"), getConfig().getInt("Redis.Port"), getConfig().getString("Redis.Password"));
        messaging = new RedisMessaging(redisManager, plugin::runAsync);
        cache = new CacheProviderImpl(redisManager);
        //mySQLStorage = new MySQLStorage(this, plugin.getConfig().getString("mysql.host"), plugin.getConfig().getInt("mysql.port"), plugin.getConfig().getString("mysql.database"), plugin.getConfig().getString("mysql.user"), plugin.getConfig().getString("mysql.password"), plugin.getConfig().getInt("mysql.pool", 8));
        Matrix.setLogger(new MatrixLoggerImpl(plugin.getConsole(), plugin.getConfig().getBoolean("Debug")));
        maintenanceManager = new MaintenanceManager(redisManager);
        if (plugin.getConfig().getString("server-info.game-mode") == null) {
            Matrix.getLogger().info("server-info.game-mode config option is missing, please add it to config.yml");
        }
        Matrix.setAPI(this);
        serverInfo = new ServerInfoImpl(
                GameType.valueOf(plugin.getConfig().getString("server-info.game-type", "NONE").toUpperCase()),
                ServerType.valueOf(plugin.getConfig().getString("server-info.server-type", plugin.getConfig().getString("Server Type")).toUpperCase()),
                plugin.getConfig().getString("server-info.group", null),
                plugin.getConfig().get("server-info.game-mode") != null ? GameMode.valueOf(plugin.getConfig().getString("server-info.game-mode").toUpperCase()) : null
        );
    }

    public String getName(UUID uniqueId) {
        return getPlayer(uniqueId).getName();
    }

    public UUID getUniqueId(String name) {
        return getPlayer(name).getUniqueId();
    }

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }

    @Override
    public RedisMessaging getMessaging() {
        return messaging;
    }

    @Override

    public CacheProviderImpl getCache() {
        return cache;
    }

    @Override
    public StorageImpl getDatabase() {
        return database;
    }

    public MatrixPlugin getPlugin() {
        return plugin;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

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

    public final SchedulerAdapter getScheduler() {
        return plugin.getBootstrap().getScheduler();
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    /**
     * Setup this api instance
     */
    protected void setup() {
        loadMessages();
        motd();
        getScheduler().asyncRepeating(() -> new HeartbeatTask(this), 1, TimeUnit.MINUTES);
    }

    private void motd() {
        plugin.getConsole().sendMessage("");
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(StringUtils.replace("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(StringUtils.replace("        &4Matrix &fBy: &7Beelzebu")));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(StringUtils.replace("")));
        StringBuilder version = new StringBuilder();
        int spaces = (48 - ("v: " + plugin.getVersion()).length()) / 2;
        for (int i = 0; i < spaces; i++) {
            version.append(" ");
        }
        version.append(StringUtils.replace("&4v: &f" + plugin.getVersion()));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(StringUtils.replace(version.toString())));
        plugin.sendMessage(plugin.getConsole(), TextComponent.fromLegacyText(StringUtils.replace("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")));
        Matrix.getLogger().info("&7Server Info:");
        Matrix.getLogger().info("&7Group: &6" + getServerInfo().getGroupName() + " &7Name: &6" + getServerInfo().getServerName());
        Matrix.getLogger().info("&7ServerType: &6" + getServerInfo().getServerType() + " &7GameType: &6" + getServerInfo().getGameType());
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
}
