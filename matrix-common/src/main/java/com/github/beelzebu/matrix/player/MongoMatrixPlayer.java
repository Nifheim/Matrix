package com.github.beelzebu.matrix.player;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.google.gson.JsonSyntaxException;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
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
        this.id = ObjectId.get();
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
    public CompletableFuture<Void> setUniqueId(@NotNull UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        if (Objects.equals(this.uniqueId, uniqueId)) {
            return CompletableFuture.completedFuture(null);
        }
        if (premium && uniqueId.version() != 4) {
            throw new IllegalArgumentException("Only random uuids are allowed for premium players");
        } else if (!premium && uniqueId.version() != 3) {
            throw new IllegalArgumentException("Can not use a random generated UUID for a cracked player");
        }
        return updateCached("uniqueId", uniqueId).thenAccept(val -> this.uniqueId = val);
    }

    @Override
    public @NotNull String getName() {
        return Objects.requireNonNull(this.name, "name");
    }

    @Override
    public CompletableFuture<Void> setName(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        knownNames.add(name);
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        return updateCached("name", name)
                .thenAccept(val -> Objects.requireNonNull(this.name = val, "name can't be null."))
                .thenRun(() ->
                        updateCached("lowercaseName", lowercaseName)
                                .thenAccept(val -> Objects.requireNonNull(this.lowercaseName = val, "name can't be (null.")))
                .thenRun(() ->
                        updateCached("knownNames", knownNames)
                                .thenAccept(val -> Objects.requireNonNull(this.knownNames = val, "known names")));
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
    public CompletableFuture<Void> setDisplayName(String displayName) {
        if (Objects.equals(this.displayName, displayName) || Objects.isNull(displayName)) {
            return CompletableFuture.completedFuture(null);
        }
        return updateCached("displayName", displayName).thenAccept(val -> this.displayName = val);
    }

    @Override
    public boolean isPremium() {
        return premium;
    }

    @Override
    public CompletableFuture<Void> setPremium(boolean premium) {
        if (this.premium == premium) {
            return CompletableFuture.completedFuture(null);
        }
        return updateCached("premium", premium).thenAccept(val -> {
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
    public CompletableFuture<Void> setRegistered(boolean registered) {
        if (this.registered == registered) {
            return CompletableFuture.completedFuture(null);
        }
        return updateCached("registered", registered).thenAccept(val -> this.registered = val);
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public CompletableFuture<Void> setLoggedIn(boolean loggedIn) {
        if (this.loggedIn == loggedIn) {
            return CompletableFuture.completedFuture(null);
        }
        // TODO: check future chaining instead of locking
        return updateCached("loggedIn", loggedIn).thenAccept(val -> {
            this.loggedIn = val;
            if (val) {
                Matrix.getAPI().getPlayerManager().setOnlineById(getId());
            } else {
                Matrix.getAPI().getPlayerManager().setOfflineById(getId());
            }
        });
    }

    @Override
    public @NotNull String getLastLocale() {
        return lastLocale;
    }

    @Override
    public CompletableFuture<Void> setLastLocale(@NotNull Locale lastLocale) {
        return setLastLocale(lastLocale.getLanguage());
    }

    @Override
    public CompletableFuture<Void> setLastLocale(String lastLocale) {
        if (Objects.equals(this.lastLocale, lastLocale)) {
            return CompletableFuture.completedFuture(null);
        }
        return updateCached("lastLocale", lastLocale).thenAccept(val -> this.lastLocale = val);
    }

    @Override
    public boolean isWatcher() {
        return watcher;
    }

    @Override
    public CompletableFuture<Void> setWatcher(boolean watcher) {
        if (this.watcher == watcher) {
            return CompletableFuture.completedFuture(null);
        }
        this.watcher = watcher;
        return updateCached("watcher", watcher).thenAccept(val -> this.watcher = val);
    }

    @Override
    public long getExp() {
        return Matrix.getAPI().getLevelProvider().getExpByUniqueId(uniqueId);
    }

    @Override
    public CompletableFuture<Void> setExp(long l) {
        return Matrix.getAPI().getPlugin().getBootstrap().getScheduler().makeFuture(() -> Matrix.getAPI().getLevelProvider().setExpByUniqueId(uniqueId, l));
    }

    @Override
    public int getLevel() {
        return (int) Matrix.getAPI().getLevelProvider().getLevelByUniqueId(uniqueId);
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        return false;
    }

    @Override
    public CompletableFuture<Void> setOption(PlayerOptionType option, boolean status) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull String getIP() {
        return IP;
    }

    @Override
    public CompletableFuture<Void> setIP(String IP) {
        if (Objects.equals(this.IP, IP)) {
            return CompletableFuture.completedFuture(null);
        }
        this.IP = IP;
        ipHistory.add(IP);
        return updateCached("IP", IP).thenAccept(val -> this.IP = val).thenRun(() -> updateCached("ipHistory", ipHistory).thenAccept(val -> this.ipHistory = val));
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
    public CompletableFuture<Void> setLastLogin(Date lastLogin) {
        if (Objects.equals(this.lastLogin, lastLogin)) {
            return CompletableFuture.completedFuture(null);
        }
        return updateCached("lastLogin", lastLogin).thenAccept(val -> this.lastLogin = val);
    }

    @Override
    public @NotNull Date getRegistration() {
        return registration;
    }

    public CompletableFuture<Void> setRegistration(Date registration) {
        if (Objects.equals(this.registration, registration)) {
            return CompletableFuture.completedFuture(null);
        }
        return updateCached("registration", registration).thenAccept(val -> this.registration = val);
    }

    @Override
    public int getCensoringLevel() {
        return censoringLevel;
    }

    @Override
    public void incrCensoringLevel() {
        updateCached("censoringLevel", censoringLevel++).thenAccept(val -> this.censoringLevel = val);
    }

    @Override
    public int getSpammingLevel() {
        return spammingLevel;
    }

    @Override
    public void incrSpammingLevel() {
        updateCached("spammingLevel", spammingLevel++).thenAccept(val -> this.spammingLevel = val);
    }

    @Override
    public @NotNull GameMode getGameMode(String serverGroup) {
        return gameModeByGame.getOrDefault(serverGroup, Matrix.getAPI().getServerInfo().getDefaultGameMode());
    }

    @Override
    public CompletableFuture<Void> setGameMode(GameMode gameMode, String serverGroup) {
        if (Objects.equals(gameModeByGame.get(serverGroup), gameMode)) {
            return CompletableFuture.completedFuture(null);
        }
        gameModeByGame.put(serverGroup, gameMode);
        return updateCached("gameModeByGame", gameModeByGame).thenAccept(val -> this.gameModeByGame = val);
    }

    @Override
    public @NotNull CompletableFuture<String> getLastServerGroup() {
        return Matrix.getAPI().getPlayerManager().getGroupById(getId());
    }

    @Override
    public CompletableFuture<Void> setLastServerGroup(String serverGroup) {
        return Matrix.getAPI().getPlayerManager().setGroupById(getId(), serverGroup);
    }

    @Override
    public @NotNull CompletableFuture<String> getLastServerName() {
        return Matrix.getAPI().getPlayerManager().getServerById(getId());
    }

    @Override
    public CompletableFuture<Void> setLastServerName(String lastServerName) {
        return Matrix.getAPI().getPlayerManager().setServerById(getId(), lastServerName);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> save() {
        Objects.requireNonNull(getUniqueId(), "Can't save a player with null uniqueId");
        Objects.requireNonNull(this.name, "Can't save a player with null name");
        if (lowercaseName == null) {
            setName(name);
        }
        return Matrix.getAPI().getDatabase().save(id == null ? null : getId(), this);
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

    @Override
    public CompletableFuture<Void> setField(String name, String json) {
        Field field = FIELDS.get(name);
        Objects.requireNonNull(field, "field");
        return setField(field, json);
    }

    @Override
    public CompletableFuture<Void> setField(String name, Object value) {
        Field field = FIELDS.get(name);
        Objects.requireNonNull(field, "field");
        return setField(field, value);
    }

    private CompletableFuture<Void> setField(@NotNull Field field, String json) {
        if (field.getName().equals("name")) {
            Objects.requireNonNull(json, "name");
        }
        if (field.getName().equals("uniqueId")) {
            Objects.requireNonNull(json, "uniqueId");
        }
        try {
            Object value = Matrix.GSON.fromJson(json, field.getGenericType());
            return setField(field, value);
        } catch (JsonSyntaxException | IllegalStateException e) {
            Matrix.getLogger().warn("Field: " + field.getName() + " Json: " + json);
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> setField(@NotNull Field field, Object value) {
        try {
            String fieldName = field.getName();
            if (fieldName.equals("name") || fieldName.equals("uniqueId")) {
                Objects.requireNonNull(value, "uniqueId or name");
            }
            field.set(this, value);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }
}
