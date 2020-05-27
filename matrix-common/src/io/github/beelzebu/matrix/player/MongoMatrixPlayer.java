package io.github.beelzebu.matrix.player;

import com.google.common.collect.ImmutableSet;
import io.github.beelzebu.matrix.MatrixAPIImpl;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.server.GameType;
import io.github.beelzebu.matrix.api.util.StringUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
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
@Entity(value = "players", noClassnameStored = true)
public class MongoMatrixPlayer implements MatrixPlayer {

    private static final transient Map<String, Field> FIELDS = new HashMap<>();
    @Id
    protected ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    protected UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    protected String name;
    @Indexed
    protected String lowercaseName;
    protected Set<String> knownNames = new HashSet<>();
    protected String displayName;
    protected boolean premium;
    protected boolean registered;
    protected boolean admin;
    protected String secret;
    protected String hashedPassword;
    protected boolean loggedIn;
    protected ChatColor chatColor = ChatColor.RESET;
    protected String lastLocale;
    protected String staffChannel;
    protected boolean watcher;
    protected long exp;
    protected Set<PlayerOptionType> options = new HashSet<>();
    @Indexed
    protected String IP;
    protected Set<String> ipHistory = new LinkedHashSet<>();
    protected Date lastLogin;
    protected Date registration;
    protected String discordId;
    protected int censoringLevel;
    protected int spammingLevel;
    protected boolean vanished;
    protected Map<GameType, GameMode> gameModeByGame = new HashMap<>();
    protected GameType lastGameType;
    protected Map<GameType, Long> totalPlayTimeByGame = new HashMap<>();
    protected Map<GameType, Long> playTimeByGame = new HashMap<>();
    protected Map<GameType, Long> playedGamesMap = new HashMap<>();

    static {
        Stream.of(MongoMatrixPlayer.class.getDeclaredFields()).filter(field -> !Modifier.isTransient(field.getModifiers())).peek(field -> field.setAccessible(true)).forEach(field -> FIELDS.put(field.getName(), field));
    }

    public MongoMatrixPlayer(UUID uniqueId, String name) {
        this();
        this.uniqueId = uniqueId;
        this.name = name;
        Matrix.getAPI().getCache().update(name, uniqueId);
        registration = new Date();
    }

    private MongoMatrixPlayer() {
        Optional<MatrixAPI> apiOptional = Matrix.getAPISafe();
        apiOptional.ifPresent(matrixAPI -> matrixAPI.getPlayers().add(this));
    }

