package com.github.beelzebu.matrix.player;

import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerManager;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * @author Beelzebu
 */
public abstract class AbstractPlayerManager <P> implements PlayerManager<P> {

    // This key is used to save all online players on the network
    public static final String ONLINE_PLAYERS_TOTAL_KEY = "matrix:onlineplayers:total"; // set
    // This key is used to save all online players on a group
    public static final String ONLINE_PLAYERS_GROUP_KEY_PREFIX = "matrix:onlineplayers:group:"; // set
    // This key is used to save all players on a server
    public static final String ONLINE_PLAYERS_SERVER_KEY_PREFIX = "matrix:onlineplayers:server:"; // set
    // This key is used to save current server for a player
    public static final String PLAYER_SERVER_KEY_PREFIX = "matrix:player:server:"; // key // matrix:player:server:<id> = serverName
    // This key is used to save current server group for a player
    public static final String PLAYER_GROUP_KEY_PREFIX = "matrix:player:group:"; // key // matrix:player:group:<id> = groupName
    protected final MatrixAPIImpl<P> api;

    // TODO: check usage of meta injector for most data instead of the external cache

    public AbstractPlayerManager(MatrixAPIImpl<P> api) {
        this.api = api;
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayerById(String hexId) {
        return api.getDatabase().getPlayerById(hexId);
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayer(UUID uniqueId) {
        return api.getDatabase().getPlayer(uniqueId);
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayerByName(String name) {
        return api.getDatabase().getPlayerByName(name);
    }

    @Override
    public CompletableFuture<String> getHexId(UUID uniqueId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getHexId(uniqueId).orElse(api.getDatabase().getStorage().getPlayer(uniqueId).getId()));
    }

    @Override
    public CompletableFuture<String> getHexIdByName(String name) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getHexIdByName(name).orElse(api.getDatabase().getStorage().getPlayerByName(name).getId()));
    }

