package io.github.beelzebu.matrix.api.cache;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public interface CacheProvider {

    Optional<UUID> getUniqueId(String name);

    Optional<String> getName(UUID uniqueId);

    Optional<MatrixPlayer> getPlayer(UUID uniqueId);

    Optional<MatrixPlayer> getPlayer(String name);

    Set<MatrixPlayer> getPlayers();
}
