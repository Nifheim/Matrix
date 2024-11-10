package net.nifheim.matrix.paper.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.nifheim.matrix.paper.MatrixPaper;
import net.nifheim.matrix.common.player.meta.PlayerMetaInjector;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jaime Suárez
 */
@SuppressWarnings("unchecked")
public class PaperPlayerMetaInjector extends PlayerMetaInjector<Player> {

    private final MatrixPaper bootstrap;
    private final Set<String> registeredKeys = new HashSet<>();

    public PaperPlayerMetaInjector(MatrixPaper bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public <T> void setMeta(@NotNull Player player, @NotNull String key, T meta) {
        registeredKeys.add(key);
        player.setMetadata(key, new FixedMetadataValue(bootstrap, meta));
    }

    @Override
    public <T> @Nullable T getMeta(@NotNull Player player, @NotNull String key, @NotNull Class<T> clazz) {
        for (MetadataValue metadataValue : player.getMetadata(key)) {
            if (metadataValue.getOwningPlugin() == bootstrap) {
                Object value = metadataValue.value();
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
            for (MetadataValue metadataValue : player.getMetadata(registeredKey)) {
                if (metadataValue.getOwningPlugin() == bootstrap) {
                    Object value = metadataValue.value();
                    if (clazz.isInstance(value)) {
                        metadata.add((T) value);
                    }
                }
            }
        }
        return metadata;
    }
}
