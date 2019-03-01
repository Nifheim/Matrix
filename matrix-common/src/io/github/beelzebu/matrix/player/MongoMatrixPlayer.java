package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.player.Statistics;
import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.database.MongoStorage;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(value = "players", noClassnameStored = true)
public class MongoMatrixPlayer implements MatrixPlayer {

    private static final transient Map<String, Field> FIELDS = new HashMap<>();
    @Id
    protected ObjectId id;
    @NonNull
    @Indexed(options = @IndexOptions(unique = true))
    protected UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    @NonNull
    protected String name;
    protected String lowercaseName;
    protected Set<String> knownNames = new HashSet<>();
    protected String displayName;
    protected boolean premium;
    protected boolean registered;
    protected boolean admin;
    protected String secret;
    protected ChatColor chatColor = ChatColor.RESET;
    protected String lastLocale;
    protected String staffChannel;
    protected boolean watcher;
    protected boolean authed;
    protected long exp;
    protected Set<PlayerOptionType> options = new HashSet<>();
    protected String IP;
    protected Set<String> ipHistory = new LinkedHashSet<>();
    protected Date lastLogin;
    protected Date registration;
    protected String discordId;
    protected int censoringLevel;
    protected int spammingLevel;
    protected boolean vanished;
    protected transient Set<Statistics> statistics = new HashSet<>();

    public MongoMatrixPlayer(@NonNull UUID uniqueId, @NonNull String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        Matrix.getAPI().getCache().update(name, uniqueId);
        registration = new Date();
    }

