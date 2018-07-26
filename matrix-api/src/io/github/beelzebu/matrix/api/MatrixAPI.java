package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.messaging.RedisMessageEvent;
import io.github.beelzebu.matrix.api.messaging.RedisMessaging;
import io.github.beelzebu.matrix.api.player.IStatistics;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.StatsAdapter;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.api.server.ServerInfo;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
@Getter
public abstract class MatrixAPI {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(IStatistics.class, new StatsAdapter()).create();
    private final Set<RedisMessageEvent> redisListeners = new HashSet<>();
    private final Map<String, AbstractConfig> messagesMap = new HashMap<>();

    public abstract RedisMessaging getRedis();

    public abstract CacheProvider getCache();

    public abstract MatrixDatabase getDatabase();

    public abstract MatrixPlugin getPlugin();

    public abstract ServerInfo getServerInfo();

    public final AbstractConfig getConfig() {
        return getPlugin().getConfig();
    }

    /**
     * Add colors to the message and replace default placeholders.
     *
     * @param message string to replace color codes and placeholders.
     * @return message with colors and replaced placeholders.
     */
    public final String rep(String message) {
        return message.replaceAll("%prefix%", "&a&lMatrix &8&l>&7").replaceAll("&", "§");
    }

    /**
     * Remove colors from a message and "Debug: "
     *
     * @param message message to remove colors.
     * @return a plain message without colors.
     */
    public final String removeColor(String message) {
        return ChatColor.stripColor(message).replaceAll("Debug: ", "");
    }

    public final void registerRedisListener(RedisMessageEvent event) {
        if (redisListeners.contains(event)) {
            throw new RuntimeException("Listener already registered");
        }
        redisListeners.add(event);
    }

    public final boolean isBungee() {
        try {
            Class.forName("net.md_5.bungee.api.plugin.Plugin");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public final MatrixPlayer getPlayer(UUID uniqueId) {
        return getCache().getPlayer(uniqueId).orElse(getDatabase().getPlayer(uniqueId));
    }

    public final MatrixPlayer getPlayer(String name) {
        return getCache().getPlayer(name).orElse(getDatabase().getPlayer(name));
    }

    public final Set<MatrixPlayer> getPlayers() {
        return getCache().getPlayers();
    }

    /**
     * Get the messages file for the specified lang.
     *
     * @param lang locale for the messages file.
     * @return messages file for the requested lang, if the file doesn't exists,
     * return the default messages file.
     */
    public final AbstractConfig getMessages(String lang) {
        if (!messagesMap.containsKey(lang.split("_")[0])) {
            messagesMap.put(lang.split("_")[0], getPlugin().getFileAsConfig(new File(getPlugin().getDataFolder(), "messages_" + lang.split("_")[0])));
        }
        return messagesMap.get(lang.split("_")[0]);
    }

    public final String getString(String path, String lang) {
        try {
            return rep(getMessages(lang).getString(path));
        } catch (NullPointerException ex) {
            log("The string " + path + " does not exists in messages_" + lang.split("_")[0] + ".yml");
            path = getMessages("").getString(path);
            debug(ex);
        }
        return rep(path);
    }

    public final String getString(Messages message, String lang, String... parameters) {
        return rep(getMessages(lang).getString(message.getPath(), message.getDefaults()));
    }

    // Logging and debugging

    public final void log(String message) {
        getPlugin().log(rep(message));
    }

    public final void debug(String message) {
        log("&cDebug: &7" + message);
    }

    public final void debug(SQLException ex) {
        log("SQLException: ");
        log("   Database state: " + ex.getSQLState());
        log("   Error code: " + ex.getErrorCode());
        log("   Error message: " + ex.getMessage());
        log("   Stacktrace: " + getStacktrace(ex));
    }

    public final void debug(JedisException ex) {
        log("JedisException: ");
        log("   Error message: " + ex.getMessage());
        log("   Stacktrace: " + getStacktrace(ex));
    }

    public final void debug(Exception ex) {
        log(ex.getClass().getName() + ": ");
        log("   Error message: " + ex.getMessage());
        log("   Stacktrace: " + getStacktrace(ex));
    }

    private String getStacktrace(Exception ex) {
        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            ex.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error getting the stacktrace";
    }
}
