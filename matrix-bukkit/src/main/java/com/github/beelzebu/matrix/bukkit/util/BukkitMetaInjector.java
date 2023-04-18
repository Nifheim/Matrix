package com.github.beelzebu.matrix.bukkit.util;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.util.MetaInjector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jaime Suárez
 */
@SuppressWarnings("unchecked")
public class BukkitMetaInjector extends MetaInjector<Player> {

    private final MatrixBukkitAPI api;
    private final Set<String> registeredKeys = new HashSet<>();

    public BukkitMetaInjector(MatrixBukkitAPI api) {
        this.api = api;
    }

    @Override
    public <T> void setMeta(@NotNull Player player, @NotNull String key, T meta) {
        registeredKeys.add(key);
        player.setMetadata(key, new FixedMetadataValue(api.getPlugin().getBootstrap(), meta));
    }

    @Override
    public <T> @Nullable T getMeta(@NotNull Player player, @NotNull String key, @NotNull Class<T> clazz) {
        for (MetadataValue metadatum : player.getMetadata(key)) {
            if (metadatum.getOwningPlugin() == api.getPlugin().getBootstrap()) {
                Object value = metadatum.value();
                if (clazz.isInstance(value)) {
                    return (T) value;
                }
            }
        }
        return null;
    }

    @Override
    public <T> @NotNull Collection<T> getMeta(@NotNull Player player, @NotNull Class<T> clazz) {
        List<T> metadata = new ArrayList<>();
        for (String registeredKey : registeredKeys) {
            for (MetadataValue metadatum : player.getMetadata(registeredKey)) {
                if (metadatum.getOwningPlugin() == api.getPlugin().getBootstrap()) {
                    Object value = metadatum.value();
                    if (clazz.isInstance(value)) {
                        metadata.add((T) value);
                    }
                }
            }
        }
        return metadata;
    }
}