    public static MongoMatrixPlayer fromHash(Map<String, String> hash) {
        MongoMatrixPlayer mongoMatrixPlayer = new MongoMatrixPlayer();
        FIELDS.forEach((id, field) -> {
            try {
                if (Objects.equals(id, "name") && hash.get(id) == null || Objects.equals(id, "uniqueId") && hash.get(id) == null) {
                    throw new NullPointerException(id + " can't be null");
                }
                if (hash.get(id) != null) {
                    Object value = Matrix.GSON.fromJson(hash.get(id), field.getType());
                    if (value != null) {
                        field.set(mongoMatrixPlayer, value);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return mongoMatrixPlayer;
    }

    public static void loadFields() {
        if (FIELDS.isEmpty()) {
            Stream.of(MongoMatrixPlayer.class.getDeclaredFields()).filter(field -> !Modifier.isTransient(field.getModifiers())).forEach(field -> FIELDS.put(field.getName(), field));
            FIELDS.values().forEach(field -> field.setAccessible(true));
        }
    }

    @Override
    public void setUniqueId(@NonNull UUID uniqueId) {
        if (Objects.equals(this.uniqueId, uniqueId)) {
            return;
        }
        this.uniqueId = uniqueId;
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCached("uniqueId");
    }

    @Override
    public void setName(@NonNull String name) {
        if (Objects.equals(this.name, name)) {
            return;
        }
        this.name = name;
        lowercaseName = name;
        knownNames.add(name);
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCached("name");
        updateCached("lowercaseName");
        updateCached("knownNames");
    }

    @Override
    public void execute(String command) {
        Matrix.getAPI().getPlugin().dispatchCommand(this, command);
    }

    @Override
    public void sendMessage(String message) {
        Matrix.getAPI().getPlugin().sendMessage(getName(), StringUtils.replace(message));
    }

    @Override
    public String getLowercaseName() {
        if (!Objects.equals(lowercaseName, getName().toLowerCase())) {
            lowercaseName = getName().toLowerCase();
        }
        return lowercaseName;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    @Override
    public void setDisplayName(String displayName) {
        if (Objects.equals(this.displayName, displayName)) {
            return;
        }
        if (Objects.isNull(displayName)) {
            displayName = getName();
        }
        this.displayName = displayName;
        updateCached("displayName");
    }

    @Override
    public void setPremium(boolean premium) {
        if (this.premium == premium) {
            return;
        }
        this.premium = premium;
        updateCached("premium");
    }

    @Override
    public void setAdmin(boolean admin) {
        if (this.admin == admin) {
            return;
        }
        this.admin = admin;
        updateCached("admin");
    }

    @Override
    public void setSecret(String secret) {
        if (Objects.equals(this.secret, secret)) {
            return;
        }
        this.secret = secret;
        updateCached("secret");
    }

    @Override
    public void setIP(String IP) {
        if (Objects.equals(this.IP, IP)) {
            return;
        }
        this.IP = IP;
        ipHistory.add(IP);
        updateCached("IP");
        updateCached("ipHistory");
    }

    @Override
    public void setChatColor(ChatColor chatColor) {
        if (Objects.equals(this.chatColor, chatColor)) {
            return;
        }
        this.chatColor = chatColor;
        updateCached("chatColor");
    }

    @Override
    public void setLastLocale(String lastLocale) {
        if (Objects.equals(this.lastLocale, lastLocale)) {
            return;
        }
        this.lastLocale = lastLocale;
        updateCached("lastLocale");
    }

    @Override
    public void setLastLocale(Locale lastLocale) {
        if (Objects.isNull(lastLocale)) {
            return;
        }
        setLastLocale(lastLocale.getISO3Language());
    }

    @Override
    public void setStaffChannel(String staffChannel) {
        if (Objects.equals(this.staffChannel, staffChannel)) {
            return;
        }
        this.staffChannel = staffChannel;
        updateCached("staffChannel");
    }

    @Override
    public void setWatcher(boolean watcher) {
        if (this.watcher == watcher) {
            return;
        }
        this.watcher = watcher;
        updateCached("watcher");
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        return options.contains(option);
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        if (status && options.contains(option)) {
            return;
        } else if (!status && !options.contains(option)) {
            return;
        }
        if (status ? options.add(option) : options.remove(option)) {
            updateCached("options");
        }
    }

    @Override
    public void setAuthed(boolean authed) {
        if (this.authed == authed) {
            return;
        }
        this.authed = authed;
        updateCached("authed");
    }

    @Override
    public void setExp(long exp) {
        if (this.exp == exp) {
            return;
        }
        this.exp = exp;
        updateCached("exp");
    }

    @Override
    public void setLastLogin(Date lastLogin) {
        if (Objects.equals(this.lastLogin, lastLogin)) {
            return;
        }
        this.lastLogin = lastLogin;
        updateCached("lastLogin");
    }

    @Override
    public void setStatistics(Statistics statistics) {
        if (getStatistics(statistics.getServer()).isPresent()) {
            this.statistics.remove(getStatistics(statistics.getServer()).get());
            this.statistics.add(statistics);
        }
    }

    @Override
    public void setDiscordId(@Nonnull String discordId) {
        if (Objects.equals(this.discordId, discordId)) {
            return;
        }
        this.discordId = discordId;
        updateCached("discordId");
    }

    @Override
    public void incrCensoringLevel() {
        censoringLevel++;
        updateCached("censoringLevel");
    }

    @Override
    public void incrSpammingLevel() {
        spammingLevel++;
        updateCached("spammingLevel");
    }

    @Override
    public void setVanished(boolean vanished) {
        this.vanished = vanished;
        updateCached("vanished");
    }

    @Override
    public MatrixPlayer save() {
        Objects.requireNonNull(getName(), "Can't save a player with null name");
        Objects.requireNonNull(getUniqueId(), "Can't save a player with null uniqueId");
        if (getDisplayName() == null) {
            setDisplayName(getName());
        }
        ((MongoStorage) Matrix.getAPI().getDatabase()).getUserDAO().save((MongoMatrixPlayer) Matrix.getAPI().getCache().getPlayer(getUniqueId()).orElse(this));
        return this;
    }

    @Override
    public void updateCached(String field) {
        if (!Matrix.getAPI().getCache().getPlayer(getUniqueId()).isPresent()) {
            return;
        }
        if (Objects.equals(field, "name") && getName() == null) {
            Matrix.getLogger().debug("Trying to save a null name for " + getUniqueId());
            return;
        }
        if (Objects.equals(field, "uniqueId") && getUniqueId() == null) {
            Matrix.getLogger().debug("Trying to save a null uuid for " + getName());
            return;
        }
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            Object value = FIELDS.get(field).get(this);
            String jsonValue = Matrix.GSON.toJson(value);
            Matrix.getLogger().debug("Updating " + getName() + " field `" + field + "' with value `" + jsonValue + "'");
            if (value != null) {
                jedis.hset(getRedisKey(), field, jsonValue);
            } else {
                jedis.hdel(getRedisKey(), field);
            }
            new FieldUpdate(getUniqueId(), field, jsonValue).send();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveToRedis() {
        Objects.requireNonNull(uniqueId, "UUID can't be null");
        Objects.requireNonNull(name, "name can't be null");
        if (Objects.isNull(lowercaseName)) {
            lowercaseName = getName().toLowerCase();
        }
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
            FIELDS.forEach((id, field) -> {
                try {
                    if (field.get(this) != null) {
                        pipeline.hset(getRedisKey(), id, Matrix.GSON.toJson(field.get(this)));
                    } else {
                        pipeline.hdel(getRedisKey(), id);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            pipeline.sync();
        }
    }

    @Override
    public void setField(String field, String json) {
        try {
            Field f = FIELDS.get(field);
            if (f != null) {
                f.set(this, Matrix.GSON.fromJson(json, f.getType()));
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public void setRegistration(Date registration) {
        if (Objects.equals(this.registration, registration)) {
            return;
        }
        this.registration = registration;
        updateCached("registration");
    }
}
