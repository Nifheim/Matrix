package com.github.beelzebu.matrix.player;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionChangeEvent;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Transient;
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
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
@SuppressWarnings("FieldMayBeFinal")
@Entity(value = "players", noClassnameStored = true)
public final class MongoMatrixPlayer implements MatrixPlayer {

    public static final transient Map<String, Field> FIELDS = new HashMap<>();

    static {
        Stream.of(MongoMatrixPlayer.class.getDeclaredFields()).filter(field -> !Modifier.isTransient(field.getModifiers())).peek(field -> field.setAccessible(true)).forEach(field -> FIELDS.put(field.getName(), field));
    }

    @Id
    private ObjectId id;
    @Transient
    private transient @Nullable String idString = null;
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uniqueId;
    @Indexed(options = @IndexOptions(unique = true))
    private String name;
    @Indexed(options = @IndexOptions(unique = true))
    private String lowercaseName;
    private @NotNull Set<String> knownNames = new HashSet<>();
    private String displayName;
    private boolean premium;
    private boolean bedrock;
    private boolean registered;
    private boolean admin;
    private String hashedPassword;
    private boolean loggedIn;
    private String lastLocale;
    private String staffChannel;
    private boolean watcher;
    private @NotNull HashSet<PlayerOptionType> options = new HashSet<>();
    @Indexed
    private String IP;
    private @NotNull Set<String> ipHistory = new LinkedHashSet<>();
    private Date lastLogin;
    private Date registration;
    private String discordId;
    private int censoringLevel;
    private int spammingLevel;
    private boolean vanished;
    private @NotNull HashMap<String, GameMode> gameModeByGame = new HashMap<>();

    public MongoMatrixPlayer(UUID uniqueId, @NotNull String name) {
        this();
        this.uniqueId = uniqueId;
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        registration = new Date();
    }

    private MongoMatrixPlayer() {
    }

    public static @Nullable MongoMatrixPlayer fromHash(@NotNull Map<String, String> hash) {
        MongoMatrixPlayer mongoMatrixPlayer = new MongoMatrixPlayer();
        for (Map.Entry<String, Field> ent : FIELDS.entrySet()) {
            String id = ent.getKey(); // field id
            Field field = ent.getValue(); // field object
            try {
                if (hash.containsKey(id)) { // hash contains field
                    mongoMatrixPlayer.setField(field, hash.get(id));
                }
            } catch (@NotNull IllegalArgumentException | NullPointerException | JsonSyntaxException e) {
                Matrix.getLogger().debug(e);
                e.printStackTrace();
                return null;
            }
        }
        return mongoMatrixPlayer;
    }

