package net.nifheim.matrix.common.player.meta;

import java.util.Collection;
import net.kyori.adventure.identity.Identified;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jaime Suárez
 */
public abstract class PlayerMetaInjector <P extends Identified> {

    public static final String ID_KEY = "matrix:id";
    public static final String SERVER_GROUP_KEY = "matrix:server_group";
    public static final String SERVER_NAME_KEY = "matrix:server_name";

    public abstract <T> void setMeta(@NotNull P player, @NotNull String key, @Nullable T meta);

    public abstract <T> @Nullable T getMeta(@NotNull P player, @NotNull String key, @NotNull Class<T> clazz);

    public abstract <T> @NotNull Collection<T> getMeta(@NotNull P player, @NotNull Class<T> clazz);

    public final @Nullable String getId(@NotNull P player) {
        return getMeta(player, ID_KEY, String.class);
    }

    public final @Nullable String getServerGroup(@NotNull P player) {
        return getMeta(player, SERVER_GROUP_KEY, String.class);
    }

    public final @Nullable String getServerName(@NotNull P player) {
        return getMeta(player, SERVER_NAME_KEY, String.class);
    }
}
