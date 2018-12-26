package io.github.beelzebu.matrix.api.player;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public interface MatrixPlayer {

    default Optional<Statistics> getStatistics(String server) {
        return getStatistics().stream().filter(statistics -> server.equals(statistics.getServer())).findFirst();
    }

    default String getKey() {
        return "user:" + getUniqueId();
    }

    /**
     * Get the UUID of this user.
     *
     * @return Saved UUID of this user.
     */
    UUID getUniqueId();

    /**
     * Change the UUID of this user, it will also update the redis hashes to map uuid->name and vice versa.
     *
     * @param uniqueId New UUID to set.
     */
    void setUniqueId(UUID uniqueId);

    /**
     * Get the real name of this user.
     *
     * @return Saved Name of this user.
     */
    String getName();

    /**
     * Change the real name of this user, it will also update the redis hashes to map uuid->name and vice versa.
     *
     * @param name New UUID to set.
     */
    void setName(String name);

    /**
     * Get the displayed name of this user, this name is used in messages and chat.
     *
     * @return Saved display name of this user.
     */
    String getDisplayName();

    void setDisplayName(String displayName);

    boolean isPremium();

    void setPremium(boolean premium);

    boolean isAdmin();

    void setAdmin(boolean admin);

    String getSecret();

    void setSecret(String secret);

    String getIP();

    void setIP(String IP);

    Set<String> getIpHistory();

    ChatColor getChatColor();

    void setChatColor(ChatColor color);

    String getLastLocale();

    void setLastLocale(String lastLocale);

    String getStaffChannel();

    void setStaffChannel(String staffChannel);

    boolean isWatcher();

    void setWatcher(boolean watcher);

    Set<PlayerOptionType> getOptions();

    boolean getOption(PlayerOptionType option);

    void setOption(PlayerOptionType option, boolean status);

    boolean isAuthed();

    void setAuthed(boolean authed);

    long getExp();

    void setExp(long xp);

    double getCoins();

    void setCoins(double coins);

    Date getLastLogin();

    void setLastLogin(Date lastLogin);

    Set<Statistics> getStatistics();

    void setStatistics(Statistics statistics);

    MatrixPlayer save();

    void updateCached(String field);

    void saveToRedis();
}