    public static MongoMatrixPlayer fromHash(Map<String, String> hash) {
        MongoMatrixPlayer mongoMatrixPlayer = new MongoMatrixPlayer();
        for (Map.Entry<String, Field> ent : FIELDS.entrySet()) {
            String id = ent.getKey();
            Field field = ent.getValue();
            try {
                if (Objects.equals(id, "name") || Objects.equals(id, "uniqueId")) {
                    Objects.requireNonNull(hash.get(id), id + " can't be null");
                }
                if (hash.get(id) != null) {
                    Object value = Matrix.GSON.fromJson(hash.get(id), field.getGenericType());
                    if (value != null) {
                        field.set(mongoMatrixPlayer, value);
                    }
                } else {
                    field.set(id, null);
                }
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return mongoMatrixPlayer;
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
    public GameMode getGameMode(GameType gameType) {
        return gameModeByGame.getOrDefault(gameType, Matrix.getAPI().getServerInfo().getDefaultGameMode());
    }

    @Override
    public void setGameMode(GameMode gameMode, GameType gameType) {
        if (Objects.equals(gameModeByGame.get(gameType), gameMode)) {
            return;
        }
        gameModeByGame.put(gameType, gameMode);
        updateCached("gameModeByGame");
    }

    @Override
    public long getTotalPlayTime(GameType gameType) {
        return totalPlayTimeByGame.getOrDefault(gameType, 0L);
    }

    @Override
    public long getGlobalTotalPlayTime() {
        long total = 0;
        for (long i : totalPlayTimeByGame.values()) {
            total += i;
        }
        return total;
    }

    @Override
    public long getLastPlayTime(GameType gameType) {
        return playTimeByGame.getOrDefault(gameType, 0L);
    }

    @Override
    public long getGlobalLastPlayTime() {
        long total = 0;
        for (long i : playTimeByGame.values()) {
            total += i;
        }
        return total;
    }

    @Override
    public void setLastPlayTime(GameType gameType, long playTime) {
        if (playTime <= 0) {
            throw new IllegalArgumentException("playTime can't be equal or less than zero.");
        }
        if (getLastPlayTime(gameType) == playTime) {
            return;
        }
        playTimeByGame.put(gameType, playTime);
        totalPlayTimeByGame.put(gameType, getTotalPlayTime(gameType) + playTime);
        updateCached("playTimeByGame");
        updateCached("totalPlayTimeByGame");
    }

    @Override
    public Collection<GameType> getPlayedGames() {
        return ImmutableSet.copyOf(playedGamesMap.keySet());
    }

    @Override
    public long getJoins(GameType gameType) {
        return playedGamesMap.getOrDefault(gameType, 0L);
    }

    @Override
    public void addPlayedGame(GameType gameType) {
        playedGamesMap.put(gameType, playedGamesMap.getOrDefault(gameType, 0L) + 1);
        updateCached("playedGamesMap");
    }

    @Override
    public MatrixPlayer save() {
        Objects.requireNonNull(getName(), "Can't save a player with null name");
        Objects.requireNonNull(getUniqueId(), "Can't save a player with null uniqueId");
        if (getDisplayName() == null) {
            setDisplayName(getName());
        }
        MatrixAPIImpl api = (MatrixAPIImpl) Matrix.getAPI();
        api.getDatabase().getUserDAO().save((MongoMatrixPlayer) api.getCache().getPlayer(getUniqueId()).orElse(this));
        return this;
    }

    @Override
    public void updateCached(String field) {
        if (!Matrix.getAPI().getCache().isCached(getUniqueId())) {
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

    public ObjectId getId() {
        return id;
    }


    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(UUID uniqueId) {
        if (Objects.equals(this.uniqueId, uniqueId)) {
            return;
        }
        this.uniqueId = uniqueId;
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCached("uniqueId");
    }


    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
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

    public Set<String> getKnownNames() {
        return knownNames;
    }

    public boolean isPremium() {
        return premium;
    }

    @Override
    public void setPremium(boolean premium) {
        if (this.premium == premium) {
            return;
        }
        this.premium = premium;
        updateCached("premium");
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void setRegistered(boolean registered) {
        if (this.registered == registered) {
            return;
        }
        this.registered = registered;
        updateCached("registered");
    }

    public boolean isAdmin() {
        return admin;
    }

    @Override
    public void setAdmin(boolean admin) {
        if (this.admin == admin) {
            return;
        }
        this.admin = admin;
        updateCached("admin");
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public void setSecret(String secret) {
        if (Objects.equals(this.secret, secret)) {
            return;
        }
        this.secret = secret;
        updateCached("secret");
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    @Override
    public void setHashedPassword(String hashedPassword) {
        if (Objects.equals(this.hashedPassword, hashedPassword)) {
            return;
        }
        this.hashedPassword = hashedPassword;
        updateCached("hashedPassword");
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        if (this.loggedIn == loggedIn) {
            return;
        }
        this.loggedIn = loggedIn;
        updateCached("loggedIn");
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    @Override
    public void setChatColor(ChatColor chatColor) {
        if (Objects.equals(this.chatColor, chatColor)) {
            return;
        }
        this.chatColor = chatColor;
        updateCached("chatColor");
    }

    public String getLastLocale() {
        return lastLocale;
    }

    @Override
    public void setLastLocale(Locale lastLocale) {
        if (Objects.isNull(lastLocale)) {
            return;
        }
        setLastLocale(lastLocale.getISO3Language());
    }

    @Override
    public void setLastLocale(String lastLocale) {
        if (Objects.equals(this.lastLocale, lastLocale)) {
            return;
        }
        this.lastLocale = lastLocale;
        updateCached("lastLocale");
    }

    public String getStaffChannel() {
        return staffChannel;
    }

    @Override
    public void setStaffChannel(String staffChannel) {
        if (Objects.equals(this.staffChannel, staffChannel)) {
            return;
        }
        this.staffChannel = staffChannel;
        updateCached("staffChannel");
    }

    public boolean isWatcher() {
        return watcher;
    }

    @Override
    public void setWatcher(boolean watcher) {
        if (this.watcher == watcher) {
            return;
        }
        this.watcher = watcher;
        updateCached("watcher");
    }

    public long getExp() {
        return exp;
    }

    @Override
    public void setExp(long exp) {
        if (this.exp == exp) {
            return;
        }
        this.exp = exp;
        updateCached("exp");
    }

    public Set<PlayerOptionType> getOptions() {
        return options;
    }

    public String getIP() {
        return IP;
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

    public Set<String> getIpHistory() {
        return ipHistory;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    @Override
    public void setLastLogin(Date lastLogin) {
        if (Objects.equals(this.lastLogin, lastLogin)) {
            return;
        }
        this.lastLogin = lastLogin;
        updateCached("lastLogin");
    }

    public Date getRegistration() {
        return registration;
    }

    public void setRegistration(Date registration) {
        if (Objects.equals(this.registration, registration)) {
            return;
        }
        this.registration = registration;
        updateCached("registration");
    }

    public String getDiscordId() {
        return discordId;
    }

    @Override
    public void setDiscordId(@Nonnull String discordId) {
        if (Objects.equals(this.discordId, discordId)) {
            return;
        }
        this.discordId = discordId;
        updateCached("discordId");
    }

    public int getCensoringLevel() {
        return censoringLevel;
    }

    public int getSpammingLevel() {
        return spammingLevel;
    }

    public boolean isVanished() {
        return vanished;
    }

    @Override
    public void setVanished(boolean vanished) {
        this.vanished = vanished;
        updateCached("vanished");
    }

    public Map<GameType, GameMode> getGameModeByGame() {
        return gameModeByGame;
    }

    public GameType getLastGameType() {
        return lastGameType;
    }

    @Override
    public void setLastGameType(GameType lastGameType) {
        if (Objects.equals(this.lastGameType, lastGameType)) {
            return;
        }
        this.lastGameType = lastGameType;
        updateCached("lastGameType");
    }

    public Map<GameType, Long> getTotalPlayTimeByGame() {
        return totalPlayTimeByGame;
    }

    public Map<GameType, Long> getPlayTimeByGame() {
        return playTimeByGame;
    }

    public Map<GameType, Long> getPlayedGamesMap() {
        return playedGamesMap;
    }
}
