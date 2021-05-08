package com.github.beelzebu.matrix.util;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public abstract class MetaInjector <P> {

    public static final String ID_KEY = "matrix:id";

    public abstract <T> void setMeta(P player, String key, T meta);

    public abstract <T> T getMeta(P player, String key, Class<T> clazz);

    public abstract <T> Collection<T> getMeta(P player, Class<T> clazz);

    public final @Nullable String getId(P player) {
        return getMeta(player, ID_KEY, String.class);
    }
}
