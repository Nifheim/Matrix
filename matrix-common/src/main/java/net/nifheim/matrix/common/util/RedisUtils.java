package net.nifheim.matrix.common.util;

import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.common.player.MatrixPlayerImpl;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class RedisUtils {

    private RedisUtils() {
        throw new RuntimeException("Cannot instantiate RedisUtils");
    }

    public static <T> T getObjectFromHash(@NotNull Map<String, String> hash, Class<T> type) throws ReflectiveOperationException {
        return getObjectFromHash(hash, type, ReflectionUtils.decodeFields(type));
    }

    public static <T> T getObjectFromHash(@NotNull Map<String, String> hash, Class<T> type, Map<String, Field> fields) throws ReflectiveOperationException {
        T instance = type.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Field> ent : fields.entrySet()) {
            String id = ent.getKey(); // field id
            Field field = ent.getValue(); // field object
            ReflectionUtils.setField(instance, field, JsonUtils.parseJsonValue(hash.get(id), field.getType()));
        }
        return instance;
    }

    public static @Nullable MatrixPlayer deserializePlayerFromHash(@NotNull Map<String, String> hash, Logger logger) {
        try {
            Map<String, Object> decodedHash = HashMap.newHashMap(hash.size());
            for (Map.Entry<String, String> ent : hash.entrySet()) {
                decodedHash.put(ent.getKey(), JsonUtils.parseJsonValue(ent.getValue(), PlayerManagerImpl.FIELDS.get(ent.getKey()).getType()));
            }
            return new MatrixPlayerImpl(decodedHash);
        } catch (JsonSyntaxException ex) {
            logger.error("An exception has occurred while deserializing player", ex);
            return null;
        }
    }
}
