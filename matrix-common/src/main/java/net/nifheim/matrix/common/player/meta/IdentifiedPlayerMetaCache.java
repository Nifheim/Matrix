package net.nifheim.matrix.common.player.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kyori.adventure.identity.Identified;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A meta injector that caches the meta in memory for implementations that don't support meta injection.
 *
 * @author Jaime Suárez
 */
public class IdentifiedPlayerMetaCache <P extends Identified> extends PlayerMetaInjector<P> {

    private final Map<UUID, Map<String, Object>> cachedMeta = new HashMap<>();

    @Override
    public <T> void setMeta(@NotNull P player, @NotNull String key, T meta) {
        Map<String, Object> playerMeta = cachedMeta.computeIfAbsent(player.identity().uuid(), uuid -> new HashMap<>());
        playerMeta.put(key, meta);
        cachedMeta.put(player.identity().uuid(), playerMeta);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getMeta(@NotNull P player, @NotNull String key, @NotNull Class<T> clazz) {
        Map<String, Object> meta = cachedMeta.get(player.identity().uuid());
        if (meta == null) {
            return null;
        }
        return (T) meta.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull Collection<T> getMeta(@NotNull P player, @NotNull Class<T> clazz) {
        return (Collection<T>) cachedMeta.get(player.identity().uuid()).values().stream().filter(clazz::isInstance).collect(Collectors.toList());
    }

    protected @Nullable Map<String, Object> deleteMeta(@NotNull P player) {
        return cachedMeta.remove(player.identity().uuid());
    }
}
