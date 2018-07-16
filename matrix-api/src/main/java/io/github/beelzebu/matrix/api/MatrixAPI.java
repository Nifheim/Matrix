package io.github.beelzebu.matrix.api;

import com.google.gson.Gson;
import io.github.beelzebu.matrix.api.cache.CacheProvider;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.messaging.RedisMessageEvent;
import io.github.beelzebu.matrix.api.messaging.RedisMessaging;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.api.server.ServerInfo;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
@Getter
public abstract class MatrixAPI {

    private final Gson gson = new Gson();
    private final Set<RedisMessageEvent> redisListeners = new HashSet<>();
    private final Map<String, AbstractConfig> messagesMap = new HashMap<>();

    public abstract RedisMessaging getRedis();

    public abstract CacheProvider getCache();

    public abstract MatrixDatabase getDatabase();

    public abstract void log(String message);

    public abstract void debug(String message);

    public abstract void debug(Exception ex);

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

    /**
     * Get a String from the specified path in the lang file.
     *
     * @param path where is located the message.
     * @param lang for what lang you need the message.
     * @return
     */
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

}
