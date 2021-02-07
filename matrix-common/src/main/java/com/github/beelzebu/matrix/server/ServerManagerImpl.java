package com.github.beelzebu.matrix.server;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerManager;
import com.github.beelzebu.matrix.api.server.ServerType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
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
    public static final String SERVER_GROUPS_KEY = "matrix:servergroup"; // set
    public static final String SERVER_GROUP_KEY_PREFIX = "matrix:servergroup:"; // set
    public static final String SERVER_INFO_KEY_PREFIX = "matrix:serverinfo:"; // hash
    public static final String SERVER_HEARTBEAT_KEY_PREFIX = "matrix:serverheartbeat:"; // value
    private final MatrixAPIImpl<?> api;

    public ServerManagerImpl(MatrixAPIImpl<?> api) {
        this.api = api;
    }

    @Override
    public CompletableFuture<Map<String, Set<ServerInfo>>> getAllServers() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Map<String, Set<ServerInfo>> servers = new HashMap<>();
            try (Jedis jedis = api.getRedisManager().getResource()) {
                Set<String> groups = jedis.smembers(SERVER_GROUPS_KEY);
                for (String group : groups) {
                    servers.put(group, new HashSet<>());
                    Set<String> serverNames = jedis.smembers(SERVER_GROUP_KEY_PREFIX + group);
                    for (String serverName : serverNames) {
                        servers.get(group).add(getServer(serverName, jedis));
                    }
                }
            } catch (JedisException ex) {
                Matrix.getLogger().log("An error has occurred getting all servers from cache.");
                Matrix.getLogger().debug(ex);
            }
            return servers;
        });
    }

    @Override
    public CompletableFuture<Set<ServerInfo>> getServers(String groupName) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Matrix.getLogger().debug("Getting servers on group " + groupName);// TODO: remove debug
            Set<ServerInfo> servers = new HashSet<>();
            try (Jedis jedis = api.getRedisManager().getResource()) {
                String cursor = ScanParams.SCAN_POINTER_START;
                ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + groupName + "*").count(100));
                int iterations = 0;
                do {
                    iterations++;
                    Matrix.getLogger().debug("Iteration: " + iterations);
                    for (String serverKey : scan.getResult()) {
                        Matrix.getLogger().debug("Result: " + serverKey);
                        try {
                            String serverName = serverKey.replaceFirst(SERVER_INFO_KEY_PREFIX, "");
                            ServerInfo serverInfo = new ServerInfoImpl(serverName, jedis.hgetAll(serverKey));
                            servers.add(serverInfo);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                    scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + groupName + "*").count(100));
                } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
            } catch (JedisException ex) {
                Matrix.getLogger().log("An error has occurred getting servers for group " + groupName + " from cache.");
                Matrix.getLogger().debug(ex);
            }
            return servers;
        });
    }

    @Override
    public CompletableFuture<Optional<ServerInfo>> getServer(String name) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return Optional.of(getServer(name, jedis));
            }
        });
    }

    public ServerInfo getServer(String name, Jedis jedis) {
        try {
            return new ServerInfoImpl(name, jedis.hgetAll(SERVER_INFO_KEY_PREFIX + name));
        } catch (JedisException | NullPointerException | IllegalArgumentException ex) {
            Matrix.getLogger().log("An error has occurred getting server with name " + name + "  from cache.");
            Matrix.getLogger().debug(ex);
        }
        return null;
    }

    @Override
    public CompletableFuture<Void> addServer(ServerInfo serverInfo) {
        return addServers(new ServerInfo[]{serverInfo});
    }

    @Override
    public CompletableFuture<Void> addServers(ServerInfo[] serverInfos) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource(); Pipeline pipeline = jedis.pipelined()) {
                for (ServerInfo serverInfo : serverInfos) {
                    // group     : string
                    // gametype  : string
                    // gamemode  : string
                    // heartbeat : long
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "group", serverInfo.getGroupName());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "gamemode", serverInfo.getDefaultGameMode().toString());
                    pipeline.hset(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName(), "servertype", serverInfo.getServerType().name());
                }
                pipeline.sync();
                checkServerGroups(jedis);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeServer(ServerInfo serverInfo) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource(); Pipeline pipeline = jedis.pipelined()) {
                pipeline.srem(SERVER_GROUP_KEY_PREFIX + serverInfo.getGroupName(), serverInfo.getServerName());
                pipeline.del(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName());
                pipeline.del(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName());
                pipeline.sync();
                checkServerGroups(jedis);
            }
        });
    }

    @Override
    public CompletableFuture<Void> heartbeat(ServerInfo serverInfo) {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            Matrix.getLogger().info("Sending heartbeat");
            try (Jedis jedis = api.getRedisManager().getResource()) {
                jedis.set(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName(), String.valueOf(System.currentTimeMillis()));
                Matrix.getLogger().info("Heartbeat sent");
            }
        });
    }

    @Override
    public CompletableFuture<OptionalLong> getLastHeartbeat(ServerInfo serverInfo) {
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
    public CompletableFuture<Set<String>> getGroupsNames() {
        return api.getPlugin().getBootstrap().getScheduler().makeFuture(() -> {
            try (Jedis jedis = api.getRedisManager().getResource()) {
                return jedis.smembers(SERVER_GROUPS_KEY);
            }
        });
    }

    private void checkServerGroups(Jedis jedis) {
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
                }
            }
            pipeline.sync();
        }
    }

    @Override
    public String getLobbyForGroup(String groupName) {
        Matrix.getLogger().debug("Finding lobby for " + groupName);
        try (Jedis jedis = api.getRedisManager().getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + groupName + "*lobby*").count(100));
            int iterations = 0;
            do {
                iterations++;
                Matrix.getLogger().debug("Iteration: " + iterations);
                for (String serverKey : scan.getResult()) {
                    Matrix.getLogger().debug("Result: " + serverKey);
                    try {
                        String serverName = serverKey.replaceFirst(SERVER_INFO_KEY_PREFIX, "");
                        ServerInfo serverInfo = new ServerInfoImpl(serverName, jedis.hgetAll(serverKey));
                        if (serverInfo.getServerType() == ServerType.LOBBY) {
                            return serverName;
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + groupName + "*lobby*").count(100));
            } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
        } catch (JedisException ex) {
            Matrix.getLogger().log("An error has occurred getting servers for group " + groupName + " from cache.");
            Matrix.getLogger().debug(ex);
        }
        return null;
    }
}
