package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.config.MatrixConfig;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.messaging.RedisMessaging;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.api.server.ServerInfo;
import io.github.beelzebu.matrix.api.util.MatrixPlayerSet;
import io.github.beelzebu.matrix.api.util.StringUtils;
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
import redis.clients.jedis.exceptions.JedisException;

/**
 * TODO: add support for NTNBAN
 *
 * @author Beelzebu
 */
public abstract class MatrixAPI {

    protected final Map<String, AbstractConfig> messagesMap = new HashMap<>();
    private final Set<MatrixPlayer> players = new MatrixPlayerSet<>();

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
    @Deprecated
    public final String rep(String message) {
        return StringUtils.replace(message);
    }

    /**
     * Remove colors from a message and "Debug: "
     *
     * @param message message to remove colors.
     * @return a plain message without colors.
     */
    @Deprecated
    public final String removeColor(String message) {
        return StringUtils.removeColor(message);
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
        for (MatrixPlayer matrixPlayer : players) {
            if (Objects.equals(matrixPlayer.getUniqueId(), uniqueId)) {
                return matrixPlayer;
            }
        }
        return getCache().getPlayer(uniqueId).orElse(getDatabase().getPlayer(uniqueId));
    }

    public MatrixPlayer getPlayer(String name) {
        for (MatrixPlayer matrixPlayer : players) {
            if (Objects.equals(matrixPlayer.getName().toLowerCase(), name.toLowerCase())) {
                return matrixPlayer;
            }
        }
        return getCache().getPlayer(name).orElse(getDatabase().getPlayer(name));
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
        return StringUtils.replace(getMessages(locale).getString(path, StringUtils.replace(getMessages("").getString(path, ""))));
    }

    public final String getString(Message message, String lang, String... parameters) {
        return rep(getMessages(lang).getString(message.getPath(), message.getDefaults()));
    }

    // Logging and debugging
    @Deprecated
    public final void log(String message) {
        Matrix.getLogger().info(rep(message));
    }

    @Deprecated
    public final void debug(String message) {
        Matrix.getLogger().debug(message);
    }

    @Deprecated
    public final void debug(SQLException ex) {
        log("SQLException: ");
        log("   Database state: " + ex.getSQLState());
        log("   Error code: " + ex.getErrorCode());
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    @Deprecated
    public final void debug(JedisException ex) {
        log("JedisException: ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    @Deprecated
    public final void debug(Exception ex) {
        log(ex.getClass().getName() + ": ");
        log("   Error message: " + ex.getLocalizedMessage());
        log("   Stacktrace:\n" + getStacktrace(ex));
    }

    @Deprecated
    public Gson getGson() {
        return Matrix.GSON;
    }

    public Map<String, AbstractConfig> getMessagesMap() {
        return messagesMap;
    }

    public Set<MatrixPlayer> getPlayers() {
        return players;
    }

    @Deprecated
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
