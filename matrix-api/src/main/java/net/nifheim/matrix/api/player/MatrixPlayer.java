package net.nifheim.matrix.api.player;

import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a player in the system. Provides methods to access and modify player data such as
 * unique identifiers, names, registration status, and session information.
 */
public interface MatrixPlayer {

    /**
     * Retrieves the unique identifier of the player.
     *
     * @return the unique identifier of the player as a {@link Long}.
     */
    Long getId();

    Long getDiscordId();

    /**
     * <p>
     * Get the minecraft {@link UUID} of this user, the {@link UUID#version()} method can be used to check if it is a
     * Mojang assigned {@link UUID} or a cracked {@link UUID}.
     * </p>
     * <p>
     * Premium players have version 4 {@link UUID}s, while cracked players have version 3 {@link UUID}s, they are
     * generated using the bytes of {@code "OfflinePlayer:" + name}.
     * </p>
     *
     * @return current {@link UUID} of this user.
     */
    @NotNull UUID getUniqueId();

    /**
     * Updates the unique identifier of the player.
     * This {@link UUID} is used to uniquely distinguish the player and can be updated to reflect a new identifier
     * in cases such as changes to premium status or identifier migration.
     *
     * @param uniqueId the new {@link UUID} to assign to the player. Must not be null.
     */
    void setUniqueId(@NotNull UUID uniqueId);

    /**
     * Get the real name of this user.
     *
     * @return Saved Name of this user.
     */
    @NotNull String getName();

    void setName(@NotNull String name);

    /**
     * Get the displayed name of this user, this name is used in messages and chat.
     *
     * @return Saved display name of this user.
     */
    @NotNull String getDisplayName();

    boolean isPremium();

    void setPremium(boolean premium);

    boolean isRegistered();

    void setRegistered(boolean registered);

    boolean isLoggedIn();

    void setLoggedIn(boolean loggedIn);

    @NotNull String getLocale();

    @Nullable Date getLastLogin();

    @Nullable Date getRegistration();

}
