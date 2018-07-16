package io.github.beelzebu.matrix.api.player;

import java.util.Date;
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

    ChatColor getChatColor();

    void setChatColor(ChatColor color);

    boolean isWatcher();

    void setWatcher(boolean watcher);

    Set<PlayerOptionType> getOptions();

    boolean getOption(PlayerOptionType option);

    void setOption(PlayerOptionType option, boolean status);

    boolean hasPermission(String permission);

    boolean isAuthed();

    void setAuthed(boolean authed);

    long getExp();

    void setExp(long xp);

    Date getLastLogin();

    void setLastLogin(Date lastLogin);

    void save();
}
