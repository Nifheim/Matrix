package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.IStatistics;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.database.MongoStorage;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

/**
 * @author Beelzebu
 */
@Getter
@NoArgsConstructor
@Entity(value = "players", noClassnameStored = true)
public abstract class MongoMatrixPlayer implements MatrixPlayer {

    private final Set<PlayerOptionType> options = new HashSet<>();
    @Property("iphistory")
    private final Set<String> ipHistory = new HashSet<>();
    @Reference(lazy = true, idOnly = true)
    private final Set<IStatistics> statistics = new HashSet<>();
    @Id
    private ObjectId id;
    @Property("uuid")
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    private String name;
    private String displayname;
    @Property("chatcolor")
    private ChatColor chatColor;
    private boolean watcher;
    private boolean authed;
    private double coins;
    private long exp;
    @Property("lastlogin")
    private Date lastLogin;
    private transient String IP;

    protected void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        updateCache();
    }

    protected void setName(String name) {
        this.name = name;
        updateCache();
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

    protected void setIP(String IP) {
        this.IP = IP;
        ipHistory.add(IP);
        updateCache();
    }

    @Override
    public void setStatistics(IStatistics statistics) {
        if (getStatistics(statistics.getServer()).isPresent()) {
            this.statistics.remove(getStatistics(statistics.getServer()).get());
            this.statistics.add(statistics);
        }
    }

    @Override
    public void save() {
        ((MongoStorage) Matrix.getAPI().getDatabase()).getUserDAO().save(this);
        updateCache();
    }

    @Override
    public void updateCache() {
        Matrix.getAPI().getRedis().setex("user:" + uniqueId, 1800, Matrix.getAPI().getGson().toJson(this));
    }
}