    @Override
    public CompletableFuture<UUID> getUniqueIdById(String hexId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getUniqueIdById(hexId).orElse(api.getDatabase().getStorage().getPlayerById(hexId).getUniqueId()));
    }

    @Override
    public CompletableFuture<UUID> getUniqueIdByName(String name) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getUniqueIdByName(name).orElse(api.getDatabase().getStorage().getPlayerByName(name).getUniqueId()));
    }

    @Override
    public CompletableFuture<String> getNameById(String hexId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getNameById(hexId).orElse(api.getDatabase().getStorage().getPlayerById(hexId).getName()));
    }

    @Override
    public CompletableFuture<String> getName(UUID uniqueId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getName(uniqueId).orElse(api.getDatabase().getStorage().getPlayer(uniqueId).getName()));
    }

    @Override
    public CompletableFuture<Set<UUID>> getOnlinePlayers() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Set<UUID> onlinePlayers = new HashSet<>();
            try (Jedis jedis = api.getRedisManager().getResource()) {
                for (String uuidString : jedis.smembers(ONLINE_PLAYERS_TOTAL_KEY)) {
                    onlinePlayers.add(UUID.fromString(uuidString));
                }
            }
            return onlinePlayers;
        });
    }

    @Override
    public CompletableFuture<Set<UUID>> getOnlinePlayersInServer(String server) {
        Objects.requireNonNull(server, "Server can't be null");
        return getPlayers(server, ONLINE_PLAYERS_SERVER_KEY_PREFIX);
    }

    @Override
    public CompletableFuture<Set<UUID>> getOnlinePlayersInGroup(String group) {
        Objects.requireNonNull(group, "Group can't be null");
        return getPlayers(group, ONLINE_PLAYERS_GROUP_KEY_PREFIX);
    }

    @NotNull
    private CompletableFuture<Set<UUID>> getPlayers(String where, String keyPrefix) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Set<UUID> onlinePlayers = new HashSet<>();
            try (Jedis jedis = api.getRedisManager().getResource()) {
                for (String uuidString : jedis.smembers(keyPrefix + where)) {
                    onlinePlayers.add(UUID.fromString(uuidString));
                }
            }
            return onlinePlayers;
        });
    }

    @Override
    public CompletableFuture<Integer> getOnlinePlayerCount() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(this::getOnlinePlayerCountSync);
    }

    public int getOnlinePlayerCountSync() {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return Math.toIntExact(jedis.scard(ONLINE_PLAYERS_TOTAL_KEY));
        }
    }

    @Override
    public CompletableFuture<Integer> getOnlinePlayerCountInServer(String server) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return Math.toIntExact(jedis.scard(ONLINE_PLAYERS_SERVER_KEY_PREFIX + server));
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getOnlinePlayerCountInGroup(String group) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return Math.toIntExact(jedis.scard(ONLINE_PLAYERS_GROUP_KEY_PREFIX + group));
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isOnline(UUID uniqueId, @Nullable String groupName, @Nullable String serverName) {
        if (uniqueId == null) {
            return CompletableFuture.completedFuture(false);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            String hexId = api.getDatabase().getCacheProvider().getHexId(uniqueId).orElseGet(() -> {
                MatrixPlayer matrixPlayer = api.getDatabase().getStorage().getPlayer(uniqueId);
                if (matrixPlayer != null) {
                    return matrixPlayer.getId();
                }
                return null;
            });
            if (hexId == null) {
                return false;
            }
            try (Jedis jedis = api.getRedisManager().getResource()) {
                if (groupName == null) {
                    return jedis.sismember(ONLINE_PLAYERS_TOTAL_KEY, hexId);
                } else {
                    if (serverName == null) {
                        return jedis.sismember(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId);
                    } else {
                        return jedis.sismember(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId)
                                && jedis.sismember(ONLINE_PLAYERS_SERVER_KEY_PREFIX + serverName, hexId);
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isOnlineByName(String name, @Nullable String groupName, @Nullable String serverName) {
        if (name == null) {
            return CompletableFuture.completedFuture(false);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            String hexId = api.getDatabase().getCacheProvider().getHexIdByName(name).orElseGet(() -> {
                MatrixPlayer matrixPlayer = api.getDatabase().getStorage().getPlayerByName(name);
                if (matrixPlayer != null) {
                    return matrixPlayer.getId();
                }
                return null;
            });
            if (hexId == null) {
                return false;
            }
            try (Jedis jedis = api.getRedisManager().getResource()) {
                if (groupName == null) {
                    return jedis.sismember(ONLINE_PLAYERS_TOTAL_KEY, hexId);
                } else {
                    if (serverName == null) {
                        return jedis.sismember(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId);
                    } else {
                        return jedis.sismember(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId)
                                && jedis.sismember(ONLINE_PLAYERS_SERVER_KEY_PREFIX + serverName, hexId);
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isOnlineById(String hexId, @Nullable String groupName, @Nullable String serverName) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(false);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                if (groupName == null) {
                    return jedis.sismember(ONLINE_PLAYERS_TOTAL_KEY, hexId);
                } else {
                    if (serverName == null) {
                        return jedis.sismember(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId);
                    } else {
                        return jedis.sismember(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId)
                                && jedis.sismember(ONLINE_PLAYERS_SERVER_KEY_PREFIX + serverName, hexId);
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> setOnlineById(String hexId) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                jedis.sadd(ONLINE_PLAYERS_TOTAL_KEY, hexId);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setOfflineById(String hexId) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                String serverName = jedis.get(PLAYER_SERVER_KEY_PREFIX + hexId);
                String serverGroup = jedis.get(PLAYER_GROUP_KEY_PREFIX + hexId);
                try (Pipeline pipeline = jedis.pipelined()) {
                    pipeline.srem(ONLINE_PLAYERS_SERVER_KEY_PREFIX + serverName, hexId);
                    pipeline.srem(ONLINE_PLAYERS_GROUP_KEY_PREFIX + serverGroup, hexId);
                    pipeline.srem(ONLINE_PLAYERS_TOTAL_KEY, hexId);
                    pipeline.sync();
                }
            }
        });
    }

    @Override
    public CompletableFuture<String> getServerById(String hexId) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return jedis.get(PLAYER_SERVER_KEY_PREFIX + hexId);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setServerById(String hexId, String serverName) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                Response<String> cachedServerName = null;
                try (Pipeline pipeline = jedis.pipelined()) {
                    if (serverName != null) {
                        pipeline.set(PLAYER_SERVER_KEY_PREFIX + hexId, serverName);
                        pipeline.sadd(ONLINE_PLAYERS_SERVER_KEY_PREFIX + serverName, hexId);
                    } else {
                        cachedServerName = pipeline.get(PLAYER_SERVER_KEY_PREFIX + hexId);
                    }
                    pipeline.sync();
                }
                if (cachedServerName != null) {
                    jedis.srem(ONLINE_PLAYERS_SERVER_KEY_PREFIX + cachedServerName.get(), hexId);
                }
            }
        });
    }

    @Override
    public CompletableFuture<String> getGroupById(String hexId) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return jedis.get(PLAYER_GROUP_KEY_PREFIX + hexId);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setGroupById(String hexId, String groupName) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                Response<String> cachedGroupName = null;
                try (Pipeline pipeline = jedis.pipelined()) {
                    if (groupName != null) {
                        pipeline.set(PLAYER_GROUP_KEY_PREFIX + hexId, groupName);
                        pipeline.sadd(ONLINE_PLAYERS_GROUP_KEY_PREFIX + groupName, hexId);
                    } else {
                        cachedGroupName = pipeline.get(PLAYER_GROUP_KEY_PREFIX + hexId);
                    }
                    pipeline.sync();
                }
                if (cachedGroupName != null) {
                    jedis.srem(ONLINE_PLAYERS_GROUP_KEY_PREFIX + cachedGroupName.get(), hexId);
                }
            }
        });
    }
}
