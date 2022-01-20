package com.github.beelzebu.matrix.server;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerManager;
import com.github.beelzebu.matrix.util.FinalCachedValue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class ServerManagerImpl implements ServerManager {

    // (group:)[name/servertype][n]
    // group     : string
    // gamemode  : string
    // servertype
    // heartbeat : long
    /**
     * Stores all registered server groups
     */
    public static final String SERVER_GROUPS_KEY = "matrix:servergroup"; // set
    /**
     * Keys using this prefix are sets which contains all keys (without prefix) for the servers on the group
     */
    public static final String SERVER_GROUP_KEY_PREFIX = "matrix:servergroup:"; // set
    /**
     * Keys using this prefix are hashes which store all info for the server, including but not limited to:
     * <ul>
     *     <li>group</li>
     *     <li>gameemode</li>
     *     <li>servertype</li>
     *     <li>heartbeat</li>
     * </ul>
     */
    public static final String SERVER_INFO_KEY_PREFIX = "matrix:serverinfo:"; // hash
    public static final String SERVER_HEARTBEAT_KEY_PREFIX = "matrix:serverheartbeat:"; // value
    private final MatrixAPIImpl api;

    public ServerManagerImpl(MatrixAPIImpl api) {
        this.api = api;
    }

    @Override
    public @NotNull CompletableFuture<Map<String, Set<ServerInfo>>> getAllServers() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Map<String, Set<ServerInfo>> servers = new HashMap<>();
            try (Jedis jedis = api.getRedisManager().getResource()) {
                Map<String, Set<String>> invalid = new HashMap<>();
                Set<String> groups = jedis.smembers(SERVER_GROUPS_KEY);
                if (groups == null) {
                    Matrix.getLogger().info("Null groups");
                    return servers;
                }
                for (String group : groups) {
                    servers.put(group, new HashSet<>());
                    Set<String> serverNames = jedis.smembers(SERVER_GROUP_KEY_PREFIX + group);
                    for (String serverName : serverNames) {
                        ServerInfo serverInfo = getServer(serverName, jedis);
                        if (serverInfo == null) {
                            Matrix.getLogger().debug("A server with the name: " + serverName + " can't be found but is on set " + SERVER_GROUP_KEY_PREFIX + group);
                            Set<String> invalidOnGroup = invalid.computeIfAbsent(group, k -> new HashSet<>());
                            invalidOnGroup.add(serverName);
                            invalid.put(group, invalidOnGroup);
                            continue;
                        }
                        servers.get(group).add(serverInfo);
                    }
                }
                removeServers(invalid, jedis);
            } catch (JedisException ex) {
                Matrix.getLogger().log("An error has occurred getting all servers from cache.");
                Matrix.getLogger().debug(ex);
            }
            return servers;
        });
    }

    @Override
    public @NotNull CompletableFuture<Set<ServerInfo>> getServers(String groupName) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Matrix.getLogger().debug("Getting servers on group " + groupName);
            Set<ServerInfo> servers = new HashSet<>();
            try (Jedis jedis = api.getRedisManager().getResource()) {
                for (String serverName : jedis.smembers(SERVER_GROUP_KEY_PREFIX + groupName)) {
                    try {
                        ServerInfo serverInfo = new ServerInfoImpl(serverName, jedis.hgetAll(SERVER_INFO_KEY_PREFIX + serverName));
                        Matrix.getLogger().debug("  Server " + serverInfo.getServerName() + " is on group " + groupName);
                        servers.add(serverInfo);
                    } catch (IllegalArgumentException | NullPointerException e) {
                        throw new JedisException("Error reading data for " + serverName, e);
                    }
                }
            } catch (JedisException ex) {
                Matrix.getLogger().log("An error has occurred getting servers for group " + groupName + " from cache.");
                Matrix.getLogger().debug(ex);
            }
            return servers;
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<ServerInfo>> getServer(@NotNull String name) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return Optional.ofNullable(getServer(name, jedis));
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> addServer(ServerInfo serverInfo) {
        return addServers(new ServerInfo[]{serverInfo});
    }

    @Override
    public @NotNull CompletableFuture<Void> addServers(ServerInfo @NotNull [] serverInfos) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource(); Pipeline pipeline = jedis.pipelined()) {
                for (ServerInfo serverInfo : serverInfos) {
                    // group     : string
                    // gametype  : string
                    // gamemode  : string
                    // heartbeat : long
                    if (serverInfo.getGroupName().trim().isEmpty()) { // skip empty groups
                        continue;
                    }
                    pipeline.sadd(SERVER_GROUPS_KEY, serverInfo.getGroupName());
                    pipeline.sadd(SERVER_GROUP_KEY_PREFIX + serverInfo.getGroupName(), serverInfo.getServerName());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "group", serverInfo.getGroupName());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "gamemode", serverInfo.getDefaultGameMode().toString());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "servertype", serverInfo.getServerType().name());
                    if (((ServerInfoImpl) serverInfo).getCachedLobby() instanceof FinalCachedValue) {
                        pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "lobby", serverInfo.getLobbyServer().join());
                    }
                }
                pipeline.sync();
                checkServerGroups(jedis);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> removeServer(@NotNull ServerInfo serverInfo) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                removeServer(serverInfo.getGroupName(), serverInfo.getServerName(), jedis);
            }
        });
    }

    private void removeServer(@NotNull String group, @NotNull String name, Jedis jedis) {
        try (Pipeline pipeline = jedis.pipelined()) {
            pipelineRemove(pipeline, group, name);
            pipeline.sync();
            checkServerGroups(jedis);
        }
    }

    private void removeServers(@NotNull Map<String, Set<String>> servers, Jedis jedis) {
        try (Pipeline pipeline = jedis.pipelined()) {
            for (Map.Entry<String, Set<String>> entry : servers.entrySet()) {
                String group = entry.getKey();
                for (String name : entry.getValue()) {
                    pipelineRemove(pipeline, group, name);
                }
            }
            pipeline.sync();
            checkServerGroups(jedis);
        }
    }

    private void pipelineRemove(Pipeline pipeline, String group, String name) {
        pipeline.srem(SERVER_GROUP_KEY_PREFIX + group, name);
        pipeline.del(SERVER_HEARTBEAT_KEY_PREFIX + name);
        pipeline.del(SERVER_INFO_KEY_PREFIX + name);
    }

    @Override
    public @NotNull CompletableFuture<Void> heartbeat(@NotNull ServerInfo serverInfo) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Matrix.getLogger().debug("Sending heartbeat");
            try (Jedis jedis = api.getRedisManager().getResource(); Pipeline pipeline = jedis.pipelined()) {
                pipeline.sadd(SERVER_GROUP_KEY_PREFIX + serverInfo.getGroupName(), serverInfo.getServerName());
                pipeline.setex(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName(), Math.toIntExact(TimeUnit.MINUTES.toMillis(15)), String.valueOf(System.currentTimeMillis()));
                pipeline.sync();
                Matrix.getLogger().debug("Heartbeat sent");
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<OptionalLong> getLastHeartbeat(@NotNull ServerInfo serverInfo) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                String longString = jedis.get(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName());
                if (longString == null) {
                    return OptionalLong.empty();
                }
                return OptionalLong.of(Long.parseLong(longString));
            } catch (NumberFormatException e) {
                Matrix.getLogger().info("Error getting heartbeat for: " + serverInfo.getServerName());
                try (Jedis jedis = api.getRedisManager().getResource()) {
                    jedis.hgetAll(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName()).forEach((k, v) -> Matrix.getLogger().info(k + ":" + v));
                }
                Matrix.getLogger().debug(e);
            }
            return OptionalLong.empty();
        });
    }

    @Override
    public @NotNull CompletableFuture<Set<String>> getGroupsNames() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return jedis.smembers(SERVER_GROUPS_KEY);
            }
        });
    }

    @Override
    public @NotNull String getLobbyForGroup(String groupName) {
        Matrix.getLogger().debug("Finding lobby for " + groupName);
        try (Jedis jedis = api.getRedisManager().getResource()) {
            Set<String> serverNames = jedis.smembers(SERVER_GROUP_KEY_PREFIX + groupName);
            for (String serverName : serverNames) {
                if (serverName.matches(groupName + ":lobby\\d?")) {
                    return serverName;
                }
            }
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting servers for group " + groupName + " from cache.");
            Matrix.getLogger().debug(ex);
        }
        return "lobby1";
    }

    private void checkServerGroups(@NotNull Jedis jedis) {
        Set<String> deadGroups = new HashSet<>();
        Map<String, Set<String>> deadServers = new HashMap<>();
        for (String groupName : jedis.smembers(SERVER_GROUPS_KEY)) {
            Set<String> servers = jedis.smembers(SERVER_GROUP_KEY_PREFIX + groupName);
            if (servers.isEmpty()) {
                deadGroups.add(groupName);
                return;
            }
            for (String server : servers) {
                if (!jedis.exists(SERVER_INFO_KEY_PREFIX + server)) {
                    Set<String> deadServersInGroup = deadServers.getOrDefault(groupName, new HashSet<>());
                    deadGroups.add(server);
                    deadServers.put(groupName, deadServersInGroup);
                }
            }
        }
        try (Pipeline pipeline = jedis.pipelined()) {
            pipeline.srem(SERVER_GROUPS_KEY, deadGroups.toArray(new String[0]));
            for (Map.Entry<String, Set<String>> entry : deadServers.entrySet()) {
                pipeline.srem(SERVER_GROUP_KEY_PREFIX + entry.getKey(), entry.getValue().toArray(new String[0]));
                for (String server : entry.getValue()) {
                    pipeline.del(SERVER_HEARTBEAT_KEY_PREFIX + server);
                    Matrix.getLogger().info("Removed server " + server + " from group " + entry.getKey());
                }
            }
            pipeline.sync();
        }
    }

    private @Nullable ServerInfo getServer(@NotNull String name, @NotNull Jedis jedis) {
        try {
            Map<String, String> data = jedis.hgetAll(SERVER_INFO_KEY_PREFIX + name);
            if (data != null && !data.isEmpty()) {
                return new ServerInfoImpl(name, data);
            }
        } catch (@NotNull JedisException | NullPointerException | IllegalArgumentException ex) {
            Matrix.getLogger().log("An error has occurred getting server with name " + name + "  from cache.");
            Matrix.getLogger().debug(ex);
        }
        return null;
    }

}
