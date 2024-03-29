package com.github.beelzebu.matrix.cache;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.messaging.message.FieldUpdateMessage;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonParseException;
import com.mongodb.DuplicateKeyException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class CacheProviderImpl implements CacheProvider {

    public static final int CACHE_SECONDS = 60_000;
    public static final String ID_KEY_PREFIX = "matrixid:";
    public static final String UUID_KEY_PREFIX = "matrixuuid:";
    public static final String NAME_KEY_PREFIX = "matrixname:";
    public static final String USER_KEY_PREFIX = "matrixuser:";
    private final Cache<String, MatrixPlayer> cachedPlayers = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).weakValues().build();
    /*
      player keys:

      uuid<->id<->name
      matrixid:<uuid>       -> id
      matrixid:<name>       -> id
      matrixuuid:<name>     -> uuid
      matrixuuid:<mongoid>  -> uuid
      matrixname:<uuid>     -> name
      matrixname:<mongoid>  -> name

      matrixuser:<mongoid>
        key: value
     */
    //public static final String DISCORD_CODE_KEY_PREFIX = "matrixdiscord:";
    private final MatrixAPIImpl api;

    public CacheProviderImpl(MatrixAPIImpl api) {
        this.api = api;
    }

    @Override
    public @NotNull Optional<UUID> getUniqueIdByName(@NotNull String name) {
        name = name.toLowerCase();
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String uuidString = jedis.get(UUID_KEY_PREFIX + name);
            return Optional.ofNullable(uuidString != null ? UUID.fromString(uuidString) : null);
        } catch (@NotNull JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<UUID> getUniqueIdById(String hexId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String uuidString = jedis.get(UUID_KEY_PREFIX + hexId);
            return Optional.ofNullable(uuidString != null ? UUID.fromString(uuidString) : null);
        } catch (@NotNull JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> getName(@NotNull UUID uniqueId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return Optional.ofNullable(jedis.get(NAME_KEY_PREFIX + uniqueId));
        } catch (@NotNull JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> getNameById(String hexId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return Optional.ofNullable(jedis.get(NAME_KEY_PREFIX + hexId));
        } catch (@NotNull JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> getHexId(@NotNull UUID uniqueId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getHexId(jedis, uniqueId);
        }
    }

    private Optional<String> getHexId(@NotNull Jedis jedis, @NotNull UUID uniqueId) {
        String hexId = jedis.get(ID_KEY_PREFIX + uniqueId);
        return Optional.ofNullable(hexId);
    }

    @Override
    public @NotNull Optional<String> getHexIdByName(@NotNull String name) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getHexId(jedis, name);
        }
    }

    private Optional<String> getHexId(@NotNull Jedis jedis, @NotNull String name) {
        Matrix.getLogger().debug("Requesting hex id for " + name + " from cache.");
        String hexId = jedis.get(ID_KEY_PREFIX + name.toLowerCase());
        if (hexId != null) {
            Matrix.getLogger().debug("HexId " + hexId + " found for " + name);
        } else {
            Matrix.getLogger().debug("HexId not found for " + name);
        }
        return Optional.ofNullable(hexId);
    }

    @Override
    public void update(@NotNull String name, @NotNull UUID uniqueId, @NotNull String hexId) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(uniqueId, "uniqueId");
        Objects.requireNonNull(hexId, "hexId");
        name = name.toLowerCase();
        String UUID_BY_ID_KEY = UUID_KEY_PREFIX + hexId;
        String UUID_BY_NAME_KEY = UUID_KEY_PREFIX + name;
        String NAME_BY_ID_KEY = NAME_KEY_PREFIX + hexId;
        String NAME_BY_UUID_KEY = NAME_KEY_PREFIX + uniqueId;
        String ID_BY_UUID_KEY = ID_KEY_PREFIX + uniqueId;
        String ID_BY_NAME_KEY = ID_KEY_PREFIX + name;

        try (Jedis jedis = api.getRedisManager().getResource()) {
            String oldUUID;
            String oldName;
            try (Pipeline pipeline = jedis.pipelined()) {
                Response<String> oldUUIDResponse = pipeline.get(UUID_BY_ID_KEY);
                Response<String> oldNameResponse = pipeline.get(NAME_BY_ID_KEY);
                pipeline.sync();
                oldUUID = oldUUIDResponse.get();
                oldName = oldNameResponse.get();
            }
            if (oldName != null || oldUUID != null) {
                try (Pipeline pipeline = jedis.pipelined()) {
                    if (oldName != null) {
                        pipeline.del(UUID_KEY_PREFIX + oldName);
                        pipeline.del(ID_KEY_PREFIX + oldName);
                    }
                    if (oldUUID != null) {
                        pipeline.del(NAME_KEY_PREFIX + oldUUID);
                        pipeline.del(ID_KEY_PREFIX + oldUUID);
                    }
                    pipeline.sync();
                }
            }
            try (Pipeline pipeline = jedis.pipelined()) {
                pipeline.setex(UUID_BY_ID_KEY, CACHE_SECONDS, uniqueId.toString());
                pipeline.setex(UUID_BY_NAME_KEY, CACHE_SECONDS, uniqueId.toString());
                pipeline.setex(NAME_BY_ID_KEY, CACHE_SECONDS, name);
                pipeline.setex(NAME_BY_UUID_KEY, CACHE_SECONDS, name);
                pipeline.setex(ID_BY_UUID_KEY, CACHE_SECONDS, hexId);
                pipeline.setex(ID_BY_NAME_KEY, CACHE_SECONDS, hexId);
                pipeline.sync();
            }
        }
    }

    @Override
    public @NotNull Optional<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        String hexId = api.getPlugin().getHexId(uniqueId).orElse(null);
        if (hexId != null) {
            MatrixPlayer matrixPlayer = cachedPlayers.getIfPresent(hexId);
            if (matrixPlayer != null) {
                return Optional.of(matrixPlayer);
            }
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getPlayer(jedis, hexId != null ? hexId : getHexId(jedis, uniqueId).orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<MatrixPlayer> getPlayerByName(@NotNull String name) {
        Matrix.getLogger().debug("Requesting player " + name + " from cache.");
        try (Jedis jedis = api.getRedisManager().getResource()) {
            Optional<String> hexId = getHexId(jedis, name);
            if (hexId.isPresent()) {
                return getPlayerById(hexId.get());
            }
            Matrix.getLogger().debug("HexId was not found for " + name + " so we can't get it from cache");
            return Optional.empty();
        }
    }

    @Override
    public @NotNull Optional<MatrixPlayer> getPlayerById(@NotNull String hexId) {
        MatrixPlayer cachedPlayer = cachedPlayers.getIfPresent(hexId);
        if (cachedPlayer != null) {
            return Optional.of(cachedPlayer);
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getPlayer(jedis, hexId);
        }
    }

    private Optional<MatrixPlayer> getPlayer(@NotNull Jedis jedis, @Nullable String hexId) {
        if (hexId == null) {
            return Optional.empty();
        }
        MatrixPlayer cachedPlayer = cachedPlayers.getIfPresent(hexId);
        if (cachedPlayer != null) {
            return Optional.of(cachedPlayer);
        }
        try {
            Map<String, String> jsonPlayer = jedis.hgetAll(USER_KEY_PREFIX + hexId);
            if (jsonPlayer == null || jsonPlayer.isEmpty()) {
                return Optional.empty();
            }
            if (!jsonPlayer.containsKey("name") || jsonPlayer.get("name").isEmpty()) {
                jedis.del(USER_KEY_PREFIX + hexId);
                return Optional.empty();
            }
            return Optional.ofNullable(MongoMatrixPlayer.fromHash(jsonPlayer));
        } catch (@NotNull JedisException | JsonParseException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Set<MatrixPlayer> getPlayers() {
        Set<MatrixPlayer> players = new HashSet<>();
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(100));
            do {
                for (String playerKey : scan.getResult()) {
                    MatrixPlayer matrixPlayer = getPlayer(jedis, playerKey.replaceFirst(USER_KEY_PREFIX, "")).orElse(null);
                    if (matrixPlayer == null) {
                        Matrix.getLogger().info("Invalid player key stored in redis: " + playerKey);
                        continue;
                    }
                    players.add(matrixPlayer);
                }
                scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(100));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting all players from cache.");
            Matrix.getLogger().debug(ex);
        }
        return players;
    }

    @Override
    public void removePlayer(@NotNull MatrixPlayer player) {
        cachedPlayers.invalidate(player.getId());
        try (Jedis jedis = api.getRedisManager().getResource()) {
            MongoMatrixPlayer cachedPlayer = (MongoMatrixPlayer) getPlayer(player.getUniqueId()).orElse(player);
            try {
                if (player.isPremium()) {
                    if (cachedPlayer.getUniqueId() != player.getUniqueId()) {
                        cachedPlayer.setUniqueId(player.getUniqueId());
                    }
                }
                cachedPlayer.save().thenAccept(p -> jedis.del(USER_KEY_PREFIX + player.getUniqueId())); // remove it from redis
            } catch (DuplicateKeyException e) {
                e.printStackTrace();
            }
        } catch (@NotNull JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
    }

    @Override
    public boolean isCached(@NotNull UUID uniqueId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return isCached(jedis, getHexId(jedis, uniqueId).orElse(null));
        }
    }

    @Override
    public boolean isCachedByName(@NotNull String name) {
        name = name.toLowerCase();
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return isCached(jedis, getHexId(jedis, name).orElse(null));
        }
    }

    @Override
    public boolean isCachedById(@NotNull String hexId) {
        return isCachedById(hexId, true);
    }

    public boolean isCachedById(@NotNull String hexId, boolean includeLocal) {
        if (includeLocal && cachedPlayers.getIfPresent(hexId) != null) {
            return true;
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return isCached(jedis, hexId);
        }
    }

    private boolean isCached(@NotNull Jedis jedis, @Nullable String hexId) {
        if (hexId == null) { // hex id was not found using name or uuid, so we don't event query redis again
            return false;
        }
        return jedis.exists(USER_KEY_PREFIX + hexId);
    }

    @Override
    public <T> @NotNull T updateCachedFieldById(@Nullable String hexId, @NotNull String field, @Nullable T value) {
        Objects.requireNonNull(hexId, "hexId");
        if (!isCachedById(hexId, false)) {
            Matrix.getLogger().debug("Trying to update cached field for a non cached player: " + hexId + " field: " + field + " value: " + value + " - Skipping");
            return Objects.requireNonNull(value, "value");
        }
        if (Objects.equals(field, "name") && value == null) {
            Matrix.getLogger().debug("Trying to save a null name for " + hexId);
            throw new NullPointerException("name");
        }
        if (Objects.equals(field, "uniqueId") && value == null) {
            Matrix.getLogger().debug("Trying to save a null uuid for " + hexId);
            throw new NullPointerException("uniqueId");
        }
        if (value == null) {
            Matrix.getLogger().info("Updating field " + field + " with null value for " + hexId);
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String jsonValue = Matrix.GSON.toJson(value);
            Matrix.getLogger().debug("Updating " + hexId + " field `" + field + "' with value `" + jsonValue + "'");
            if (value != null) {
                jedis.hset(getUserKey(hexId), field, jsonValue);
            } else {
                jedis.hdel(getUserKey(hexId), field);
            }
            api.getMessaging().sendMessage(new FieldUpdateMessage(hexId, field, value, value.getClass()));
        }
        return value;
    }

    @Override
    public @NotNull MatrixPlayer saveToCache(@NotNull MatrixPlayer matrixPlayer) {
        Objects.requireNonNull(matrixPlayer.getUniqueId(), "UUID can't be null");
        try {
            Objects.requireNonNull(matrixPlayer.getName(), "name can't be null");
        } catch (NullPointerException e) {
            Matrix.getLogger().info("Null name for player " + matrixPlayer.getUniqueId());
            return matrixPlayer;
        }
        if (isCached(matrixPlayer.getUniqueId())) {
            Matrix.getLogger().info("Tried to save already cached player");
            return matrixPlayer;
        }
        //cachedPlayers.put(matrixPlayer.getId(), matrixPlayer);
        try (Jedis jedis = api.getRedisManager().getResource(); Pipeline pipeline = jedis.pipelined()) {
            MongoMatrixPlayer.FIELDS.forEach((id, field) -> {
                try {
                    if (field.get(matrixPlayer) != null) {
                        pipeline.hset(getUserKey(matrixPlayer.getId()), id, Matrix.GSON.toJson(field.get(matrixPlayer)));
                    } else {
                        pipeline.hdel(getUserKey(matrixPlayer.getId()), id);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            pipeline.sync();
        }
        return matrixPlayer;
    }

    @Override
    public void purgeForAllPlayers(@NotNull String field) {
        Set<UUID> playerUUIDs = new HashSet<>();
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(100));
            do {
                for (String playerKey : scan.getResult()) {
                    playerUUIDs.add(UUID.fromString(playerKey.replaceFirst(USER_KEY_PREFIX, "")));
                }
                scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(100));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
            Iterator<UUID> it = playerUUIDs.iterator();
            while (it.hasNext()) {
                UUID uuid = it.next();
                jedis.hdel(USER_KEY_PREFIX + uuid, field);
                it.remove();
            }
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred deleting field for players on cache.");
            Matrix.getLogger().debug(ex);
        }
    }

    @Override
    public void shutdown() {
        api.getServerManager().removeServer(api.getServerInfo());
    }

    public @NotNull Set<MatrixPlayer> getPlayers(@NotNull ServerInfo serverInfo) {
        Set<MatrixPlayer> players = new HashSet<>();
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(100));
            do {
                for (String playerKey : scan.getResult()) {
                    try {
                        String hexId = playerKey.replaceFirst(USER_KEY_PREFIX, "");
                        MatrixPlayer matrixPlayer = getPlayer(jedis, hexId).orElse(null);
                        if (matrixPlayer == null) {
                            continue;
                        }
                        if (!matrixPlayer.isLoggedIn()) {
                            continue;
                        }
                        if (!matrixPlayer.getLastServerName().equals(serverInfo.getServerName())) {
                            continue;
                        }
                        players.add(matrixPlayer);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(100));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting online players from cache.");
            Matrix.getLogger().debug(ex);
        }
        return players;
    }

    private @NotNull String getUserKey(String hexId) {
        return USER_KEY_PREFIX + hexId;
    }

    public MatrixPlayer getLocalCached(String hexId) {
        return cachedPlayers.getIfPresent(hexId);
    }
}
