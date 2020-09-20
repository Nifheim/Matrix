package com.github.beelzebu.matrix.cache;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import com.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.github.beelzebu.matrix.util.RedisManager;
import com.google.gson.JsonParseException;
import com.mongodb.DuplicateKeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    public static final String SERVER_INFO_KEY_PREFIX = "matrix:serverinfo:"; // hash
    // (group:)(gametype:)[name/servertype][n]
    // group     : string
    // gametype  : string
    // gamemode  : string
    // heartbeat : long
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
            return getPlayer(jedis, uniqueId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<MatrixPlayer> getPlayer(Jedis jedis, UUID uniqueId) {
        try {
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
            Matrix.getLogger().debug(e);
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
                    MatrixPlayer matrixPlayer = getPlayer(jedis, UUID.fromString(playerKey.split(":")[1])).orElse(null);
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
    public Set<UUID> getOnlinePlayers() {
        Set<UUID> uniqueIds = new HashSet<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            do {
                for (String playerKey : scan.getResult()) {
                    try {
                        UUID uniqueId = UUID.fromString(playerKey.split(":")[1]);
                        if (!jedis.exists(USER_KEY_PREFIX + uniqueId)) {
                            continue;
                        }
                        if (!jedis.hget(USER_KEY_PREFIX + uniqueId, "loggedIn").equals("true")) {
                            continue;
                        }
                        uniqueIds.add(uniqueId);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting online players from cache.");
            Matrix.getLogger().debug(ex);
        }
        return uniqueIds;
    }

    @Override
    public void removePlayer(MatrixPlayer player) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
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
    public boolean isCached(UUID uniqueId) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            return jedis.exists(USER_KEY_PREFIX + uniqueId);
        }
    }

    @Override
    public Set<String> getGroupsNames() {
        Set<String> groups = new HashSet<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            do {
                for (String groupKey : scan.getResult()) {
                    try {
                        String server = groupKey.replaceFirst(SERVER_INFO_KEY_PREFIX, "");
                        groups.add(server.split(":", 2)[0]);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting groups from cache.");
            Matrix.getLogger().debug(ex);
        }
        return groups;
    }

    @Override
    public Map<String, Set<ServerInfo>> getAllServers() {
        Map<String, Set<ServerInfo>> servers = new HashMap<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            do {
                for (String key : scan.getResult()) {
                    try {
                        ServerInfo serverInfo = getServerInfo(key, jedis.hgetAll(key));
                        if (serverInfo == null) {
                            jedis.del(key);
                            continue;
                        }
                        servers.computeIfAbsent(serverInfo.getGroupName(), k -> new HashSet<>()).add(serverInfo);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting all servers from cache.");
            Matrix.getLogger().debug(ex);
        }
        return servers;
    }

    @Override
    public Set<ServerInfo> getServers(String group) {
        Set<ServerInfo> servers = new HashSet<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + group + "*").count(Integer.MAX_VALUE));
            do {
                for (String key : scan.getResult()) {
                    try {
                        ServerInfo serverInfo = getServerInfo(key, jedis.hgetAll(key));
                        if (serverInfo == null) {
                            jedis.del(key);
                            continue;
                        }
                        servers.add(serverInfo);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + group + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting all servers from cache.");
            Matrix.getLogger().debug(ex);
        }
        return servers;
    }

    private ServerInfo getServerInfo(String name, Map<String, String> data) {
        if (data.isEmpty()) {
            return null;
        }
        return new ServerInfoImpl(name.replaceFirst(SERVER_INFO_KEY_PREFIX, ""), data);
    }

    @Override
    public boolean isGroupRegistered(String group) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + group + "*").count(Integer.MAX_VALUE));
            do {
                if (!scan.getResult().isEmpty()) {
                    return true;
                }
                scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + group + "*").count(Integer.MAX_VALUE));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting all servers from cache.");
            Matrix.getLogger().debug(ex);
        }
        return false;
    }

    @Override
    public void addServer(ServerInfo serverInfo) {
        addServers(new ServerInfo[]{serverInfo});
    }

    @Override
    public void addServers(ServerInfo[] serverInfos) {
        try (Jedis jedis = redisManager.getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
            for (ServerInfo serverInfo : serverInfos) {
                // group     : string
                // gametype  : string
                // gamemode  : string
                // heartbeat : long
                pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "group", serverInfo.getGroupName());
                pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "gametype", serverInfo.getGameType().toString());
                pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "gamemode", serverInfo.getDefaultGameMode().toString());
                pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "servertype", serverInfo.getServerType().name());
                pipeline.sync();
            }
        }
    }

    @Override
    public void removeServer(ServerInfo serverInfo) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            jedis.del(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName());
        }
    }

    @Override
    public void heartbeat(ServerInfo serverInfo) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            jedis.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "heartbeat", String.valueOf(System.currentTimeMillis()));
        }
    }

    @Override
    public long getLastHeartbeat(ServerInfo serverInfo) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            if (!jedis.hexists(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "group")) {
                return 0;
            }
            return Long.parseLong(jedis.hget(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "heartbeat"));
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

    @Override
    public void purgeForAllPlayers(String field) {
        Set<UUID> playerUUIDs = new HashSet<>();
        try (Jedis jedis = redisManager.getPool().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
            do {
                for (String playerKey : scan.getResult()) {
                    playerUUIDs.add(UUID.fromString(playerKey.split(":")[1]));
                }
                scan = jedis.scan(cursor, new ScanParams().match(USER_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
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
    }
}
