package com.github.beelzebu.matrix.player;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.player.GameMode;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.player.PlayerOptionChangeEvent;
import cl.indiopikaro.jmatrix.api.player.PlayerOptionType;
import cl.indiopikaro.jmatrix.api.player.Statistic;
import cl.indiopikaro.jmatrix.api.server.GameType;
import cl.indiopikaro.jmatrix.api.util.StringUtils;
import com.github.beelzebu.matrix.MatrixAPIImpl;
import com.github.beelzebu.matrix.cache.CacheProviderImpl;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Beelzebu
 */
@SuppressWarnings("FieldMayBeFinal")
@Entity(value = "players", noClassnameStored = true)
public final class MongoMatrixPlayer implements MatrixPlayer {

    public static final transient Map<String, Field> FIELDS = new HashMap<>();
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    private String name;
    @Indexed(options = @IndexOptions(unique = true))
    private String lowercaseName;
    private Set<String> knownNames = new HashSet<>();
    private String displayName;
    private boolean premium;
    private boolean registered;
    private boolean admin;
    private String secret;
    private String hashedPassword;
    private boolean loggedIn;
    private ChatColor chatColor = ChatColor.RESET;
    private String lastLocale;
    private String staffChannel;
    private boolean watcher;
    private HashSet<PlayerOptionType> options = new HashSet<>();
    @Indexed
    private String IP;
    private Set<String> ipHistory = new LinkedHashSet<>();
    private Date lastLogin;
    private Date registration;
    private String discordId;
    private int censoringLevel;
    private int spammingLevel;
    private boolean vanished;
    private GameType lastGameType;
    private String lastServerGroup;
    private String lastServerName;
    private HashMap<GameType, GameMode> gameModeByGame = new HashMap<>();

    static {
        Stream.of(MongoMatrixPlayer.class.getDeclaredFields()).filter(field -> !Modifier.isTransient(field.getModifiers())).peek(field -> field.setAccessible(true)).forEach(field -> FIELDS.put(field.getName(), field));
    }

    public MongoMatrixPlayer(UUID uniqueId, String name) {
        this();
        this.uniqueId = uniqueId;
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        Matrix.getAPI().getCache().update(name, uniqueId);
        registration = new Date();
    }

    private MongoMatrixPlayer() {
    }

    public String getId() {
        if (id == null) {
            return null;
        }
        return id.toHexString();
    }

