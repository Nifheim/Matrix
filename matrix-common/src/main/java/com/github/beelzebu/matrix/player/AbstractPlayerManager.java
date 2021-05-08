package com.github.beelzebu.matrix.player;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerManager;
import com.github.beelzebu.matrix.util.MetaInjector;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

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
    private final MatrixAPIImpl api;
    private final MetaInjector<P> metaInjector;

    public AbstractPlayerManager(MatrixAPIImpl api, MetaInjector<P> metaInjector) {
        this.api = api;
        this.metaInjector = metaInjector;
    }

    public final MetaInjector<P> getMetaInjector() {
        return metaInjector;
    }

    protected abstract @Nullable P getPlatformPlayer(UUID uniqueId);

    protected abstract @Nullable P getPlatformPlayerByName(String name);

    protected abstract @Nullable P getPlatformPlayerById(String hexId);

    @Override
    public final @NotNull CompletableFuture<MatrixPlayer> getPlayer(@NotNull P player) {
        String hexId = getMetaInjector().getId(player);
        if (hexId != null) {
            return getPlayerById(hexId);
        }
        return getPlayer(getUniqueId(player));
    }

    @Override
    public final @NotNull CompletableFuture<MatrixPlayer> getPlayerById(@NotNull String hexId) {
        return api.getDatabase().getPlayerById(hexId);
    }

    @Override
    public final @NotNull CompletableFuture<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        return api.getDatabase().getPlayer(uniqueId);
    }

    @Override
    public final @NotNull CompletableFuture<MatrixPlayer> getPlayerByName(@NotNull String name) {
        return api.getDatabase().getPlayerByName(name);
    }

    @Override
    public final @NotNull CompletableFuture<String> getHexId(@NotNull P player) {
        String hexId = getMetaInjector().getId(player);
        if (hexId != null) {
            return CompletableFuture.completedFuture(hexId);
        }
        UUID uniqueId = getUniqueId(player);
        return getHexId(uniqueId);
    }

    @Override
    public final @NotNull CompletableFuture<String> getHexId(@NotNull UUID uniqueId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getHexId(uniqueId).orElse(api.getDatabase().getStorage().getPlayer(uniqueId).getId()));
    }

    @Override
    public final @NotNull CompletableFuture<String> getHexIdByName(@NotNull String name) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getHexIdByName(name).orElse(api.getDatabase().getStorage().getPlayerByName(name).getId()));
    }

    @Override
    public final @NotNull CompletableFuture<UUID> getUniqueIdById(@NotNull String hexId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getUniqueIdById(hexId).orElse(api.getDatabase().getStorage().getPlayerById(hexId).getUniqueId()));
    }

    @Override
    public final @NotNull CompletableFuture<UUID> getUniqueIdByName(@NotNull String name) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getUniqueIdByName(name).orElse(api.getDatabase().getStorage().getPlayerByName(name).getUniqueId()));
    }

    @Override
    public final @NotNull CompletableFuture<String> getNameById(@NotNull String hexId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getNameById(hexId).orElse(api.getDatabase().getStorage().getPlayerById(hexId).getName()));
    }

    @Override
    public final @NotNull CompletableFuture<String> getName(@NotNull UUID uniqueId) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> api.getDatabase().getCacheProvider().getName(uniqueId).orElse(api.getDatabase().getStorage().getPlayer(uniqueId).getName()));
    }

    @Override
    public final @NotNull CompletableFuture<Set<UUID>> getOnlinePlayers() {
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
    public final @NotNull CompletableFuture<Set<UUID>> getOnlinePlayersInServer(String server) {
        Objects.requireNonNull(server, "Server can't be null");
        return getPlayers(server, ONLINE_PLAYERS_SERVER_KEY_PREFIX);
    }

    @Override
    public final @NotNull CompletableFuture<Set<UUID>> getOnlinePlayersInGroup(String group) {
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
    public final @NotNull CompletableFuture<Integer> getOnlinePlayerCount() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(this::getOnlinePlayerCountSync);
    }

    public int getOnlinePlayerCountSync() {
        try (Jedis jedis = api.getRedisManager().getResource()) {
            return Math.toIntExact(jedis.scard(ONLINE_PLAYERS_TOTAL_KEY));
        }
    }

    @Override
    public final @NotNull CompletableFuture<Integer> getOnlinePlayerCountInServer(String server) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return Math.toIntExact(jedis.scard(ONLINE_PLAYERS_SERVER_KEY_PREFIX + server));
            }
        });
    }

    @Override
    public final @NotNull CompletableFuture<Integer> getOnlinePlayerCountInGroup(String group) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return Math.toIntExact(jedis.scard(ONLINE_PLAYERS_GROUP_KEY_PREFIX + group));
            }
        });
    }

    @Override
    public final @NotNull CompletableFuture<Boolean> isOnline(@NotNull P player, @Nullable String serverGroup, @Nullable String serverName) {
        String playerServerGroup = getMetaInjector().getServerGroup(player);
        if (playerServerGroup == null) {
            throw new IllegalStateException("Server group meta not injected yet.");
        }
        String playerServerName = getMetaInjector().getServerName(player);
        if (playerServerName == null) {
            throw new IllegalStateException("Server server meta not injected yet.");
        }
        if (serverGroup == null) {
            return CompletableFuture.completedFuture(true);
        } else if (serverName == null) {
            return CompletableFuture.completedFuture(Objects.equals(serverGroup, playerServerGroup));
        } else {
            return CompletableFuture.completedFuture(Objects.equals(serverGroup, playerServerGroup)
                    && Objects.equals(serverName, playerServerName));
        }
    }

    @Override
    public final @NotNull CompletableFuture<Boolean> isOnline(@Nullable UUID uniqueId, @Nullable String groupName, @Nullable String serverName) {
        if (uniqueId == null) {
            return CompletableFuture.completedFuture(false);
        }
        try {
            P player = getPlatformPlayer(uniqueId);
            if (player != null) {
                return isOnline(player, groupName, serverName);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
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
            return isOnline(hexId, groupName, serverName);
        });
    }

    @Override
    public final @NotNull CompletableFuture<Boolean> isOnlineByName(@Nullable String name, @Nullable String groupName, @Nullable String serverName) {
        if (name == null) {
            return CompletableFuture.completedFuture(false);
        }
        try {
            P player = getPlatformPlayerByName(name);
            if (player != null) {
                return isOnline(player, groupName, serverName);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
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
            return isOnline(hexId, groupName, serverName);
        });
    }

    @Override
    public final @NotNull CompletableFuture<Boolean> isOnlineById(@Nullable String hexId, @Nullable String groupName, @Nullable String serverName) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(false);
        }
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> isOnline(hexId, groupName, serverName));
    }

    private boolean isOnline(@NotNull String hexId, @Nullable String groupName, @Nullable String serverName) {
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
    }

    @Override
    public final @NotNull CompletableFuture<Void> setOnline(@NotNull P player) {
        return setOnlineById(getMetaInjector().getId(player));
    }

    @Override
    public final @NotNull CompletableFuture<Void> setOnlineById(@Nullable String hexId) {
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
    public final @NotNull CompletableFuture<Void> setOffline(@NotNull P player) {
        return setOfflineById(getMetaInjector().getId(player));
    }

    @Override
    public final @NotNull CompletableFuture<Void> setOfflineById(String hexId) {
        Objects.requireNonNull(hexId);
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                String serverName = jedis.get(PLAYER_SERVER_KEY_PREFIX + hexId);
                String serverGroup = jedis.get(PLAYER_GROUP_KEY_PREFIX + hexId);
                if (serverName == null || serverGroup == null) {
                    Matrix.getLogger().info("id: " + hexId);
                    Matrix.getLogger().info("name: " + serverName);
                    Matrix.getLogger().info("group: " + serverGroup);
                }
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
    public final @NotNull CompletableFuture<String> getServer(@NotNull P player) {
        String serverName = getMetaInjector().getServerName(player);
        if (serverName != null) {
            return CompletableFuture.completedFuture(serverName);
        }
        return getServerById(getMetaInjector().getId(player));
    }

    @Override
    public final @NotNull CompletableFuture<String> getServerById(@Nullable String hexId) {
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
    public final @NotNull CompletableFuture<Void> setServer(@NotNull P player, String serverName) {
        String hexId = getMetaInjector().getId(player);
        if (hexId == null) {
            throw new IllegalStateException("HexID not injected yet.");
        }
        return setServerById(hexId, serverName);
    }

    @Override
    public final @NotNull CompletableFuture<Void> setServerById(@Nullable String hexId, String serverName) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        P player = getPlatformPlayerById(hexId);
        if (player != null) {
            getMetaInjector().setMeta(player, MetaInjector.SERVER_NAME_KEY, serverName);
        }
        return updateOnline(hexId, serverName, PLAYER_SERVER_KEY_PREFIX, ONLINE_PLAYERS_SERVER_KEY_PREFIX);
    }

    @Override
    public final @NotNull CompletableFuture<String> getGroup(@NotNull P player) {
        String serverGroup = getMetaInjector().getServerGroup(player);
        if (serverGroup != null) {
            return CompletableFuture.completedFuture(serverGroup);
        }
        return getServerById(getMetaInjector().getId(player));
    }

    @Override
    public final @NotNull CompletableFuture<String> getGroupById(@Nullable String hexId) {
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
    public final @NotNull CompletableFuture<Void> setGroup(@NotNull P player, String groupName) {
        String hexId = getMetaInjector().getId(player);
        if (hexId == null) {
            throw new IllegalStateException("HexID not injected yet.");
        }
        return setGroupById(hexId, groupName);
    }

    @Override
    public final @NotNull CompletableFuture<Void> setGroupById(@Nullable String hexId, String groupName) {
        if (hexId == null) {
            return CompletableFuture.completedFuture(null);
        }
        P player = getPlatformPlayerById(hexId);
        if (player != null) {
            getMetaInjector().setMeta(player, MetaInjector.SERVER_GROUP_KEY, groupName);
        }
        return updateOnline(hexId, groupName, PLAYER_GROUP_KEY_PREFIX, ONLINE_PLAYERS_GROUP_KEY_PREFIX);
    }

    private @NotNull CompletableFuture<Void> updateOnline(String hexId, @Nullable String where, String playerKeyPrefix, String onlinePlayersKeyPrefix) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                String cachedGroupName = jedis.get(playerKeyPrefix + hexId);
                if (where != null && !Objects.equals(cachedGroupName, where)) {
                    try (Pipeline pipeline = jedis.pipelined()) {
                        pipeline.set(playerKeyPrefix + hexId, where);
                        pipeline.sadd(onlinePlayersKeyPrefix + where, hexId);
                        pipeline.sync();
                    }
                }
                if (cachedGroupName != null) {
                    jedis.srem(onlinePlayersKeyPrefix + cachedGroupName, hexId);
                }
            }
        });
    }
}
