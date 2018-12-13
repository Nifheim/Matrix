package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
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
import org.mongodb.morphia.annotations.Property;

/**
 * @author Beelzebu
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(value = "players", noClassnameStored = true)
public class MongoMatrixPlayer implements MatrixPlayer {

    @Id
    protected ObjectId id;
    @Property("uuid")
    @Indexed(options = @IndexOptions(unique = true))
    protected UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    protected String name;
    protected boolean premium;
    protected String displayname;
    @Property("chatcolor")
    protected ChatColor chatColor = ChatColor.RESET;
    protected String lastLocale;
    protected boolean watcher;
    protected boolean authed;
    protected double coins;
    protected long exp;
    @Property("lastlogin")
    protected Date lastLogin;
    protected Set<PlayerOptionType> options = new HashSet<>();
    @Property("iphistory")
    protected Set<String> ipHistory = new LinkedHashSet<>();
    protected transient String IP;
    protected transient Set<Statistics> statistics = new HashSet<>();

    @Override
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        updateCache();
    }

    @Override
    public void setName(String name) {
        this.name = name;
        updateCache();
    }

    @Override
    public void setPremium(boolean premium) {
        this.premium = premium;
        updateCache();
    }

    @Override
    public String getDisplayname() {
        return displayname != null ? displayname : getName();
    }

    @Override
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
        updateCache();
    }

    @Override
    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
        updateCache();
    }

    @Override
    public void setLastLocale(String locale) {
        lastLocale = locale;
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
        ((MongoStorage) Matrix.getAPI().getDatabase()).getUserDAO().save(this);
        updateCache();
        return this;
    }

    @Override
    public void updateCache() {
        Matrix.getAPI().getRedis().setex("user:" + uniqueId, 1800, toJson());
    }

    private String toJson() {
        return Matrix.getAPI().getGson().toJson(this, MongoMatrixPlayer.class);
    }
}
