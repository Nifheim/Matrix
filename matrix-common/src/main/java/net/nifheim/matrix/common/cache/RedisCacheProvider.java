package net.nifheim.matrix.common.cache;

import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.util.RedisManager;
import net.nifheim.matrix.common.util.RedisUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

public class RedisCacheProvider implements CacheProvider<MatrixPlayer> {

    /*
      player keys:

      uuid<->id
      matrixid:<uuid>       -> id
      matrixuuid:<mongoid>  -> uuid

      matrixuser:<mongoid>
        key: value
     */
    //public static final String DISCORD_CODE_KEY_PREFIX = "matrixdiscord:";
    public static final long CACHE_SECONDS = 60_000;
    public static final String USER_KEY_PREFIX = "matrixuser:";
    private final Logger logger;
    private final RedisManager redisManager;
    private final MessagingService messaging;
    private final ServerManager serverManager;

    public RedisCacheProvider(Logger logger, RedisManager redisManager, MessagingService messaging, ServerManager serverManager) {
        this.logger = logger;
        this.redisManager = redisManager;
        this.messaging = messaging;
        this.serverManager = serverManager;
    }

    @Override
    public @NotNull Optional<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        Objects.requireNonNull(uniqueId);
        logger.info("Requesting player {} from redis cache.", uniqueId);
        try (Jedis jedis = redisManager.getResource()) {
            Map<String, String> jsonPlayer = jedis.hgetAll(USER_KEY_PREFIX + uniqueId);
            if (jsonPlayer == null || jsonPlayer.isEmpty()) {
                return Optional.empty();
            }
            if (!jsonPlayer.containsKey("name") || jsonPlayer.get("name").isEmpty()) {
                jedis.del(USER_KEY_PREFIX + uniqueId);
                return Optional.empty();
            }
            MatrixPlayer deserializedPlayer = RedisUtils.deserializePlayerFromHash(jsonPlayer, logger);
            return Optional.ofNullable(deserializedPlayer);
        } catch (@NotNull JedisException | JsonParseException ex) {
            logger.info("An error has occurred getting player from cache.", ex);
        }
        return Optional.empty();
    }

    @Override
    public MatrixPlayer removePlayer(@NotNull MatrixPlayer player) {
        try (Jedis jedis = redisManager.getResource()) {

            // TODO: check this logic, was wrong implemented if the goal was to update the uuid in case player is now premium
//                if (player.isPremium()) {
//                    if (cachedPlayer.getUniqueId() != player.getUniqueId()) {
//                        cachedPlayer.setUniqueId(player.getUniqueId());
//                    }
//                }

            jedis.unlink(USER_KEY_PREFIX + player.getUniqueId());
        }
        return player;
    }

    @Override
    public boolean isCached(UUID uniqueId) {
        Objects.requireNonNull(uniqueId);
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.exists(USER_KEY_PREFIX + uniqueId);
        }
    }


    @Override
    public void updateCachedFieldById(@NotNull UUID uniqueId, @NotNull String field, @Nullable Object value) {
        Objects.requireNonNull(uniqueId);
        Objects.requireNonNull(field);
        if (!isCached(uniqueId)) {
            logger.warn("Trying to update cached field for a non cached player: {} field: {} value: {} - Skipping", uniqueId, field, value);
            return;
        }
        if (Objects.equals(field, "name") && value == null) {
            logger.error("Trying to save a null name for {}", uniqueId);
            throw new NullPointerException("name");
        }
        if (Objects.equals(field, "uniqueId") && value == null) {
            logger.error("Trying to save a null uuid for {}", uniqueId);
            throw new NullPointerException("uniqueId");
        }
        try (Jedis jedis = redisManager.getResource()) {
            String jsonValue = MatrixCommon.GSON.toJson(value);
            if (value != null) {
                jedis.hset(getUserKey(uniqueId), field, jsonValue);
            } else {
                jedis.hdel(getUserKey(uniqueId), field);
            }
            // removed
            //messaging.sendMessage(new FieldUpdateMessage(uniqueId, field, value, value != null ? value.getClass() : null));
        }
    }

    @Override
    public void add(@NotNull MatrixPlayer player) {
        writeToRedis(player);
    }

    @Override
    public void update(@NotNull MatrixPlayer player) {
        writeToRedis(player);
    }

    private void writeToRedis(@NotNull MatrixPlayer matrixPlayer) {
        Objects.requireNonNull(matrixPlayer.getUniqueId(), "uniqueId");
        Objects.requireNonNull(matrixPlayer.getId(), "hexId");
        Objects.requireNonNull(matrixPlayer.getName(), "name");
        if (isCached(matrixPlayer.getUniqueId())) {
            logger.warn("Tried to save already cached player");
            return;
        }
        logger.info("Player {} ({} - {}) is being written to redis cache.", matrixPlayer.getName(), matrixPlayer.getUniqueId(), matrixPlayer.getId());
        try (Jedis jedis = redisManager.getResource(); Pipeline pipeline = jedis.pipelined()) {
            Collection<String> fields = PlayerManagerImpl.FIELDS.keySet();
            for (String id : fields) {
                Field field = PlayerManagerImpl.FIELDS.get(id);
                if (field == null) {
                    logger.error("Field with id '{}' does not exists on '{}' for {} ({} - {})", id, matrixPlayer.getClass().getName(), matrixPlayer.getName(), matrixPlayer.getUniqueId(), matrixPlayer.getId());
                    continue;
                }
                try {
                    if (field.get(matrixPlayer) != null) {
                        pipeline.hset(getUserKey(matrixPlayer.getUniqueId()), id, MatrixCommon.GSON.toJson(field.get(matrixPlayer)));
                    } else {
                        pipeline.hdel(getUserKey(matrixPlayer.getUniqueId()), id);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("An error has occurred saving player to cache.", e);
                }
                pipeline.expire(getUserKey(matrixPlayer.getUniqueId()), CACHE_SECONDS);
            }
            pipeline.sync();
        }
        logger.info("Player {} saved to cache", matrixPlayer.getId());
    }

    @Override
    public void shutdown() {
        serverManager.removeServer().join();
    }

    @Override
    public boolean isActive() {
        return redisManager.isClosed();
    }

    private @NotNull String getUserKey(UUID uniqueId) {
        return USER_KEY_PREFIX + uniqueId;
    }
}
