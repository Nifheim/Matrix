package net.nifheim.matrix.common.player;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.nifheim.matrix.api.player.MatrixPlayer;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents an implementation of the MatrixPlayer interface that is stored in a MongoDB database.
 * It provides methods to get and set player data such as uniqueId, name, premium state, registration status, etc.
 *
 * @author Jaime Suárez
 */
@Entity(value = "players", useDiscriminator = false)
public final class MongoMatrixPlayer implements MatrixPlayer {

    @Id
    private ObjectId id;
    @Transient
    private @Nullable
    transient String idString = null;
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
    private Date lastLogin;
    @Transient
    public transient List<String> $dirtyFields = new ArrayList<>();

    public MongoMatrixPlayer(UUID uniqueId, @NotNull String name) {
        this.id = ObjectId.get();
        this.uniqueId = uniqueId;
        this.name = name;
        this.lowercaseName = name.toLowerCase();
    }

    public MongoMatrixPlayer() {
    }


    @Override
    public @NotNull String getId() {
        Objects.requireNonNull(id, "id");
        if (idString == null) {
            return idString = id.toHexString();
        }
        return Objects.requireNonNull(idString);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return Objects.requireNonNull(uniqueId, "uniqueId");
    }

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
        this.uniqueId = uniqueId;
        $dirtyFields.add("uniqueId");
    }

    @Override
    public @NotNull String getName() {
        return Objects.requireNonNull(this.name, "name");
    }

    public void setName(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        if (Objects.equals(this.name, name) && Objects.equals(this.lowercaseName, name.toLowerCase()) && knownNames.contains(name)) {
            return;
        }
        knownNames.add(name);
        this.name = name;
        this.lowercaseName = name.toLowerCase();
        $dirtyFields.add("name");
    }

    @Override
    public @NotNull String getLowercaseName() {
        return lowercaseName;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    public void setDisplayName(String displayName) {
        if (Objects.equals(this.displayName, displayName)) {
            return;
        }
        this.displayName = displayName;
        $dirtyFields.add("displayName");
    }

    @Override
    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        if (this.premium == premium) {
            return;
        }
        if (!premium) {
            setUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes()));
        }
        this.premium = premium;
        $dirtyFields.add("premium");
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        if (this.registered == registered) {
            return;
        }
        this.registered = registered;
        $dirtyFields.add("registered");
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        if (this.loggedIn == loggedIn) {
            return;
        }
        this.loggedIn = loggedIn;
        $dirtyFields.add("loggedIn");
    }

    @Override
    public @NotNull String getLastLocale() {
        return lastLocale;
    }

    public void setLastLocale(@NotNull Locale lastLocale) {
        setLastLocale(lastLocale.getLanguage());
    }

    public void setLastLocale(String lastLocale) {
        if (Objects.equals(this.lastLocale, lastLocale)) {
            return;
        }
        this.lastLocale = lastLocale;
        $dirtyFields.add("lastLocale");
    }

    @Override
    public @Nullable Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        if (Objects.equals(this.lastLogin, lastLogin)) {
            return;
        }
        this.lastLogin = lastLogin;
        $dirtyFields.add("lastLogin");
    }

    @Override
    public @NotNull Date getRegistration() {
        return id.getDate();
    }

    public @NotNull Set<String> getKnownNames() {
        return knownNames;
    }

    public boolean isDirty() {
        return !$dirtyFields.isEmpty();
    }
}
