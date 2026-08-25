package net.nifheim.matrix.common.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import net.nifheim.matrix.api.player.MatrixPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MatrixPlayerImpl implements MatrixPlayer {

    private @Nullable Long id;
    private @NotNull UUID uniqueId;
    private @Nullable Long discordId;
    private @NotNull String name;
    private final @Nullable String displayName;
    private boolean premium;
    private boolean registered;
    private final @NotNull String lastLocale;
    private final @Nullable Date lastLogin;
    private final @Nullable Date registration;
    private transient boolean loggedIn = false;

    public MatrixPlayerImpl(ResultSet resultSet) throws SQLException {
        id = resultSet.getLong("id");
        uniqueId = UUID.fromString(resultSet.getString("uniqueId"));
        name = resultSet.getString("name");
        displayName = resultSet.getString("display_name");
        premium = resultSet.getBoolean("premium");
        registered = resultSet.getBoolean("registered");
        lastLocale = resultSet.getString("locale");
        lastLogin = resultSet.getDate("last_login");
        registration = resultSet.getDate("registered_at");
    }

    public MatrixPlayerImpl(Map<String, Object> data) {
        id = (Long) data.get("id");
        uniqueId = UUID.fromString((String) data.get("uniqueId"));
        name = (String) data.get("name");
        displayName = (String) data.get("display_name");
        premium = (boolean) data.get("premium");
        registered = (boolean) data.get("registered");
        lastLocale = (String) data.get("locale");
        lastLogin = (Date) data.get("last_login");
        registration = (Date) data.get("registered_at");
    }

    @Override
    public @Nullable Long getId() {
        return id;
    }

    @Override
    public Long getDiscordId() {
        return null;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    @Override
    public boolean isPremium() {
        return premium;
    }

    @Override
    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public boolean isLoggedIn() {
        return premium || loggedIn;
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @Override
    public @NotNull String getLocale() {
        return lastLocale;
    }

    @Override
    public @Nullable Date getLastLogin() {
        return lastLogin;
    }

    @Override
    public @Nullable Date getRegistration() {
        return registration;
    }
}
