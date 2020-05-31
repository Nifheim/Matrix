package io.github.beelzebu.matrix.api.player;

import com.github.beelzebu.coins.api.CoinsAPI;
import io.github.beelzebu.matrix.api.command.CommandSource;
import io.github.beelzebu.matrix.api.server.GameType;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public interface MatrixPlayer extends CommandSource {

    default String getRedisKey() {
        return "user:" + getUniqueId();
    }

    default double getCoins() {
        return CoinsAPI.getCoins(getUniqueId());
    }

    default void setCoins(double coins) {
        CoinsAPI.setCoins(getUniqueId(), coins);
    }

    default boolean hasPlayed(GameType gameType) {
        return getPlayedGames().contains(gameType);
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
    @Override
    String getName();

    /**
     * Change the real name of this user, it will also update the redis hashes to map uuid->name and vice versa.
     *
     * @param name New UUID to set.
     */
    void setName(String name);

    @Override
    void execute(String command);

    @Override
    void sendMessage(String message);

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

    boolean isRegistered();

    void setRegistered(boolean registered);

    boolean isAdmin();

    void setAdmin(boolean admin);

    String getSecret();

    void setSecret(String secret);

    String getHashedPassword();

    void setHashedPassword(String hashedPassword);

    boolean isLoggedIn();

    void setLoggedIn(boolean loggedIn);

    ChatColor getChatColor();

    void setChatColor(ChatColor color);

    String getLastLocale();

    void setLastLocale(Locale lastLocale);

    void setLastLocale(String lastLocale);

    String getStaffChannel();

    void setStaffChannel(String staffChannel);

    boolean isWatcher();

    void setWatcher(boolean watcher);

    default long getExp() {
        // TODO: use network levels
        return 0;
    }

    default void setExp(long xp) {
        // TODO: use network levels
    }

    Set<PlayerOptionType> getOptions();

    boolean getOption(PlayerOptionType option);

    void setOption(PlayerOptionType option, boolean status);

    String getIP();

    void setIP(String IP);

    Set<String> getIpHistory();

    Date getLastLogin();

    void setLastLogin(Date lastLogin);

    Date getRegistration();

    @Nullable
    String getDiscordId();

    void setDiscordId(@Nonnull String discordId);

    int getCensoringLevel();

    void incrCensoringLevel();

    int getSpammingLevel();

    void incrSpammingLevel();

    boolean isVanished();

    void setVanished(boolean vanished);

    GameMode getGameMode(GameType gameType);

    void setGameMode(GameMode gameMode, GameType gameType);

    GameType getLastGameType();

    void setLastGameType(GameType lastGameType);

    long getTotalPlayTime(GameType gameType);

    long getGlobalTotalPlayTime();

    long getLastPlayTime(GameType gameType);

    long getGlobalLastPlayTime();

    void setLastPlayTime(GameType gameType, long playTime);

    Collection<GameType> getPlayedGames();

    long getJoins(GameType gameType);

    void addPlayedGame(GameType gameType);

    MatrixPlayer save();

    void updateCached(String field);

    void saveToRedis();

    /**
     * Set the field with the given name to the given value without publishing an update to redis.
     *
     * @param field Field to update.
     * @param json  Json value to set to this field.
     */
    void setField(String field, String json);

    enum GameMode {
        SURVIVAL(0),
        CREATIVE(1),
        ADVENTURE(2),
        SPECTATOR(3);

        private final int id;

        GameMode(int id) {
            this.id = id;
        }

        public static GameMode getById(int id) {
            for (GameMode gameMode : values()) {
                if (gameMode.id == id) {
                    return gameMode;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
