package io.github.beelzebu.matrix.api.player;

import io.github.beelzebu.coins.api.CoinsAPI;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public interface MatrixPlayer {

    default Optional<Statistics> getStatistics(String server) {
        return getStatistics().stream().filter(statistics -> server.equals(statistics.getServer())).findFirst();
    }

    default String getRedisKey() {
        return "user:" + getUniqueId();
    }

    default double getCoins() {
        return CoinsAPI.getCoins(getUniqueId());
    }

    default void setCoins(double coins) {
        CoinsAPI.setCoins(getUniqueId(), coins);
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

    String getLowercaseName();

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

    Date getLastLogin();

    void setLastLogin(Date lastLogin);

    Date getRegistration();

    Set<Statistics> getStatistics();

    void setStatistics(Statistics statistics);

    @Nullable
    String getDiscordId();

    void setDiscordId(@Nonnull @NonNull String discordId);

    int getCensoringLevel();

    void incrCensoringLevel();

    int getSpammingLevel();

    void incrSpammingLevel();

    MatrixPlayer save();

    void updateCached(String field);

    void saveToRedis();

    /**
     * Set the field with the given name to the given value without publishing an update to redis.
     *
     * @param field Field to update.
     * @param value Value to set to this field.
     */
    void setField(String field, Object value);
}
