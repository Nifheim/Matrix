package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.config.MatrixConfig;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.messaging.RedisMessageEvent;
import io.github.beelzebu.matrix.api.messaging.RedisMessaging;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.api.server.ServerInfo;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    protected final Map<String, AbstractConfig> messagesMap = new HashMap<>();
    private final Set<RedisMessageEvent> redisListeners = new HashSet<>();
    private final Set<MatrixPlayer> players = new HashSet<>();

    /**
     * Get matrix configuration file.
     *
     * @return matrix configuration file, null if it isn't loaded yet.
     */
    public final MatrixConfig getConfig() {
        return getPlugin().getConfig();
    }

    /**
     * Add colors to the message and replace default placeholders.
     *
     * @param message string to replace color codes and placeholders.
     * @return message with colors and replaced placeholders.
     */
    public final String rep(String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", "&a&lMatrix &8&l>&7"));
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

    public MatrixPlayer getPlayer(UUID uniqueId) {
        return players.stream().filter(p -> Objects.equals(p.getUniqueId(), uniqueId)).findFirst().orElse(getCache().getPlayer(uniqueId).orElse(getDatabase().getPlayer(uniqueId)));
    }

    public MatrixPlayer getPlayer(String name) {
        return players.stream().filter(p -> Objects.equals(p.getName(), name)).findFirst().orElse(getCache().getPlayer(name).orElse(getDatabase().getPlayer(name)));
    }

    /**
     * Get the messages file for the specified lang.
     *
     * @param locale locale for the messages file.
     * @return messages file for the requested lang, if the file doesn't exists,
     * return the default messages file.
     */
    public final AbstractConfig getMessages(String locale) {
        return Optional.ofNullable(messagesMap.get(locale.split("_")[0])).orElse(messagesMap.get("default"));
    }


    public final String getString(String path, String locale) {
        return rep(getMessages(locale).getString(path, rep(getMessages("").getString(path, ""))));
    }

    public final String getString(Message message, String lang, String... parameters) {
        return rep(getMessages(lang).getString(message.getPath(), message.getDefaults()));
    }

    // Logging and debugging
    // TODO: move this to a logger class.
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
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    public final void debug(JedisException ex) {
        log("JedisException: ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    public final void debug(Exception ex) {
        log(ex.getClass().getName() + ": ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    @Deprecated
    public Gson getGson() {
        return Matrix.GSON;
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

    public abstract RedisMessaging getRedis();

    public abstract CacheProvider getCache();

    public abstract MatrixDatabase getDatabase();

    public abstract MatrixPlugin getPlugin();

    public abstract ServerInfo getServerInfo();

    public abstract boolean hasPermission(MatrixPlayer player, String permission);
}
