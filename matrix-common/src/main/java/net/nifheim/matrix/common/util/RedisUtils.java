package net.nifheim.matrix.common.util;

import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Field;
import java.util.Map;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import org.jetbrains.annotations.NotNull;
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

    public static MongoMatrixPlayer deserializePlayerFromHash(@NotNull Map<String, String> hash, Logger logger) {
        MongoMatrixPlayer player = new MongoMatrixPlayer();
        String objectId = hash.get("id");
        String name = hash.get("name");
        String uniqueId = hash.get("uniqueId");
        for (Map.Entry<String, Field> ent : PlayerManagerImpl.FIELDS.entrySet()) {
            String id = ent.getKey(); // field id
            Field field = ent.getValue(); // field object
            try {
                Object value = JsonUtils.parseJsonValue(hash.get(id), field.getType());
                ReflectionUtils.setField(player, field, value);
            } catch (@NotNull IllegalArgumentException | NullPointerException | JsonSyntaxException | ReflectiveOperationException e) {
                logger.warn("Error setting field {} with value {} for player {} ({}) with uniqueId {}", id, hash.get(id), objectId, name, uniqueId);
                logger.error("Error setting field", e);
                if (id.equals("name") || id.equals("uniqueId") || id.equals("id")) {
                    logger.error("Error setting field {} with value {}, returning null", id, hash.get(id));
                    return null;
                }
            }
        }
        return player;
    }
}
