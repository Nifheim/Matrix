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

    default Optional<Statistics> getStatistics(String server) {
        return getStatistics().stream().filter(statistics -> server.equals(statistics.getServer())).findFirst();
    }

    MatrixPlayer save();

    void updateCache();
}
