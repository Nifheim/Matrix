package net.nifheim.matrix.common.cache;

import com.google.gson.JsonParseException;
import com.mongodb.DuplicateKeyException;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.messaging.MessagingService;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.messaging.message.FieldUpdateMessage;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.util.RedisManager;
import net.nifheim.matrix.common.util.RedisUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RedisCacheProvider implements CacheProvider<MongoMatrixPlayer> {

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
    public static final String ID_KEY_PREFIX = "matrixid:";
    public static final String UUID_KEY_PREFIX = "matrixuuid:";
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
    public @NotNull Optional<@NotNull String> getHexId(@NotNull UUID uniqueId) {
        logger.info("Requesting hex id for {} from jedis cache.", uniqueId);
        try (Jedis jedis = redisManager.getResource()) {
            String hexId = jedis.get(ID_KEY_PREFIX + uniqueId);
            if (hexId != null) {
                logger.info("HexId {} found for {}", hexId, uniqueId);
                return Optional.of(hexId);
            }
            logger.info("HexId not found for " + uniqueId);
        } catch (JedisException ex) {
            logger.error("An error has occurred getting hex id from cache for %s".formatted(uniqueId), ex);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<UUID> getUniqueId(String hexId) {
        logger.info("Requesting uniqueId for {} from jedis cache.", hexId);
        try (Jedis jedis = redisManager.getResource()) {
            String uuidString = jedis.get(UUID_KEY_PREFIX + hexId);
            if (uuidString != null) {
                logger.info("UUID {} found for {}", uuidString, hexId);
                return Optional.of(UUID.fromString(uuidString));
            }
            logger.info("UUID not found for {}", hexId);
        } catch (JedisException ex) {
            logger.info("An error has occurred getting uuid from cache.", ex);
        }
        return Optional.empty();
    }

    // TODO: check need
    @Override
    public void update(@NotNull UUID uniqueId, @NotNull String hexId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        Objects.requireNonNull(hexId, "hexId");
        String UUID_BY_ID_KEY = UUID_KEY_PREFIX + hexId;
        String ID_BY_UUID_KEY = ID_KEY_PREFIX + uniqueId;
        try (Jedis jedis = redisManager.getResource()) {
            String oldUUID = jedis.get(UUID_BY_ID_KEY);
            try (Pipeline pipeline = jedis.pipelined()) {
                if (oldUUID != null) {
                    pipeline.del(ID_KEY_PREFIX + oldUUID);
                }
                pipeline.setex(UUID_BY_ID_KEY, CACHE_SECONDS, uniqueId.toString());
                pipeline.setex(ID_BY_UUID_KEY, CACHE_SECONDS, hexId);
                pipeline.sync();
            }
        }
    }

    @Override
    public @NotNull Optional<MongoMatrixPlayer> getPlayer(@NotNull String hexId) {
        Objects.requireNonNull(hexId, "hexId");
        logger.info("Requesting player {} from redis cache.", hexId);
        try (Jedis jedis = redisManager.getResource()) {
            Map<String, String> jsonPlayer = jedis.hgetAll(USER_KEY_PREFIX + hexId);
            if (jsonPlayer == null || jsonPlayer.isEmpty()) {
                return Optional.empty();
            }
            if (!jsonPlayer.containsKey("name") || jsonPlayer.get("name").isEmpty()) {
                jedis.del(USER_KEY_PREFIX + hexId);
                return Optional.empty();
            }
            MongoMatrixPlayer deserializedPlayer = RedisUtils.deserializePlayerFromHash(jsonPlayer, logger);
            return Optional.ofNullable(deserializedPlayer);
        } catch (@NotNull JedisException | JsonParseException ex) {
            logger.info("An error has occurred getting player from cache.", ex);
        }
        return Optional.empty();
    }


    @Override
    public MongoMatrixPlayer removePlayer(@NotNull MongoMatrixPlayer player) {
        Objects.requireNonNull(player.getId(), "hexId");
        try (Jedis jedis = redisManager.getResource()) {
            MongoMatrixPlayer cachedPlayer = getPlayer(player.getId()).orElse(player);
            try {
                // TODO: check this logic, was wrong implemented if the goal was to update the uuid in case player is now premium
//                if (player.isPremium()) {
//                    if (cachedPlayer.getUniqueId() != player.getUniqueId()) {
//                        cachedPlayer.setUniqueId(player.getUniqueId());
//                    }
//                }
                jedis.del(USER_KEY_PREFIX + player.getUniqueId()); // remove it from redis
                return cachedPlayer;
            } catch (DuplicateKeyException e) {
                logger.error("An error has occurred removing player from cache.", e);
            }
        } catch (@NotNull JedisException | JsonParseException ex) {
            logger.info("An error has occurred removing player from cache.", ex);
        }
        return null;
    }

    @Override
    public boolean isCached(@NotNull String hexId) {
        Objects.requireNonNull(hexId, "hexId");
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.exists(USER_KEY_PREFIX + hexId);
        }
    }

    @Override
    public void updateCachedFieldById(@Nullable String hexId, @NotNull String field, @Nullable Object value) {
        Objects.requireNonNull(hexId, "hexId");
        Objects.requireNonNull(field, "field");
        if (!isCached(hexId)) {
            logger.warn("Trying to update cached field for a non cached player: {} field: {} value: {} - Skipping", hexId, field, value);
            return;
        }
        if (Objects.equals(field, "name") && value == null) {
            logger.error("Trying to save a null name for {}", hexId);
            throw new NullPointerException("name");
        }
        if (Objects.equals(field, "uniqueId") && value == null) {
            logger.error("Trying to save a null uuid for {}", hexId);
            throw new NullPointerException("uniqueId");
        }
        try (Jedis jedis = redisManager.getResource()) {
            String jsonValue = MatrixCommon.GSON.toJson(value);
            if (value != null) {
                jedis.hset(getUserKey(hexId), field, jsonValue);
            } else {
                jedis.hdel(getUserKey(hexId), field);
            }
            messaging.sendMessage(new FieldUpdateMessage(hexId, field, value, value != null ? value.getClass() : null));
        }
    }


    @Override
    public void add(@NotNull MongoMatrixPlayer matrixPlayer) {
        writeToRedis(matrixPlayer, false);
    }

    @Override
    public void update(@NotNull MongoMatrixPlayer matrixPlayer) {
        writeToRedis(matrixPlayer, true);
    }

    private void writeToRedis(@NotNull MongoMatrixPlayer matrixPlayer, boolean dirty) {
        Objects.requireNonNull(matrixPlayer.getUniqueId(), "uniqueId");
        Objects.requireNonNull(matrixPlayer.getId(), "hexId");
        Objects.requireNonNull(matrixPlayer.getName(), "name");
        if (isCached(matrixPlayer.getId())) {
            logger.warn("Tried to save already cached player");
            return;
        }
        logger.info("Player {} ({} - {}) is being written to redis cache. Only dirty fields: {}", matrixPlayer.getName(), matrixPlayer.getUniqueId(), matrixPlayer.getId(), dirty);
        try (Jedis jedis = redisManager.getResource(); Pipeline pipeline = jedis.pipelined()) {
            Collection<String> fields = dirty ? matrixPlayer.$dirtyFields : PlayerManagerImpl.FIELDS.keySet();
            for (String id : fields) {
                Field field = PlayerManagerImpl.FIELDS.get(id);
                if (field == null) {
                    logger.error("Field with id '{}' does not exists on '{}' for {} ({} - {})", id, matrixPlayer.getClass().getName(), matrixPlayer.getName(), matrixPlayer.getUniqueId(), matrixPlayer.getId());
                    continue;
                }
                try {
                    if (field.get(matrixPlayer) != null) {
                        pipeline.hset(getUserKey(matrixPlayer.getId()), id, MatrixCommon.GSON.toJson(field.get(matrixPlayer)));
                    } else {
                        pipeline.hdel(getUserKey(matrixPlayer.getId()), id);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("An error has occurred saving player to cache.", e);
                }
            }
            pipeline.set(ID_KEY_PREFIX + matrixPlayer.getUniqueId(), matrixPlayer.getId());
            pipeline.set(UUID_KEY_PREFIX + matrixPlayer.getId(), matrixPlayer.getUniqueId().toString());
            pipeline.sync();
            matrixPlayer.$dirtyFields.clear();
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

    private @NotNull String getUserKey(String hexId) {
        return USER_KEY_PREFIX + hexId;
    }
}
