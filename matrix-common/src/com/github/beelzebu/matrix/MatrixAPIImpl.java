package com.github.beelzebu.matrix;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.database.SQLDatabase;
import com.github.beelzebu.matrix.api.messaging.RedisMessaging;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.cache.CacheProviderImpl;
import com.github.beelzebu.matrix.database.MongoStorage;
import com.github.beelzebu.matrix.database.MySQLStorage;
import com.github.beelzebu.matrix.util.FileManager;
import java.io.File;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Beelzebu
 */
public abstract class MatrixAPIImpl extends MatrixAPI {

    private final MatrixPlugin plugin;
    private final MongoStorage database;
    private final RedisMessaging redis;
    private final CacheProviderImpl cache;
    private final ServerInfo serverInfo;
    private final MySQLStorage mySQLStorage;

    public MatrixAPIImpl(MatrixPlugin plugin) {
        this.plugin = plugin;
        database = new MongoStorage(plugin.getConfig().getString("Database.Host"), 27017, "admin", "matrix", plugin.getConfig().getString("Database.Password"), "admin");
        redis = new RedisMessaging(getConfig().getString("Redis.Host"), getConfig().getInt("Redis.Port"), getConfig().getString("Redis.Password"), runnable -> getPlugin().runAsync(runnable));
        cache = new CacheProviderImpl(redis.getPool());
        serverInfo = new ServerInfo(
                plugin.getConfig().getString("server-info.group"),
                plugin.getConfig().getString("server-info.name", plugin.getConfig().getString("Server Table")).replaceAll(" ", ""),
                GameType.valueOf(plugin.getConfig().getString("server-info.game-type", "NONE").toUpperCase()),
                ServerType.valueOf(plugin.getConfig().getString("server-info.server-type", plugin.getConfig().getString("Server Type")).toUpperCase())
        );
        mySQLStorage = new MySQLStorage(plugin, plugin.getConfig().getString("mysql.host"), plugin.getConfig().getInt("mysql.port"), plugin.getConfig().getString("mysql.database"), plugin.getConfig().getString("mysql.user"), plugin.getConfig().getString("mysql.password"), plugin.getConfig().getInt("mysql.pool", 8));
        messagesMap.put("default", plugin.getFileAsConfig(new File(plugin.getDataFolder(), "messages.yml")));
        for (File file : plugin.getDataFolder().listFiles()) {
            if (!file.getName().startsWith("messages_")) {
                continue;
            }
            String locale = file.getName().split("_")[1].replaceFirst("\\.yml", "");
            messagesMap.put(locale, plugin.getFileAsConfig(file));
        }
        Matrix.getLogger().init(this);
    }

    public String getName(UUID uniqueId) {
        return getPlayer(uniqueId).getName();
    }

    public UUID getUniqueId(String name) {
        return getPlayer(name).getUniqueId();
    }

    @Override
    public abstract boolean hasPermission(MatrixPlayer player, String permission);

    public MatrixPlugin getPlugin() {
        return plugin;
    }

    public MongoStorage getDatabase() {
        return database;
    }

    public RedisMessaging getMessaging() {
        return redis;
    }

    public CacheProviderImpl getCache() {
        return cache;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
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

    /**
     * Setup this api instance
     */
    protected void setup() {
        try {
            FileManager fileManager = new FileManager(plugin);
            fileManager.generateFiles();
            fileManager.updateMessages();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        motd();
    }

    /**
     * Shutdown this api instance.
     */
    void shutdown() {
        motd();
    }

    @Override
    public SQLDatabase getSQLDatabase() {
        return mySQLStorage;
    }

    public final SchedulerAdapter getScheduler() {
        return plugin.getBootstrap().getScheduler();
    }
}
