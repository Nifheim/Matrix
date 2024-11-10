package net.nifheim.matrix.common.player;

import java.util.UUID;
import net.kyori.adventure.identity.Identified;
import org.jetbrains.annotations.Nullable;

/**
 * A proxy to get the platform player from the platform specific player object.
 *
 * @param <P> The platform specific player object.
 * @author Jaime Suárez
 * @see net.nifheim.matrix.api.player.PlayerManager
 * @see net.nifheim.matrix.api.player.MatrixPlayer
 * @see net.nifheim.matrix.common.player.meta.PlayerMetaInjector
 */
@FunctionalInterface
public interface PlayerProxy <P extends Identified> {

    /**
     * Get the platform player by their {@link UUID}.
     *
     * @param uniqueId {@link UUID} of the player to get.
     * @return The platform player if found, or null if not found.
     */
    @Nullable
    public abstract P getPlatformPlayer(UUID uniqueId);
}
