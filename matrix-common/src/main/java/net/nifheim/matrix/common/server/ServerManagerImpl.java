package net.nifheim.matrix.common.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.config.sub.ServerInfoConfiguration;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.common.util.RedisManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Jaime Suárez
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
     *     <li>gamemode</li>
     *     <li>servertype</li>
     *     <li>heartbeat</li>
     * </ul>
     */
    public static final String SERVER_INFO_KEY_PREFIX = "matrix:serverinfo:"; // hash
    public static final String SERVER_HEARTBEAT_KEY_PREFIX = "matrix:serverheartbeat:"; // value
    private final SchedulerAdapter schedulerAdapter;
    private final RedisManager redisManager;
    private final ServerInfo serverInfo;
    private final Logger logger;
    private final ServerInfoFactoryImpl serverInfoFactory;

    public ServerManagerImpl(SchedulerAdapter schedulerAdapter, RedisManager redisManager, ServerInfoConfiguration configuration, ServerPlatformInfo platformInfo, Logger logger) {
        this.schedulerAdapter = schedulerAdapter;
        this.redisManager = redisManager;
        this.logger = logger;
        this.serverInfoFactory = new ServerInfoFactoryImpl(logger, this);
        this.serverInfo = serverInfoFactory.createServerInfo(configuration, platformInfo);
    }

    @Override
    public @NotNull CompletableFuture<Map<String, Set<ServerInfo>>> getAllServers() {
        return schedulerAdapter.makeFuture(this::getAllServersSync);
    }

    @Override
    public @NotNull CompletableFuture<Set<String>> getServerNames(@NotNull String groupName) {
        return schedulerAdapter.makeFuture(() -> {
            logger.debug("Getting servers on group " + groupName);
            return getServerNamesSync(groupName);
        });
    }

    public @NotNull Set<String> getServerNamesSync(@NotNull String groupName) {
        Set<String> servers = new HashSet<>();
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.smembers(SERVER_GROUP_KEY_PREFIX + groupName);
        } catch (JedisException ex) {
            logger.error("An error has occurred getting servers for group {} from cache.", groupName);
            logger.error("An error has occurred getting all servers for a group from cache.", ex);
        }
        return servers;

    }


    @Override
    public @NotNull CompletableFuture<Optional<ServerInfo>> getServer(@NotNull String name) {
        return schedulerAdapter.makeFuture(() -> {
            try (Jedis jedis = redisManager.getResource()) {
                return Optional.ofNullable(getServer(name, jedis));
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> addServer(@NotNull ServerInfo serverInfo) {
        return addServers(new ServerInfo[]{serverInfo});
    }

    @Override
    public @NotNull CompletableFuture<Void> addServers(ServerInfo @NotNull [] serverInfos) {
        return schedulerAdapter.makeFuture(() -> {
            try (Jedis jedis = redisManager.getResource(); Pipeline pipeline = jedis.pipelined()) {
                for (ServerInfo serverInfo : serverInfos) {
                    // group     : string
                    // gametype  : string
                    // gamemode  : string
                    // heartbeat : long
                    if (serverInfo.getGroupName().trim().isEmpty()) { // skip empty groups
                        continue;
                    }
                    pipeline.sadd(SERVER_GROUPS_KEY, serverInfo.getGroupName());
                    pipeline.sadd(SERVER_GROUP_KEY_PREFIX + serverInfo.getGroupName(), serverInfo.getName());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "groupName", serverInfo.getGroupName());
                    if (serverInfo.getRawName() != null) {
                        pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "rawServerName", serverInfo.getRawName());
                    } else {
                        pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "serverNumber", String.valueOf(serverInfo.getServerNumber()));
                    }
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "serverType", serverInfo.getServerType().name());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "gameMode", serverInfo.getDefaultGameMode().name());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "address", serverInfo.getAddress());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "port", String.valueOf(serverInfo.getPort()));
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getName(), "maxPlayers", String.valueOf(serverInfo.getMaxPlayers()));
                }
                pipeline.sync();
                checkServerGroups(jedis);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> removeServer(@NotNull ServerInfo serverInfo) {
        return schedulerAdapter.makeFuture(() -> {
            try (Jedis jedis = redisManager.getResource()) {
                removeServer(serverInfo.getGroupName(), serverInfo.getName(), jedis);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> removeServer() {
        return removeServer(serverInfo);
    }

    @Override
    public @NotNull CompletableFuture<Void> heartbeat() throws IllegalStateException {
        return schedulerAdapter.makeFuture(() -> {
            try (Jedis jedis = redisManager.getResource(); Pipeline pipeline = jedis.pipelined()) {
                pipeline.sadd(SERVER_GROUP_KEY_PREFIX + serverInfo.getGroupName(), serverInfo.getName());
                pipeline.setex(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getName(), TimeUnit.MINUTES.toSeconds(15), String.valueOf(System.currentTimeMillis()));
                pipeline.sync();
            } catch (JedisException ex) {
                logger.error("An error has occurred sending heartbeat to cache.", ex);
            }
        });
    }

    public Map<String, Set<ServerInfo>> getAllServersSync() {
        Map<String, Set<ServerInfo>> servers = new HashMap<>();
        try (Jedis jedis = redisManager.getResource()) {
            Map<String, Set<String>> invalid = new HashMap<>();
            Set<String> groups = jedis.smembers(SERVER_GROUPS_KEY);
            if (groups == null) {
                logger.info("Null groups");
                return servers;
            }
            for (String group : groups) {
                servers.put(group, new HashSet<>());
                Set<String> serverNames = jedis.smembers(SERVER_GROUP_KEY_PREFIX + group);
                for (String serverName : serverNames) {
                    ServerInfo serverInfo = getServer(serverName, jedis);
                    if (serverInfo == null) {
                        logger.warn("A server info with the name: '{}' can't be found on key '{}' but is on set '{}', it will be removed.", serverName, SERVER_INFO_KEY_PREFIX + serverName, SERVER_GROUP_KEY_PREFIX + group);
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
            logger.error("An error has occurred getting all servers from cache.", ex);
        }
        return servers;
    }

    public OptionalLong getLastHeartbeatSync(ServerInfo serverInfo) {
        try (Jedis jedis = redisManager.getResource()) {
            String longString = jedis.get(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getName());
            if (longString == null) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(Long.parseLong(longString));
        } catch (NumberFormatException ex) {
            logger.warn("Error getting heartbeat for: {}", serverInfo.getName());
            try (Jedis jedis = redisManager.getResource()) {
                jedis.hgetAll(SERVER_INFO_KEY_PREFIX + serverInfo.getName()).forEach((k, v) -> logger.info(k + ":" + v));
            }
            logger.error("Error getting heartbeat for: " + serverInfo.getName(), ex);
        }
        return OptionalLong.empty();
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
        logger.warn("Added server {} to remove pipeline", name);
    }

    @Override
    public @NotNull CompletableFuture<OptionalLong> getLastHeartbeat(@NotNull ServerInfo serverInfo) {
        return schedulerAdapter.makeFuture(() -> getLastHeartbeatSync(serverInfo));
    }

    @Override
    public @NotNull CompletableFuture<Set<String>> getGroupsNames() {
        return schedulerAdapter.makeFuture(() -> {
            try (Jedis jedis = redisManager.getResource()) {
                return jedis.smembers(SERVER_GROUPS_KEY);
            }
        });
    }

    @Override
    public @NotNull String getLobbyForGroup(@NotNull String groupName) {
        logger.debug("Finding lobby for " + groupName);
        try (Jedis jedis = redisManager.getResource()) {
            Set<String> serverNames = jedis.smembers(SERVER_GROUP_KEY_PREFIX + groupName);
            for (String serverName : serverNames) {
                if (serverName.matches(groupName + ":lobby\\d?")) {
                    return serverName;
                }
            }
        } catch (JedisException ex) {
            logger.error("An error has occurred getting servers for group {} from cache.", groupName);
            logger.error("An error has occurred getting lobby servers from cache.", ex);
        }
        return "lobby1";
    }

    @Override
    public @NotNull CompletableFuture<@Nullable String> getLobbyServerName(ServerInfo serverInfo) {
        return schedulerAdapter.makeFuture(() -> {
            try (Jedis jedis = redisManager.getResource()) {
                // TODO: search servers in group with lobby type or fallback to default lobby
                return jedis.hget(SERVER_INFO_KEY_PREFIX + serverInfo.getRawName(), "lobby");
            }
        });
    }

    public boolean isServerRegisteredInGroupSync(String group, String serverName) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.sismember(SERVER_GROUP_KEY_PREFIX + group, serverName);
        }
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
                    logger.info("Removed server " + server + " from group " + entry.getKey());
                }
            }
            pipeline.sync();
        }
    }

    public Optional<ServerInfo> getServerSync(@NotNull String name) {
        try (Jedis jedis = redisManager.getResource()) {
            return Optional.ofNullable(getServer(name, jedis));
        }
    }

    private @Nullable ServerInfo getServer(@NotNull String name, @NotNull Jedis jedis) {
        try {
            Map<String, String> data = jedis.hgetAll(SERVER_INFO_KEY_PREFIX + name);
            if (data != null && !data.isEmpty()) {
                return serverInfoFactory.createServerInfo(data);
            } else {
                logger.error("Server with name {} not found or empty.", name);
            }
        } catch (@NotNull JedisException ex) {
            logger.error("An error has occurred getting server with name " + name + " from cache.", ex);
        } catch (IllegalArgumentException | NullPointerException ex) {
            logger.warn("An error has occurred getting server with name {} from cache.", name);
        }
        return null;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public ServerInfoFactoryImpl getServerInfoFactory() {
        return serverInfoFactory;
    }
}