    public static MongoMatrixPlayer fromHash(Map<String, String> hash) {
        MongoMatrixPlayer mongoMatrixPlayer = new MongoMatrixPlayer();
        for (Map.Entry<String, Field> ent : FIELDS.entrySet()) {
            String id = ent.getKey(); // field id
            Field field = ent.getValue(); // field object
            try {
                if (Objects.equals(id, "name") || Objects.equals(id, "uniqueId")) {
                    Objects.requireNonNull(hash.get(id), id + " can't be null");
                }
                if (hash.containsKey(id)) { // hash contains field
                    mongoMatrixPlayer.setField(field, hash.get(id));
                }
            } catch (IllegalArgumentException | NullPointerException | JsonSyntaxException e) {
                Matrix.getLogger().debug(e);
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
            PlayerOptionChangeEvent.LISTENERS.forEach(playerOptionChangeListener -> playerOptionChangeListener.onPlayerOptionChange(new PlayerOptionChangeEvent(this, option, !status, status)));
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
        if (gameType == GameType.NONE) {
            return;
        }
        if (Objects.equals(gameModeByGame.get(gameType), gameMode)) {
            return;
        }
        gameModeByGame.put(gameType, gameMode);
        updateCached("gameModeByGame");
    }

    @Override
    public long getTotalPlayTime(GameType gameType) {
        return 0;//totalPlayTimeByGame.getOrDefault(gameType, 0L);
    }

    @Override
    public long getGlobalTotalPlayTime() {
        long total = 0;
        /*
        for (long i : totalPlayTimeByGame.values()) {
            total += i;
        }
         */
        return total;
    }

    @Override
    public long getLastPlayTime(GameType gameType) {
        return 0;//playTimeByGame.getOrDefault(gameType, 0L);
    }

    @Override
    public long getGlobalLastPlayTime() {
        long total = 0;
        /*
        for (long i : playTimeByGame.values()) {
            total += i;
        }
        */
        return total;
    }

    @Override
    public void setLastPlayTime(GameType gameType, long playTime) {
        /*
        if (gameType == GameType.NONE) {
            return;
        }
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
         */
    }

    @Override
    public Collection<GameType> getPlayedGames() {
        return ImmutableSet.of();//ImmutableSet.copyOf(playedGamesMap.keySet());
    }

    @Override
    public long getJoins(GameType gameType) {
        return 0;//playedGamesMap.getOrDefault(gameType, 0L);
    }

    @Override
    public void addPlayedGame(GameType gameType) {
        /*
        if (gameType == GameType.NONE) {
            return;
        }
        playedGamesMap.put(gameType, playedGamesMap.getOrDefault(gameType, 0L) + 1);
        updateCached("playedGamesMap");
         */
    }

    @Override
    public MatrixPlayer save() {
        Objects.requireNonNull(getName(), "Can't save a player with null name");
        Objects.requireNonNull(getUniqueId(), "Can't save a player with null uniqueId");
        if (getDisplayName() == null) {
            setDisplayName(getName());
        }
        if (lowercaseName == null) {
            setName(name);
        }
        MatrixAPIImpl api = (MatrixAPIImpl) Matrix.getAPI();
        api.getDatabase().getUserDAO().save((MongoMatrixPlayer) api.getCache().getPlayer(getUniqueId()).orElse(this));
        return this;
    }

    public void updateCached(String field) {
        try {
            Matrix.getAPI().getCache().updateCachedField(this, field, MongoMatrixPlayer.FIELDS.get(field).get(this));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setField(String fieldName, String json) {
        Field field = FIELDS.get(fieldName);
        if (field == null) {
            Matrix.getLogger().info("Trying to update null field for " + getName() + " field: '" + fieldName + "' value: '" + json + "'");
            return;
        }
        setField(field, json);
    }

    @SuppressWarnings("unchecked")
    private void setField(Field field, String json) {
        try {
            if (field.getName().equals("gameModeByGame")) {
                HashMap<GameType, GameMode> map = (HashMap<GameType, GameMode>) field.get(this);
                map.remove(GameType.NONE);
                HashMap<GameType, GameMode> jsonMap = Matrix.GSON.fromJson(json, new TypeToken<HashMap<GameType, GameMode>>() {
                }.getType());
                if (!jsonMap.isEmpty()) {
                    Matrix.getLogger().info("Updating json map for " + getName() + " json: '" + json + "'");
                    for (Map.Entry<GameType, GameMode> ent : jsonMap.entrySet()) {
                        map.put(ent.getKey(), ent.getValue());
                    }
                }
                map.remove(GameType.NONE);
            } else if (field.getName().equals("options")) {
                Object value = Matrix.GSON.fromJson(json, new TypeToken<HashSet<PlayerOptionType>>() {
                }.getType());
                if (value != null) {
                    field.set(this, value);
                }
            } else {
                try {
                    Object value = Matrix.GSON.fromJson(json, field.getGenericType());
                    if (value != null) {
                        field.set(this, value);
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    Matrix.getLogger().warn("Field: " + field.getName() + " Json: " + json);
                    e.printStackTrace();
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveStats(Map<Statistic, Long> stats) {
        Matrix.getAPI().getSQLDatabase().incrStats(this, /* using group name here because actual server name may be just an arena server*/Matrix.getAPI().getServerInfo().getGroupName(), stats);
    }

    @Override
    public void saveStat(Statistic stat, long value) {
        Matrix.getAPI().getSQLDatabase().incrStat(this, Matrix.getAPI().getServerInfo().getGroupName(), stat, value);
    }

    @Override
    public CompletableFuture<Long> getStat(Statistic statistic) {
        return getStat(Matrix.getAPI().getServerInfo().getGroupName(), statistic);
    }

    @Override
    public CompletableFuture<Long> getStat(String serverGroup, Statistic statistic) {
        return Matrix.getAPI().getSQLDatabase().getStat(this, serverGroup, statistic);
    }

    @Override
    public String getRedisKey() {
        return CacheProviderImpl.USER_KEY_PREFIX + getUniqueId();
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(UUID uniqueId) {
        if (Objects.equals(this.uniqueId, uniqueId)) {
            return;
        }
        Matrix.getAPI().getCache().update(name, uniqueId);
        this.uniqueId = uniqueId;
        updateCached("uniqueId");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (Objects.equals(this.name, name)) {
            return;
        }
        this.name = Objects.requireNonNull(name, "name can't be null.");
        lowercaseName = name.toLowerCase();
        knownNames.add(name);
        Matrix.getAPI().getCache().update(name, uniqueId);
        updateCached("name");
        updateCached("lowercaseName");
        updateCached("knownNames");
    }


    public Set<String> getKnownNames() {
        return knownNames;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public Set<PlayerOptionType> getOptions() {
        return options;
    }

    @Override
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

    @Override
    public Set<String> getIpHistory() {
        return ipHistory;
    }

    @Override
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

    @Override
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

    @Override
    public String getDiscordId() {
        return discordId;
    }

    @Override
    public void setDiscordId(String discordId) {
        if (Objects.equals(this.discordId, discordId)) {
            return;
        }
        this.discordId = discordId;
        updateCached("discordId");
    }

    @Override
    public int getCensoringLevel() {
        return censoringLevel;
    }

    @Override
    public int getSpammingLevel() {
        return spammingLevel;
    }

    @Override
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

    @Override
    public GameType getLastGameType() {
        return lastGameType;
    }

    @Override
    public String getLastServerGroup() {
        return lastServerGroup;
    }

    @Override
    public void setLastServerGroup(String serverGroup) {
        if (Objects.equals(this.lastServerGroup, serverGroup)) {
            return;
        }
        this.lastServerGroup = serverGroup;
        updateCached("lastServerGroup");
    }

    @Override
    public String getLastServerName() {
        return lastServerName;
    }

    @Override
    public void setLastServerName(String serverName) {
        if (Objects.equals(this.lastServerName, serverName)) {
            return;
        }
        this.lastServerName = serverName;
        updateCached("lastServerName");
    }

    @Override
    public void setLastGameType(GameType lastGameType) {
        if (Objects.equals(this.lastGameType, lastGameType)) {
            return;
        }
        this.lastGameType = lastGameType;
        updateCached("lastGameType");
    }
}
