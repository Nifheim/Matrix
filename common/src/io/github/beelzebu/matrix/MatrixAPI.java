package io.github.beelzebu.matrix;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.database.MySQLStorage;
import io.github.beelzebu.matrix.database.RedisMessageEvent;
import io.github.beelzebu.matrix.database.RedisStorage;
import io.github.beelzebu.matrix.interfaces.IConfiguration;
import io.github.beelzebu.matrix.interfaces.IMethods;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.server.ServerInfo;
import io.github.beelzebu.matrix.utils.FileManager;
import io.github.beelzebu.matrix.utils.Messages;
import io.github.beelzebu.matrix.utils.MessagesManager;
import io.github.beelzebu.matrix.utils.PermsUtils;
import io.github.beelzebu.matrix.utils.ServerType;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public final class MatrixAPI {

    private static MatrixAPI instance;
    @Getter
    private final Gson gson = new Gson();
    @Getter
    private final Set<RedisMessageEvent> redisListeners = new HashSet<>();
    @Getter
    private IMethods methods;
    @Getter
    private final LoadingCache<UUID, MatrixPlayer> players = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.HOURS).build(new CacheLoader<UUID, MatrixPlayer>() {
        @Override
        public MatrixPlayer load(UUID k) throws NullPointerException {
            return methods.getPlayer(k);
        }
    });
    private FileManager fileManager;
    private MySQLStorage mysql;
    private RedisStorage redis;
    @Getter
    private Map<String, MessagesManager> messagesMap;
    private ServerInfo serverInfo;

    private MatrixAPI() { // Prevent accidental initialization.
    }

    /**
     * Get the core instance for this plugin.
     *
     * @return instance for this core.
     */
    public static MatrixAPI getInstance() {
        return instance == null ? instance = new MatrixAPI() : instance;
    }

    /**
     * Setup this core instance with the specified MethodInterface
     *
     * @param imethods instance to setup this core.
     * @see io.github.beelzebu.matrix.interfaces.IMethods
     */
    public void setup(IMethods imethods) {
        methods = imethods;
        messagesMap = new HashMap<>();
        fileManager = new FileManager(this);
        fileManager.generateFiles();
        fileManager.updateMessages();
        serverInfo = new ServerInfo(getConfig().getString("Server Table").replaceAll(" ", ""));
        serverInfo.setServerType(ServerType.valueOf(methods.getConfig().getString("Server Type").toUpperCase()));
        motd();
        try (Jedis jedis = getRedis().getPool().getResource()) {
            jedis.ping();
        } catch (JedisException ex) {
            debug(ex);
        }
    }

    /**
     * Shutdown this core instance.
     */
    public void shutdown() {
        motd();
    }

    /**
     * Get the MySQL database.
     *
     * @return instance used for managing the MySQL database.
     */
    public MySQLStorage getMySQL() {
        return mysql != null ? mysql : (mysql = new MySQLStorage(this));
    }

    /**
     * Get the Redis database.
     *
     * @return instance used for managing the Redis database.
     */
    public RedisStorage getRedis() {
        return redis != null ? redis : (redis = new RedisStorage(this));
    }

    /**
     * Add colors to the message and replace default placeholders.
     *
     * @param message string to replace color codes and placeholders.
     * @return message with colors and replaced placeholders.
     */
    public String rep(String message) {
        return message.replaceAll("%prefix%", "&a&lMatrix &8&l>&7").replaceAll("&", "§");
    }

    /**
     * Remove colors from a message and "Debug: "
     *
     * @param message message to remove colors.
     * @return a plain message without colors.
     */
    public String removeColor(String message) {
        return ChatColor.stripColor(message).replaceAll("Debug: ", "");
    }

    /**
     * Get the config loaded by methods interface.
     *
     * @return config loaded from methods interface.
     */
    public IConfiguration getConfig() {
        return methods.getConfig();
    }

    /**
     * Get the messages file for the specified lang.
     *
     * @param lang locale for the messages file.
     * @return messages file for the requested lang, if the file doesn't exists,
     * return the default messages file.
     */
    public MessagesManager getMessages(String lang) {
        if (!messagesMap.containsKey(lang.split("_")[0])) {
            messagesMap.put(lang.split("_")[0], methods.getMessages(lang.split("_")[0]));
        }
        return messagesMap.get(lang.split("_")[0]);
    }

    /**
     * Get a String from the specified path in the lang file.
     *
     * @param path where is located the message.
     * @param lang for what lang you need the message.
     * @return
     */
    public String getString(String path, String lang) {
        try {
            return rep(getMessages(lang).getString(path));
        } catch (NullPointerException ex) {
            log("The string " + path + " does not exists in messages_" + lang.split("_")[0] + ".yml");
            path = getMessages("").getString(path);
            debug(ex);
        }
        return rep(path);
    }

    public String getString(Messages message, String lang, String... parameters) {
        return rep(getMessages(lang).getString(message.getPath(), message.getDefaults()));
    }

    /**
     * The folder where the plugin data is stored.
     *
     * @return The File representing the folder.
     */
    public File getDataFolder() {
        return methods.getDataFolder();
    }

    public String getDisplayName(UUID player, boolean withPrefix) {
        String name;
        try (Jedis jedis = getRedis().getPool().getResource()) {
            name = gson.fromJson(jedis.hget("ncore_data", player.toString()), JsonObject.class).get("displayname").getAsString();
        } catch (Exception ex) {
            log("An unknown exception has ocurred in getDisplayName for " + player);
            if (methods.isOnline(player, true)) {
                name = methods.getNick(player);
            } else {
                try {
                    name = getNick(player);
                } catch (Exception ex2) {
                    return "";
                }
            }
        }
        return (withPrefix ? PermsUtils.getPrefix(player) + name : name).replace('&', '§');
    }

    private void motd() {
        methods.sendMessage(methods.getConsole(), TextComponent.fromLegacyText(""));
        methods.sendMessage(methods.getConsole(), TextComponent.fromLegacyText(rep("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")));
        methods.sendMessage(methods.getConsole(), TextComponent.fromLegacyText(rep("        &4Matrix &fBy: &7Beelzebu")));
        methods.sendMessage(methods.getConsole(), TextComponent.fromLegacyText(rep("")));
        StringBuilder version = new StringBuilder();
        int spaces = (48 - ("v: " + methods.getVersion()).length()) / 2;
        for (int i = 0; i < spaces; i++) {
            version.append(" ");
        }
        version.append(rep("&4v: &f" + methods.getVersion()));
        methods.sendMessage(methods.getConsole(), TextComponent.fromLegacyText(rep(version.toString())));
        methods.sendMessage(methods.getConsole(), TextComponent.fromLegacyText(rep("&6-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")));
    }

    public void log(Object msg) {
        methods.log(rep(msg.toString()));
    }

    public void log(SQLException ex) {
        methods.log("SQLException: ");
        methods.log("   Database state: " + ex.getSQLState());
        methods.log("   Error code: " + ex.getErrorCode());
        methods.log("   Error message: " + ex.getMessage());
    }

    public void debug(Object msg) {
        methods.log("§cDebug: §7" + msg);
    }

    public String getNick(UUID uuid) {
        if (methods.isOnline(uuid, true)) {
            return methods.getNick(uuid);
        }
        if (getRedis().isRegistred(uuid)) {
            return getRedis().getName(uuid);
        }
        return null;
    }

    public UUID getUUID(String name, boolean fromdb) {
        if (methods.isOnline(name, true) && !fromdb) {
            return methods.getUUID(name);
        }
        if (getRedis().isRegistred(name)) {
            return getRedis().getUUID(name);
        }
        return null;
    }

    public UUID getUUID(String name) {
        return getUUID(name, false);
    }

    public boolean isBungee() {
        try {
            Class.forName("net.md_5.bungee.api.plugin.Plugin");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public MatrixPlayer getPlayer(UUID uuid) {
        try {
            return players.get(uuid);
        } catch (ExecutionException | NullPointerException ex) {
            Logger.getLogger(MatrixAPI.class.getName()).log(Level.SEVERE, ex.getMessage());
            return null;
        }
    }

    public void registerRedisListener(RedisMessageEvent event) {
        if (redisListeners.contains(event)) {
            throw new RuntimeException("Listener already registred");
        }
        redisListeners.add(event);
    }
}
