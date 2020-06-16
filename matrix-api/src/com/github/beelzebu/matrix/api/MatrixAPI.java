package com.github.beelzebu.matrix.api;

import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.api.database.MatrixDatabase;
import com.github.beelzebu.matrix.api.database.SQLDatabase;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.messaging.Messaging;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.util.MatrixPlayerSet;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
        MatrixPlayer matrixPlayer = getCache().getPlayer(uniqueId).orElse(getDatabase().getPlayer(uniqueId));
        players.add(matrixPlayer);
        return matrixPlayer;
    }

    public MatrixPlayer getPlayer(String name) {
        for (MatrixPlayer matrixPlayer : players) {
            if (matrixPlayer.getName() == null) {
                Matrix.getLogger().info("Null name for: " + matrixPlayer.getUniqueId());
                continue;
            }
            if (Objects.equals(matrixPlayer.getName().toLowerCase(), name.toLowerCase())) {
                return matrixPlayer;
            }
        }
        MatrixPlayer matrixPlayer = getCache().getPlayer(name).orElse(getDatabase().getPlayer(name));
        players.add(matrixPlayer);
        return matrixPlayer;
    }

    /**
     * Get the messages file for the specified lang.
     *
     * @param locale locale for the messages file.
     * @return messages file for the requested lang, if the file doesn't exists,
     * return the default messages file.
     * @deprecated Use {@link com.github.beelzebu.matrix.api.i18n.I18n}
     */
    @Deprecated
    public final AbstractConfig getMessages(String locale) {
        return I18n.getMessagesFile(locale.split("_")[0]);
    }

    /**
     * @deprecated Use {@link com.github.beelzebu.matrix.api.i18n.I18n}
     */
    @Deprecated
    public final String getString(String path, String locale) {
        return StringUtils.replace(getMessages(locale.split("_")[0]).getString(path, StringUtils.replace(getMessages(I18n.DEFAULT_LOCALE).getString(path, ""))));
    }

    /**
     * @deprecated Use {@link com.github.beelzebu.matrix.api.i18n.I18n}
     */
    @Deprecated
    public final String getString(Message message, String lang, String... parameters) {
        return I18n.tl(message, lang);
    }

    /**
     * @deprecated Use {@link com.github.beelzebu.matrix.api.i18n.I18n}
     */
    @Deprecated
    public Map<String, AbstractConfig> getMessagesMap() {
        return messagesMap;
    }

    public Set<MatrixPlayer> getPlayers() {
        return players;
    }

    public abstract Messaging getMessaging();

    public abstract CacheProvider getCache();

    public abstract MatrixDatabase getDatabase();

    public abstract SQLDatabase getSQLDatabase();

    public abstract MatrixPlugin getPlugin();

    public abstract ServerInfo getServerInfo();

    public abstract boolean hasPermission(MatrixPlayer player, String permission);

    protected abstract void initI18n();
}
