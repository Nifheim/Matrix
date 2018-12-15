package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.messaging.message.AuthMessage;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.player.Statistics;
import io.github.beelzebu.matrix.database.MongoStorage;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Beelzebu
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(value = "players", noClassnameStored = true)
public final class MongoMatrixPlayer implements MatrixPlayer {

    @Id
    protected ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    protected UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    protected String name;
    protected String displayName;
    protected boolean premium;
    protected boolean admin;
    protected String secret;
    protected ChatColor chatColor = ChatColor.RESET;
    protected String lastLocale;
    protected String staffChannel;
    protected boolean watcher;
    protected boolean authed;
    protected double coins;
    protected long exp;
    protected Date lastLogin;
    protected Set<PlayerOptionType> options = new HashSet<>();
    protected Set<String> ipHistory = new LinkedHashSet<>();
    protected transient String IP;
    protected transient Set<Statistics> statistics = new HashSet<>();

    public MongoMatrixPlayer(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        Matrix.getAPI().getCache().update(name, uniqueId);
    }

    @Override
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCache();
    }

    @Override
    public void setName(String name) {
        this.name = name;
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCache();
    }

    @Override
    public void setPremium(boolean premium) {
        this.premium = premium;
        setUniqueId(Matrix.getAPI().getPlugin().getUniqueId(name));
        updateCache();
    }

    @Override
    public void setAdmin(boolean admin) {
        this.admin = admin;
        updateCache();
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
        updateCache();
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateCache();
    }

    @Override
    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
        updateCache();
    }

    @Override
    public void setLastLocale(String lastLocale) {
        this.lastLocale = lastLocale;
        updateCache();
    }

    @Override
    public void setStaffChannel(String staffChannel) {
        this.staffChannel = staffChannel;
        updateCache();
    }

    @Override
    public void setWatcher(boolean watcher) {
        this.watcher = watcher;
        updateCache();
    }


    @Override
    public void setAuthed(boolean authed) {
        this.authed = authed;
        AuthMessage authMessage = new AuthMessage(getUniqueId(), authed);
        Matrix.getAPI().getRedis().sendMessage(authMessage.getChannel(), Matrix.getAPI().getGson().toJson(authMessage));
        updateCache();
    }

    @Override
    public void setCoins(double coins) {
        this.coins = coins;
        updateCache();
    }

    @Override
    public void setExp(long exp) {
        this.exp = exp;
        updateCache();
    }

    @Override
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
        updateCache();
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        return options.contains(option);
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        boolean update;
        if (status) {
            update = options.add(option);
        } else {
            update = options.remove(option);
        }
        if (update) {
            updateCache();
        }
    }

    @Override
    public void setIP(String IP) {
        this.IP = IP;
        ipHistory.add(IP);
        updateCache();
    }

    @Override
    public void setStatistics(Statistics statistics) {
        if (getStatistics(statistics.getServer()).isPresent()) {
            this.statistics.remove(getStatistics(statistics.getServer()).get());
            this.statistics.add(statistics);
        }
    }

    @Override
    public MatrixPlayer save() {
        ((MongoStorage) Matrix.getAPI().getDatabase()).getUserDAO().save((MongoMatrixPlayer) Matrix.getAPI().getCache().getPlayer(uniqueId).orElse(this));
        setLastLogin(new Date());
        if (getDisplayName() == null) {
            setDisplayName(getName());
        }
        updateCache();
        return this;
    }

    @Override
    public void updateCache() {
        Matrix.getAPI().getRedis().set("user:" + uniqueId, toJson());
    }

    private String toJson() {
        return Matrix.getAPI().getGson().toJson(this, MongoMatrixPlayer.class);
    }
}
