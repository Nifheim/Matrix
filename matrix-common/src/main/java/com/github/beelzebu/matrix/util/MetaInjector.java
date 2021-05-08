package com.github.beelzebu.matrix.util;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public abstract class MetaInjector <P> {

    public static final String ID_KEY = "matrix:id";
    public static final String SERVER_GROUP_KEY = "matrix:server_group";
    public static final String SERVER_NAME_KEY = "matrix:server_name";

    public abstract <T> void setMeta(@NotNull P player, @NotNull String key, @Nullable T meta);

    public abstract <T> @Nullable T getMeta(@NotNull P player, @NotNull String key, @NotNull Class<T> clazz);

    public abstract <T> @NotNull Collection<T> getMeta(@NotNull P player, @NotNull Class<T> clazz);

    public final @Nullable String getId(@NotNull P player) {
        return getMeta(player, ID_KEY, String.class);
    }

    public final @Nullable String getServerGroup(@NotNull P player) { // TODO inject meta on platforms
        return getMeta(player, SERVER_GROUP_KEY, String.class);
    }

    public final @Nullable String getServerName(@NotNull P player) { // TODO inject meta on platforms
        return getMeta(player, SERVER_NAME_KEY, String.class);
    }
}
