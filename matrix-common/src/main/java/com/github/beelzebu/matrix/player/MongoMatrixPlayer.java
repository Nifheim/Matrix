package com.github.beelzebu.matrix.player;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
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
@SuppressWarnings({"FieldMayBeFinal", "UnstableApiUsage"})
@Entity(value = "players", useDiscriminator = false)
public final class MongoMatrixPlayer implements MatrixPlayer {

    public static final transient Map<String, Field> FIELDS = new HashMap<>();

    static {
        Stream.of(MongoMatrixPlayer.class.getDeclaredFields()).filter(field -> !Modifier.isTransient(field.getModifiers())).peek(field -> field.setAccessible(true)).forEach(field -> FIELDS.put(field.getName(), field));
    }

    @Id
    private ObjectId id;
    private @Nullable String idString = null;
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
    private boolean loggedIn;
    private String lastLocale;
    private boolean watcher;
    @Indexed
    private String IP;
    private @NotNull Set<String> ipHistory = new LinkedHashSet<>();
    private Date lastLogin;
    private Date registration;
    private int censoringLevel;
    private int spammingLevel;
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
                if (Objects.equals(id, "name")) {
                    mongoMatrixPlayer.name = hash.get(id).replace("\"", "");
                } else if (hash.containsKey(id)) { // hash contains field
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
        if (id == null) {
            Matrix.getLogger().info("Id not set yet");
        }
        if (id != null && idString == null) {
            return idString = id.toHexString();
        }
        return Objects.requireNonNull(idString);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        if (uniqueId == null) {
            setUniqueId(Matrix.getAPI().getPlayerManager().getUniqueIdById(getId()).join());
        }
        return Objects.requireNonNull(uniqueId, "uniqueId");
    }

    @Override
    public void setUniqueId(@NotNull UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        if (Objects.equals(this.uniqueId, uniqueId)) {
            return;
        }
        if (premium && uniqueId.version() != 4) {
            throw new IllegalArgumentException("Only random uuids are allowed for premium players");
        } else if (!premium && uniqueId.version() != 3) {
            throw new IllegalArgumentException("Can not use a random generated UUID for a cracked player");
        }
        this.uniqueId = updateCached("uniqueId", uniqueId).join();
        save().join();
    }

    @Override
    public @NotNull String getName() {
        return Objects.requireNonNull(this.name, "name");
    }

    @Override
    public void setName(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        knownNames.add(name);
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        updateCached("name", name).thenAccept(val -> Objects.requireNonNull(this.name = val, "name can't be null."));
        updateCached("lowercaseName", lowercaseName).thenAccept(val -> Objects.requireNonNull(this.lowercaseName = val, "name can't be null."));
        updateCached("knownNames", knownNames).thenAccept(val -> Objects.requireNonNull(this.knownNames = val, "known names"));
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
        return false;
    }

    @Override
    public void setAdmin(boolean admin) {
    }

    @Override
    public @NotNull String getHashedPassword() {
        return "";
    }

    @Override
    public void setHashedPassword(String hashedPassword) {
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
    public @Nullable String getStaffChannel() {
        return null;
    }

    @Override
    public void setStaffChannel(String staffChannel) {
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
        return new HashSet<>();
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        return false;
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
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
    public @Nullable String getDiscordId() {
        return null;
    }

    @Override
    public void setDiscordId(String discordId) {
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
        return false;
    }

    @Override
    public void setVanished(boolean vanished) {
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
        Objects.requireNonNull(getUniqueId(), "Can't save a player with null uniqueId");
        Objects.requireNonNull(this.name, "Can't save a player with null name");
        if (this.name != null) {
            if (getDisplayName() == null) {
                setDisplayName(getName());
            }
            if (lowercaseName == null) {
                setName(name);
            }
        }
        return Matrix.getAPI().getDatabase().save(id == null ? null : getId(), this);
    }

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

    @Override
    public void setField(String name, String json) {
        Field field = FIELDS.get(name);
        Objects.requireNonNull(field, "field");
        setField(field, json);
    }

    @Override
    public void setField(String name, Object value) {
        Field field = FIELDS.get(name);
        Objects.requireNonNull(field, "field");
        setField(field, value);
    }

    private void setField(@NotNull Field field, String json) {
        if (field.getName().equals("name")) {
            Objects.requireNonNull(json, "name");
        }
        if (field.getName().equals("uniqueId")) {
            Objects.requireNonNull(json, "uniqueId");
        }
        try {
            Object value = Matrix.GSON.fromJson(json, field.getGenericType());
            setField(field, value);
        } catch (JsonSyntaxException | IllegalStateException e) {
            Matrix.getLogger().warn("Field: " + field.getName() + " Json: " + json);
            e.printStackTrace();
        }
    }

    private void setField(@NotNull Field field, Object value) {
        try {
            String fieldName = field.getName();
            if (fieldName.equals("name") || fieldName.equals("uniqueId")) {
                Objects.requireNonNull(value, "uniqueId or name");
            }
            field.set(this, value);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
