package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.messaging.message.AuthMessage;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.player.Statistics;
import io.github.beelzebu.matrix.database.MongoStorage;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * @author Beelzebu
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(value = "players", noClassnameStored = true)
public final class MongoMatrixPlayer implements MatrixPlayer {

    private static final transient Map<String, Field> FIELDS = new HashMap<>();
    @Id
    protected ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    protected UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    protected String name;
    protected Set<String> knownNames;
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

    public static MongoMatrixPlayer fromHash(Map<String, String> hash) {
        MongoMatrixPlayer mongoMatrixPlayer = new MongoMatrixPlayer();
        loadFields();
        FIELDS.forEach((id, field) -> {
            try {
                Object value = Matrix.getAPI().getGson().fromJson(hash.get(id), field.getType());
                if (value != null) {
                    field.set(mongoMatrixPlayer, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return mongoMatrixPlayer;
    }

    private static void loadFields() {
        if (FIELDS.isEmpty()) {
            Stream.of(MongoMatrixPlayer.class.getDeclaredFields()).filter(field -> !Modifier.isTransient(field.getModifiers())).forEach(field -> FIELDS.put(field.getName(), field));
            FIELDS.values().forEach(field -> field.setAccessible(true));
        }
    }

    @Override
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCached("uniqueId");
    }

    @Override
    public void setName(String name) {
        this.name = name;
        knownNames.add(name);
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCached("name");
        updateCached("knownNames");
    }

    @Override
    public void setPremium(boolean premium) {
        this.premium = premium;
        setUniqueId(Matrix.getAPI().getPlugin().getUniqueId(name));
        updateCached("premium");
    }

    @Override
    public void setAdmin(boolean admin) {
        this.admin = admin;
        updateCached("admin");
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
        updateCached("secret");
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateCached("displayName");
    }

    @Override
    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
        updateCached("chatColor");
    }

    @Override
    public void setLastLocale(String lastLocale) {
        this.lastLocale = lastLocale;
        updateCached("lastLocale");
    }

    @Override
    public void setStaffChannel(String staffChannel) {
        this.staffChannel = staffChannel;
        updateCached("staffChannel");
    }

    @Override
    public void setWatcher(boolean watcher) {
        this.watcher = watcher;
        updateCached("watcher");
    }

    @Override
    public void setAuthed(boolean authed) {
        this.authed = authed;
        AuthMessage authMessage = new AuthMessage(getUniqueId(), authed);
        Matrix.getAPI().getRedis().sendMessage(authMessage.getChannel(), Matrix.getAPI().getGson().toJson(authMessage));
        updateCached("authed");
    }

    @Override
    public void setCoins(double coins) {
        this.coins = coins;
        updateCached("coins");
    }

    @Override
    public void setExp(long exp) {
        this.exp = exp;
        updateCached("exp");
    }

    @Override
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
        updateCached("lastLogin");
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        return options.contains(option);
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        if (status ? options.add(option) : options.remove(option)) {
            updateCached("options");
        }
    }

    @Override
    public void setIP(String IP) {
        this.IP = IP;
        ipHistory.add(IP);
        updateCached("IP");
        updateCached("ipHistory");
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
        setLastLogin(new Date());
        if (getDisplayName() == null) {
            setDisplayName(getName());
        }
        saveToRedis();
        return this;
    }

    @Override
    public void updateCached(String field) {
        loadFields();
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            Object object = FIELDS.get(field).get(this);
            if (object != null) {
                jedis.hset(getKey(), field, Matrix.getAPI().getGson().toJson(FIELDS.get(field).get(this)));
            } else {
                jedis.hdel(getKey(), field);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void saveToRedis() {
        loadFields();
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
            FIELDS.forEach((id, field) -> {
                try {
                    if (field.get(this) != null) {
                        return;
                    }
                    pipeline.hset(getKey(), id, Matrix.getAPI().getGson().toJson(field.get(this)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            pipeline.sync();
        }
    }
}
