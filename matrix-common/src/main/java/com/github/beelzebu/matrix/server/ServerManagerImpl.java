package com.github.beelzebu.matrix.server;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerManager;
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
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Set<ServerInfo>> servers = new HashMap<>();
            try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                String cursor = ScanParams.SCAN_POINTER_START;
                ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
                do {
                    for (String serverKey : scan.getResult()) {
                        try {
                            String serverName = serverKey.replaceFirst(SERVER_INFO_KEY_PREFIX, "");
                            ServerInfo serverInfo = new ServerInfoImpl(serverName, jedis.hgetAll(serverKey));
                            Set<ServerInfo> group = servers.getOrDefault(serverInfo.getGroupName(), new HashSet<>());
                            group.add(serverInfo);
                            servers.put(serverName, group);
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
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<Set<ServerInfo>> getServers(String groupName) {
        return CompletableFuture.supplyAsync(() -> {
            Set<ServerInfo> servers = new HashSet<>();
            try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                String cursor = ScanParams.SCAN_POINTER_START;
                ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
                do {
                    for (String serverKey : scan.getResult()) {
                        try {
                            String serverName = serverKey.replaceFirst(SERVER_INFO_KEY_PREFIX, "");
                            ServerInfo serverInfo = new ServerInfoImpl(serverName, jedis.hgetAll(serverKey));
                            if (Objects.equals(groupName, serverInfo.getGroupName())) {
                                servers.add(serverInfo);
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                    scan = jedis.scan(cursor, new ScanParams().match(SERVER_INFO_KEY_PREFIX + "*").count(Integer.MAX_VALUE));
                } while (!Objects.equals(cursor = scan.getCursor(), ScanParams.SCAN_POINTER_START));
            } catch (JedisException ex) {
                Matrix.getLogger().log("An error has occurred getting servers for group " + groupName + " from cache.");
                Matrix.getLogger().debug(ex);
            }
            return servers;
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<Optional<ServerInfo>> getServer(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                ServerInfo serverInfo = new ServerInfoImpl(name, jedis.hgetAll(SERVER_INFO_KEY_PREFIX + name));
                return Optional.of(serverInfo);
            } catch (JedisException | NullPointerException | IllegalArgumentException ex) {
                Matrix.getLogger().log("An error has occurred getting server with name " + name + "  from cache.");
                Matrix.getLogger().debug(ex);
            }
            return Optional.empty();
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<Void> addServer(ServerInfo serverInfo) {
        return addServers(new ServerInfo[]{serverInfo});
    }

    @Override
    public CompletableFuture<Void> addServers(ServerInfo[] serverInfos) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = api.getRedisManager().getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
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
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<Void> removeServer(ServerInfo serverInfo) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = api.getRedisManager().getPool().getResource(); Pipeline pipeline = jedis.pipelined()) {
                pipeline.srem(SERVER_GROUP_KEY_PREFIX + serverInfo.getGroupName(), serverInfo.getServerName());
                pipeline.del(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName());
                pipeline.del(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName());
                pipeline.sync();
                checkServerGroups(jedis);
            }
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<Void> heartbeat(ServerInfo serverInfo) {
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                jedis.set(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName(), String.valueOf(System.currentTimeMillis()));
            }
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<OptionalLong> getLastHeartbeat(ServerInfo serverInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                String longString = jedis.get(SERVER_HEARTBEAT_KEY_PREFIX + serverInfo.getServerName());
                if (longString == null) {
                    return OptionalLong.empty();
                }
                return OptionalLong.of(Long.parseLong(longString));
            } catch (NumberFormatException e) {
                Matrix.getLogger().info("Error getting heartbeat for: " + serverInfo.getServerName());
                try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                    jedis.hgetAll(SERVER_INFO_KEY_PREFIX + serverInfo.getServerName()).forEach((k, v) -> Matrix.getLogger().info(k + ":" + v));
                }
                Matrix.getLogger().debug(e);
            }
            return OptionalLong.empty();
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    @Override
    public CompletableFuture<Set<String>> getGroupsNames() {
        return CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = api.getRedisManager().getPool().getResource()) {
                return jedis.smembers(SERVER_GROUPS_KEY);
            }
        }, api.getPlugin().getBootstrap().getScheduler().async());
    }

    private void checkServerGroups(Jedis jedis) {
        Set<String> deadGroups = new HashSet<>();
        Map<String, Set<String>> deadServers = new HashMap<>();
        for (String groupName : jedis.smembers(SERVER_GROUPS_KEY)) {
            Set<String> servers = jedis.smembers(SERVER_GROUPS_KEY + groupName);
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
}