    @Override
    public @NotNull String getId() {
        if (id != null && idString == null) {
            return idString = id.toHexString();
        }
        return Objects.requireNonNull(idString);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(@NotNull UUID uniqueId) {
        if (Objects.equals(this.uniqueId, uniqueId)) {
            return;
        }
        if (premium && uniqueId.version() != 4) {
            throw new IllegalArgumentException("Only random uuids are allowed for premium players");
        } else if (!premium && uniqueId.version() != 3) {
            throw new IllegalArgumentException("Can not use a random generated UUID for a cracked player");
        }
        updateCached("uniqueId", uniqueId).thenAccept(val -> {
            this.uniqueId = val;
            save().join();
        });
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        if (Objects.equals(this.name, name)) {
            return;
        }
        if (!premium) {
            throw new RuntimeException("Can't update name for cracked player");
        }
        this.name = Objects.requireNonNull(name, "name can't be null.");
        lowercaseName = name.toLowerCase();
        knownNames.add(name);
        updateCached("name");
        updateCached("lowercaseName");
        updateCached("knownNames");
    }

    @Override
    public void execute(String command) {
        Matrix.getAPI().getPlugin().dispatchCommand(this, command);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        Matrix.getAPI().getPlugin().sendMessage(getName(), StringUtils.replace(message));
    }

    @Override
    public @NotNull String getLowercaseName() {
        if (!Objects.equals(lowercaseName, getName().toLowerCase())) {
            lowercaseName = getName().toLowerCase();
        }
        return lowercaseName;
    }

    @Override
    public @NotNull String getDisplayName() {
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
    public boolean isPremium() {
        return premium;
    }

    @Override
    public void setPremium(boolean premium) {
        if (this.premium == premium) {
            return;
        }
        updateCached("premium", premium).thenAccept(val -> {
            this.premium = val;
            if (!val) {
                setUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes()));
            }
        });
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
    public @NotNull String getHashedPassword() {
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
        updateCached("loggedIn", loggedIn).thenAccept(val -> this.loggedIn = val);
        if (loggedIn) {
            Matrix.getAPI().getPlayerManager().setOnlineById(getId());
        } else {
            Matrix.getAPI().getPlayerManager().setOfflineById(getId());
        }
    }

    @Override
    public @NotNull String getLastLocale() {
        return lastLocale;
    }

    @Override
    public void setLastLocale(@NotNull Locale lastLocale) {
        if (Objects.isNull(lastLocale)) {
            return;
        }
        setLastLocale(lastLocale.getLanguage());
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
    public @NotNull String getStaffChannel() {
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
    public long getExp() {
        return Matrix.getAPI().getLevelProvider().getExpByUniqueId(uniqueId);
    }

    @Override
    public void setExp(long l) {
        Matrix.getAPI().getLevelProvider().setExpByUniqueId(uniqueId, l);
    }

    @Override
    public int getLevel() {
        return (int) Matrix.getAPI().getLevelProvider().getLevelByUniqueId(uniqueId);
    }

    @Override
    public @NotNull Set<PlayerOptionType> getOptions() {
        return options;
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
    public @NotNull String getIP() {
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
    public @NotNull Set<String> getIpHistory() {
        return ipHistory;
    }

    @Override
    public @NotNull Date getLastLogin() {
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
    public @NotNull Date getRegistration() {
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
    public @NotNull String getDiscordId() {
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
    public void incrCensoringLevel() {
        censoringLevel++;
        updateCached("censoringLevel");
    }

    @Override
    public int getSpammingLevel() {
        return spammingLevel;
    }

    @Override
    public void incrSpammingLevel() {
        spammingLevel++;
        updateCached("spammingLevel");
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

    @Override
    public @NotNull GameMode getGameMode(String serverGroup) {
        return gameModeByGame.getOrDefault(serverGroup, Matrix.getAPI().getServerInfo().getDefaultGameMode());
    }

    @Override
    public void setGameMode(GameMode gameMode, String serverGroup) {
        if (Objects.equals(gameModeByGame.get(serverGroup), gameMode)) {
            return;
        }
        gameModeByGame.put(serverGroup, gameMode);
        updateCached("gameModeByGame");
    }

    @Override
    public @NotNull CompletableFuture<String> getLastServerGroup() {
        return Matrix.getAPI().getPlayerManager().getGroupById(getId());
    }

    @Override
    public void setLastServerGroup(String serverGroup) {
        Matrix.getAPI().getPlayerManager().setGroupById(getId(), serverGroup);
    }

    @Override
    public @NotNull CompletableFuture<String> getLastServerName() {
        return Matrix.getAPI().getPlayerManager().getServerById(getId());
    }

    @Override
    public void setLastServerName(String lastServerName) {
        Matrix.getAPI().getPlayerManager().setServerById(getId(), lastServerName);
    }

    @Override
    public long getTotalPlayTime(String serverGroup) {
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
    public long getLastPlayTime(String serverGroup) {
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
    public void setLastPlayTime(String serverGroup, long playTime) {
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
    public @NotNull Collection<String> getPlayedGames() {
        return ImmutableSet.of();//ImmutableSet.copyOf(playedGamesMap.keySet());
    }

    @Override
    public long getJoins(String serverGroup) {
        return 0;//playedGamesMap.getOrDefault(gameType, 0L);
    }

    @Override
    public void addPlayedGame(String serverGroup) {
        /*
        if (gameType == GameType.NONE) {
            return;
        }
        playedGamesMap.put(gameType, playedGamesMap.getOrDefault(gameType, 0L) + 1);
        updateCached("playedGamesMap");
         */
    }

    @Override
    public @NotNull CompletableFuture<Boolean> save() {
        Objects.requireNonNull(getName(), "Can't save a player with null name");
        Objects.requireNonNull(getUniqueId(), "Can't save a player with null uniqueId");
        if (getDisplayName() == null) {
            setDisplayName(getName());
        }
        if (lowercaseName == null) {
            setName(name);
        }
        return Matrix.getAPI().getDatabase().save(id == null ? null : getId(), this);
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

    @Deprecated
    public void updateCached(String field) {
        try {
            Matrix.getAPI().getDatabase().updateFieldById(getId(), field, MongoMatrixPlayer.FIELDS.get(field).get(this));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> @NotNull CompletableFuture<T> updateCached(String field, T value) {
        try {
            return Matrix.getAPI().getDatabase().updateFieldById(getId(), field, value);
        } catch (CompletionException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull Set<String> getKnownNames() {
        return knownNames;
    }

    public boolean isBedrock() {
        return bedrock;
    }

    public void setBedrock(boolean bedrock) {
        if (!bedrock) {
            throw new IllegalArgumentException("Bedrock can't be disabled for this player");
        }
        if (!isPremium()) {
            setPremium(true);
        }
        updateCached("bedrock", true).thenAccept(val -> this.bedrock = val);
    }

    @SuppressWarnings("unchecked")
    private void setField(@NotNull Field field, String json) {
        try {
            if (field.getName().equals("name")) {
                Objects.requireNonNull(json, "name");
            }
            if (field.getName().equals("uniqueId")) {
                Objects.requireNonNull(json, "name");
            }
            if (field.getName().equals("options")) {
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
                } catch (@NotNull JsonSyntaxException | IllegalStateException e) {
                    Matrix.getLogger().warn("Field: " + field.getName() + " Json: " + json);
                    e.printStackTrace();
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
