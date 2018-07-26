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

    String getName();

    String getDisplayname();

    void setDisplayname(String displayname);

    String getIP();

    Set<String> getIpHistory();

    ChatColor getChatColor();

    void setChatColor(ChatColor color);

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

    Set<IStatistics> getStatistics();

    void setStatistics(IStatistics statistics);

    default Optional<IStatistics> getStatistics(String server) {
        return getStatistics().stream().filter(iStatistics -> server.equals(iStatistics.getServer())).findFirst();
    }

    void save();

    void updateCache();
}
