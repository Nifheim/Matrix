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

    UUID getUniqueId();

    void setUniqueId(UUID uniqueId);

    String getName();

    void setName(String name);

    boolean isPremium();

    void setPremium(boolean premium);

    String getDisplayname();

    void setDisplayname(String displayname);

    String getIP();

    void setIP(String IP);

    Set<String> getIpHistory();

    ChatColor getChatColor();

    void setChatColor(ChatColor color);

    String getLastLocale();

    void setLastLocale(String locale);

    boolean isWatcher();

    void setWatcher(boolean watcher);

    Set<PlayerOptionType> getOptions();

    boolean getOption(PlayerOptionType option);

    void setOption(PlayerOptionType option, boolean status);

    default boolean hasPermission(String permission) {
        return false;
    }

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

    default Optional<Statistics> getStatistics(String server) {
        return getStatistics().stream().filter(statistics -> server.equals(statistics.getServer())).findFirst();
    }

    MatrixPlayer save();

    void updateCache();
}
