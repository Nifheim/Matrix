package com.github.beelzebu.matrix.cache;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.server.ServerInfo;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class CacheProviderImpl implements CacheProvider {

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
    private final MatrixAPIImpl<?> api;

    public CacheProviderImpl(MatrixAPIImpl<?> api) {
        this.api = api;
    }

    @Override
    public Optional<UUID> getUniqueIdByName(@NotNull String name) {
        name = name.toLowerCase();
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String uuidString = jedis.get(UUID_KEY_PREFIX + name);
            return Optional.ofNullable(uuidString != null ? UUID.fromString(uuidString) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getUniqueIdById(String hexId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String uuidString = jedis.get(UUID_KEY_PREFIX + hexId);
            return Optional.ofNullable(uuidString != null ? UUID.fromString(uuidString) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getName(@NotNull UUID uniqueId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return Optional.ofNullable(jedis.get(NAME_KEY_PREFIX + uniqueId));
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getNameById(String hexId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return Optional.ofNullable(jedis.get(NAME_KEY_PREFIX + hexId));
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHexId(@NotNull UUID uniqueId) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getHexId(jedis, uniqueId);
        }
    }

    private Optional<String> getHexId(Jedis jedis, @NotNull UUID uniqueId) {
        String hexId = jedis.get(ID_KEY_PREFIX + uniqueId);
        return Optional.ofNullable(hexId);
    }

    @Override
    public Optional<String> getHexIdByName(@NotNull String name) {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getHexId(jedis, name);
        }
    }

    private Optional<String> getHexId(Jedis jedis, @NotNull String name) {
        String hexId = jedis.get(ID_KEY_PREFIX + name);
        return Optional.ofNullable(hexId);
    }

    @Override
    public void update(@NotNull String name, @NotNull UUID uniqueId, @NotNull String hexId) {
        // TODO: update hex id too
        String uuidById = UUID_KEY_PREFIX + hexId;
        String uuidByName = UUID_KEY_PREFIX + name;
        String nameById = NAME_KEY_PREFIX + hexId;
        String nameByUuid = NAME_KEY_PREFIX + uniqueId;
        String idByUuid = ID_KEY_PREFIX + uniqueId;
        String idByName = ID_KEY_PREFIX + name;

        try (Jedis jedis = api.getRedisManager().getResource(); Pipeline pipeline = jedis.pipelined()) {
            pipeline.set(uuidById, uniqueId.toString());
            pipeline.set(uuidByName, uniqueId.toString());
            pipeline.set(nameById, name);
            pipeline.set(nameByUuid, name);
            pipeline.set(idByUuid, hexId);
            pipeline.set(idByName, hexId);
            /*
            if (jedis.exists(uuidStoreKey)) { // check for old uuid to update
                oldUniqueId = UUID.fromString(jedis.get(uuidStoreKey));
                if (oldUniqueId != uniqueId) { // check if old and new are the same
                    // this is caused when player changed from cracked to premium
                    jedis.del(NAME_KEY_PREFIX + oldUniqueId);
                    jedis.set(uuidStoreKey, uniqueId.toString());
                }
            } else { // store uuid because it doesn't exists.
                jedis.set(uuidStoreKey, uniqueId.toString());
            }
            if (jedis.exists(nameStoreKey)) { // check for old name to update
                oldName = jedis.get(nameStoreKey);
                if (!Objects.equals(oldName, name)) { // check if old and new are the same
                    jedis.del(UUID_KEY_PREFIX + oldName);
                    jedis.set(nameStoreKey, name);
                }
            } else { // store name because it doesn't exists.
                jedis.set(nameStoreKey, name);
            }
             */
        }
        /*
        if (Objects.equals(name, oldName == null ? name : oldName) && Objects.equals(uniqueId, oldUniqueId == null ? uniqueId : oldUniqueId)) {
            return;
        }
        new NameUpdatedMessage(name, oldName == null ? name : oldName, uniqueId, oldUniqueId == null ? uniqueId : oldUniqueId).send();
        */
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        String hexId = api.getPlugin().getHexId(uniqueId).orElse(null);
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getPlayer(jedis, hexId != null ? hexId : getHexId(jedis, uniqueId).orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getPlayer(jedis, hexId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayerByName(@NotNull String name) {
        // TODO: query local data first, then fallback to cache
        UUID uniqueId = getUniqueIdByName(name).orElse(api.getPlugin().getUniqueId(name));
        return uniqueId != null ? getPlayer(uniqueId) : Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayerById(String hexId) {
        MatrixPlayer cachedPlayer = cachedPlayers.getIfPresent(hexId);
        if (cachedPlayer != null) {
            return Optional.of(cachedPlayer);
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return getPlayer(jedis, hexId);
        }
    }

    private Optional<MatrixPlayer> getPlayer(Jedis jedis, String hexId) {
        if (hexId == null) {
            return Optional.empty();
        }
        MatrixPlayer cachedPlayer = cachedPlayers.getIfPresent(hexId);
        if (cachedPlayer != null) {
            return Optional.of(cachedPlayer);
        }
        try {
            Map<String, String> jsonPlayer = jedis.hgetAll(UUID_KEY_PREFIX + hexId);
            if (jsonPlayer == null || jsonPlayer.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(MongoMatrixPlayer.fromHash(jsonPlayer));
        } catch (JedisException | JsonParseException e) {
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
    public void removePlayer(MatrixPlayer player) {
        cachedPlayers.invalidate(player.getId());
        try (Jedis jedis = api.getRedisManager().getResource()) {
            MongoMatrixPlayer cachedPlayer = (MongoMatrixPlayer) getPlayer(player.getUniqueId()).orElse(player);
            try {
                if (player.isPremium()) {
                    if (cachedPlayer.getUniqueId() != player.getUniqueId()) {
                        cachedPlayer.setUniqueId(player.getUniqueId());
                    }
                    if (!Objects.equals(cachedPlayer.getName(), player.getName())) {
                        cachedPlayer.setName(player.getName());
                    }
                }
                cachedPlayer.save();
                jedis.del(USER_KEY_PREFIX + player.getUniqueId()); // remove it from redis
            } catch (DuplicateKeyException e) {
                e.printStackTrace();
            }
        } catch (JedisException | JsonParseException ex) {
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
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return isCached(jedis, getHexId(jedis, name).orElse(null));
        }
    }

    @Override
    public boolean isCachedById(String hexId) {
        if (cachedPlayers.getIfPresent(hexId) != null) {
            return true;
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return isCached(jedis, hexId);
        }
    }

    private boolean isCached(Jedis jedis, String hexId) {
        if (hexId == null) { // hex id was not found using name or uuid, so we don't event query redis again
            return false;
        }
        return jedis.exists(USER_KEY_PREFIX + hexId);
    }

    @Override
    public <T> T updateCachedFieldById(String hexId, @NotNull String field, T value) {
        if (hexId == null) {
            return value;
        }
        if (!isCachedById(hexId)) {
            Matrix.getLogger().info("Trying to update cached field for a non cached player: " + hexId + " field: " + field + " value: " + value);
            return value;
        }
        if (Objects.equals(field, "name") && value == null) {
            Matrix.getLogger().debug("Trying to save a null name for " + hexId);
            return null;
        }
        if (Objects.equals(field, "uniqueId") && value == null) {
            Matrix.getLogger().debug("Trying to save a null uuid for " + hexId);
            return null;
        }
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String jsonValue = Matrix.GSON.toJson(value);
            Matrix.getLogger().debug("Updating " + hexId + " field `" + field + "' with value `" + jsonValue + "'");
            if (value != null) {
                jedis.hset(getUserKey(hexId), field, jsonValue);
            } else {
                jedis.hdel(getUserKey(hexId), field);
            }
            new FieldUpdate(getUniqueIdById(hexId).orElse(api.getDatabase().getPlayerById(hexId).join().getUniqueId()), field, jsonValue).send();
        }
        return value;
    }

    @Override
    public @NotNull MatrixPlayer saveToCache(MatrixPlayer matrixPlayer) {
        Objects.requireNonNull(matrixPlayer.getUniqueId(), "UUID can't be null");
        Objects.requireNonNull(matrixPlayer.getName(), "name can't be null");
        if (isCached(matrixPlayer.getUniqueId())) {
            Matrix.getLogger().info("Tried to save already cached player");
            return matrixPlayer;
        }
        if (Objects.isNull(matrixPlayer.getLowercaseName())) {
            matrixPlayer.setName(matrixPlayer.getName());
        }
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

    public Set<MatrixPlayer> getPlayers(ServerInfo serverInfo) {
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

    private String getUserKey(String hexId) {
        return USER_KEY_PREFIX + hexId;
    }
}
