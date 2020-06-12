package com.github.beelzebu.matrix.cache;

import com.github.beelzebu.coins.api.CoinsAPI;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import com.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.util.RedisManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class CacheProviderImpl implements CacheProvider {

    public static final String UUID_KEY_PREFIX = "matrixuuid:";
    public static final String NAME_KEY_PREFIX = "matrixname:";
    public static final String USER_KEY_PREFIX = "matrixuser:";
    public static final String SERVER_GROUP_KEY_PREFIX = "matrix:group:"; // set
    public static final String SERVER_GROUP_NAME_KEY_PREFIX = "matrix:server:"; // value
    public static final String DISCORD_CODE_KEY_PREFIX = "matrixdiscord:";

    private final RedisManager redisManager;

    public CacheProviderImpl(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    @Override
    public Optional<UUID> getUniqueId(String name) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            return Optional.ofNullable(jedis.exists(UUID_KEY_PREFIX + name) ? UUID.fromString(jedis.get(UUID_KEY_PREFIX + name)) : null);
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getName(UUID uniqueId) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            return Optional.ofNullable(jedis.get(NAME_KEY_PREFIX + uniqueId));
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
        return Optional.empty();
    }

    @Override
    public void update(String name, UUID uniqueId) {
        if (name == null || uniqueId == null) {
            return;
        }

        String uuidStoreKey = UUID_KEY_PREFIX + name;
        String nameStoreKey = NAME_KEY_PREFIX + uniqueId;

        UUID oldUniqueId = null;
        String oldName = null;

        try (Jedis jedis = redisManager.getPool().getResource()) {
            if (jedis.exists(uuidStoreKey)) { // check for old uuid to update
                oldUniqueId = UUID.fromString(jedis.get(uuidStoreKey));
                if (oldUniqueId != uniqueId) { // check if old and new are the same
                    // this is caused when player changed from cracked to premium
                    jedis.del(NAME_KEY_PREFIX + oldUniqueId);
                    jedis.set(uuidStoreKey, uniqueId.toString());
                    CoinsAPI.createPlayer(name, uniqueId);
                    CoinsAPI.setCoins(uniqueId, CoinsAPI.getCoins(oldUniqueId));
                    CoinsAPI.resetCoins(oldUniqueId);
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
        }

        if (Objects.equals(name, oldName == null ? name : oldName) && Objects.equals(uniqueId, oldUniqueId == null ? uniqueId : oldUniqueId)) {
            return;
        }
        new NameUpdatedMessage(name, oldName == null ? name : oldName, uniqueId, oldUniqueId == null ? uniqueId : oldUniqueId).send();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(UUID uniqueId) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            try {
                if (jedis.exists(USER_KEY_PREFIX + uniqueId)) {
                    return Optional.ofNullable(MongoMatrixPlayer.fromHash(jedis.hgetAll(USER_KEY_PREFIX + uniqueId)));
                }
            } catch (ClassCastException e) {
                return getPlayer(uniqueId);
            } catch (NullPointerException e) {
                Matrix.getLogger().debug(e);
                Matrix.getLogger().debug(Matrix.GSON.toJson(jedis.hgetAll(USER_KEY_PREFIX + uniqueId)));
                jedis.del(USER_KEY_PREFIX + uniqueId);
            }
        } catch (JedisDataException e) {
            return getPlayer(uniqueId);
        } catch (JedisException | JsonParseException e) {
            Matrix.getLogger().debug(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MatrixPlayer> getPlayer(String name) {
        UUID uniqueId = getUniqueId(name).orElse(Matrix.getAPI().getPlugin().getUniqueId(name));
        return uniqueId != null ? getPlayer(uniqueId) : Optional.empty();
    }

    @Override
    public Set<MatrixPlayer> getPlayers() {
        Set<MatrixPlayer> players = new HashSet<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            do {
                for (String playerKey : scan.getResult()) {
                    MatrixPlayer matrixPlayer = getPlayer(UUID.fromString(playerKey.split(":")[1])).orElse(null);
                    if (matrixPlayer == null) {
                        Matrix.getLogger().info("Invalid player key stored in redis: " + playerKey);
                        continue;
                    }
                    players.add(matrixPlayer);
                }
                scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting all players from cache.");
            Matrix.getLogger().debug(ex);
        }
        return players;
    }

    @Override
    public void removePlayer(MatrixPlayer player) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            getPlayer(player.getUniqueId()).orElse(player).save(); // save cached version to database
            jedis.del(USER_KEY_PREFIX + player.getUniqueId()); // remove it from redis
        } catch (JedisException | JsonParseException ex) {
            Matrix.getLogger().debug(ex);
        }
    }

    @Override
    public boolean isCached(UUID uniqueId) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            return jedis.exists(USER_KEY_PREFIX + uniqueId);
        }
    }

    @Override
    public Set<String> getGroups() {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            return getGroups(jedis);
        }
    }

    public Set<String> getGroups(Jedis jedis) {
        Set<String> groups = new HashSet<>();
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_GROUP_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            do {
                for (String groupKey : scan.getResult()) {
                    groups.add(groupKey.split(":")[2]);
                }
                scan = jedis.scan(cursor, new ScanParams().match(SERVER_GROUP_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting all server groups from cache.");
            Matrix.getLogger().debug(ex);
        }
        return ImmutableSet.copyOf(groups);
    }

    @Override
    public Map<String, Set<String>> getAllServers() {
        Map<String, Set<String>> serverGroups = new HashMap<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            Set<String> groups = getGroups(jedis);
            for (String group : groups) {
                Set<String> servers = ImmutableSet.copyOf(jedis.smembers(SERVER_GROUP_KEY_PREFIX + group));
                serverGroups.put(group, servers);
            }
        }
        return ImmutableMap.copyOf(serverGroups);
    }

    @Override
    public Set<String> getServers(String group) {
        Set<String> servers;
        try (Jedis jedis = redisManager.getPool().getResource()) {
            servers = ImmutableSet.copyOf(jedis.smembers(SERVER_GROUP_KEY_PREFIX + group));
        }
        return servers;
    }

    @Override
    public boolean isGroupRegistered(String group) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            return isGroupRegistered(jedis, group);
        }
    }

    private boolean isGroupRegistered(Jedis jedis, String group) {
        try {
            Boolean result = jedis.exists(SERVER_GROUP_KEY_PREFIX + group);
            if (result != null) {
                return result;
            } else {
                throw new NullPointerException("Result returned from redis server is null.");
            }
        } catch (JedisException | NullPointerException e) {
            Matrix.getLogger().info("An error occurred while checking if group '" + group + "' is registered in cache.");
            Matrix.getLogger().debug(e);
        }
        return false;
    }

    @Override
    public void registerGroup(String name) {
        // TODO: check NOOP
        /*
        try (Jedis jedis = redisManager.getPool().getResource()) {
            if (isGroupRegistered(jedis, name)) {
                return;
            }
            jedis.sadd(SERVER_GROUP_KEY_PREFIX + name);
        }
         */
    }

    @Override
    public void addServer(String group, String server) {
        addServer(group, new String[]{server});
    }

    @Override
    public void addServer(String group, String[] servers) {
        try (Jedis jedis = redisManager.getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
            for (String server : servers) {
                pipeline.set(SERVER_GROUP_NAME_KEY_PREFIX + server, group);
                pipeline.sadd(SERVER_GROUP_KEY_PREFIX + group, server);
                pipeline.sync();
            }
        }
    }

    @Override
    public void removeServer(String name) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String group = jedis.get(SERVER_GROUP_NAME_KEY_PREFIX + name);
            try (Pipeline pipeline = jedis.pipelined()) {
                pipeline.del(SERVER_GROUP_NAME_KEY_PREFIX + name);
                pipeline.srem(SERVER_GROUP_KEY_PREFIX + group, name);
                pipeline.sync();
            }
        }
    }

    @Override
    public void updateCachedField(MatrixPlayer matrixPlayer, String field, Object value) {
        if (!isCached(matrixPlayer.getUniqueId())) {
            Matrix.getLogger().info("Trying to update cached field for a non cached player: " + matrixPlayer.getName() + " field: " + field + " value: " + value);
            return;
        }
        if (Objects.equals(field, "name") && matrixPlayer.getName() == null) {
            Matrix.getLogger().debug("Trying to save a null name for " + matrixPlayer.getUniqueId());
            return;
        }
        if (Objects.equals(field, "uniqueId") && matrixPlayer.getUniqueId() == null) {
            Matrix.getLogger().debug("Trying to save a null uuid for " + matrixPlayer.getName());
            return;
        }
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String jsonValue = Matrix.GSON.toJson(value);
            Matrix.getLogger().debug("Updating " + matrixPlayer.getName() + " field `" + field + "' with value `" + jsonValue + "'");
            if (value != null) {
                jedis.hset(matrixPlayer.getRedisKey(), field, jsonValue);
            } else {
                jedis.hdel(matrixPlayer.getRedisKey(), field);
            }
            new FieldUpdate(matrixPlayer.getUniqueId(), field, jsonValue).send();
        }
    }

    @Override
    public MatrixPlayer saveToCache(MatrixPlayer matrixPlayer) {
        Objects.requireNonNull(matrixPlayer.getUniqueId(), "UUID can't be null");
        Objects.requireNonNull(matrixPlayer.getName(), "name can't be null");
        if (Objects.isNull(matrixPlayer.getLowercaseName())) {
            matrixPlayer.setName(matrixPlayer.getName());
        }
        try (Jedis jedis = redisManager.getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
            MongoMatrixPlayer.FIELDS.forEach((id, field) -> {
                try {
                    if (field.get(matrixPlayer) != null) {
                        pipeline.hset(matrixPlayer.getRedisKey(), id, Matrix.GSON.toJson(field.get(matrixPlayer)));
                    } else {
                        pipeline.hdel(matrixPlayer.getRedisKey(), id);
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
    public void setDiscordVerificationCode(String name, String code) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            jedis.setex(DISCORD_CODE_KEY_PREFIX + code, 360, name);
        }
    }
}
