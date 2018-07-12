package io.github.beelzebu.matrix.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.Statistics;
import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import io.github.beelzebu.matrix.utils.ServerType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisStorage {

    private final MatrixAPI core;
    @Getter
    private final JedisPool pool;
    @Getter
    private final PubSubListener pubSubListener;

    public RedisStorage(MatrixAPI core) {
        this.core = core;
        pool = new JedisPool(new JedisPoolConfig(), core.getConfig().getString("Redis.Host"), core.getConfig().getInt("Redis.Port"), 0, core.getConfig().getString("Redis.Password"));
        core.getMethods().runAsync(pubSubListener = new PubSubListener());
    }

    public void createPlayer(UUID uuid, String nick) {
        if (isRegistred(uuid)) {
            return;
        }
        JsonObject userdata = new JsonObject();
        userdata.addProperty("nick", nick);
        userdata.addProperty("displayname", nick);
        userdata.addProperty("watcher", false);
        userdata.addProperty("exp", 0);
        userdata.addProperty("level", 0);
        userdata.add("ignored", new JsonArray());
        userdata.add("rewards", new JsonArray());
        try (Jedis jedis = pool.getResource()) {
            jedis.hset("ncore_data", uuid.toString(), userdata.toString());
            jedis.hset("ncore_nicks", nick, uuid.toString());
            jedis.hset("ncore_uuids", uuid.toString(), nick);
        }
        Set<PlayerOptionType> options = new HashSet<>();
        options.add(PlayerOptionType.SPEED);
        options.add(PlayerOptionType.CHAT);
        setData(uuid, "options", options);
    }

    public void saveStats(UUID uuid, String server, Statistics stats) {
        JsonObject serverStats = new JsonObject();
        if (stats != null) {
            serverStats.addProperty("mkills", stats.getMobKills());
            serverStats.addProperty("pkills", stats.getPlayerKills());
            serverStats.addProperty("deaths", stats.getDeaths());
            serverStats.addProperty("broken", stats.getBlocksBroken());
            serverStats.addProperty("placed", stats.getBlocksPlaced());
        } else {
            serverStats.addProperty("mkills", 0);
            serverStats.addProperty("pkills", 0);
            serverStats.addProperty("deaths", 0);
            serverStats.addProperty("broken", 0);
            serverStats.addProperty("placed", 0);
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.hset("ncore_" + server + "_stats", uuid.toString(), serverStats.toString());
        }
    }

    public void saveStats(MatrixPlayer player, String server, ServerType serverType, Statistics stats) {
        try (Jedis jedis = pool.getResource()) {
            JsonObject userdata = core.getGson().fromJson(jedis.hget("ncore_data", player.getUniqueId().toString()), JsonObject.class);
            userdata.addProperty("nick", core.getNick(player.getUniqueId()));
            userdata.addProperty("displayname", player.getNickname());
            userdata.addProperty("watcher", player.isWatcher());
            userdata.addProperty("exp", player.getXP());
            userdata.addProperty("level", player.getLevel());
            if (player.getIP() != null) {
                userdata.addProperty("ip", player.getIP());
            }
            JsonArray ignored = new JsonArray();
            player.getIgnoredPlayers().forEach(ign -> ignored.add(ign));
            userdata.add("ignored", ignored);
            JsonArray options = new JsonArray();
            player.getActiveOptions().forEach(opt -> options.add(opt.toString()));
            userdata.add("options", options);
            if (stats != null) {
                userdata.addProperty("lastlogin", stats.getLastlogin());
            }
            jedis.hset("ncore_data", player.getUniqueId().toString(), userdata.toString());
            jedis.hset("ncore_nicks", core.getNick(player.getUniqueId()), player.getUniqueId().toString());
            jedis.hset("ncore_uuids", player.getUniqueId().toString(), core.getNick(player.getUniqueId()));
            if (serverType.equals(ServerType.SURVIVAL)) {
                JsonObject serverStats = new JsonObject();
                if (stats != null) {
                    serverStats.addProperty("mkills", stats.getMobKills());
                    serverStats.addProperty("pkills", stats.getPlayerKills());
                    serverStats.addProperty("deaths", stats.getDeaths());
                    serverStats.addProperty("broken", stats.getBlocksBroken());
                    serverStats.addProperty("placed", stats.getBlocksPlaced());
                } else {
                    serverStats.addProperty("mkills", 0);
                    serverStats.addProperty("pkills", 0);
                    serverStats.addProperty("deaths", 0);
                    serverStats.addProperty("broken", 0);
                    serverStats.addProperty("placed", 0);
                }
                jedis.hset("ncore_" + server + "_stats", player.getUniqueId().toString(), serverStats.toString());
            }
        }
    }

    public void setData(UUID uuid, String property, Object value) {
        JsonObject userdata;
        try (Jedis jedis = pool.getResource()) {
            userdata = core.getGson().fromJson(jedis.hget("ncore_data", uuid.toString()), JsonObject.class);
            if (property.equals("options") && value instanceof Set) {
                JsonArray options = new JsonArray();
                Set<PlayerOptionType> opt = (Set<PlayerOptionType>) value;
                opt.forEach(op -> options.add(op.toString()));
                userdata.add(property, options);
            } else if (value instanceof String) {
                userdata.addProperty(property, (String) value);
            } else if (value instanceof Boolean) {
                userdata.addProperty(property, (Boolean) value);
            } else if (value instanceof Number) {
                userdata.addProperty(property, (Number) value);
            } else {
                core.log("Invalid value '" + value + "' for property: " + property + " in RedisStorage.");
            }
            jedis.hset("ncore_data", uuid.toString(), userdata.toString());
        }
    }

    public long getXP(UUID uuid) {
        long xp;
        try (Jedis jedis = pool.getResource()) {
            xp = core.getGson().fromJson(jedis.hget("ncore_data", uuid.toString()), JsonObject.class).get("exp").getAsLong();
        }
        return xp;
    }

    public boolean isRegistred(UUID uuid) {
        boolean registred;
        try (Jedis jedis = pool.getResource()) {
            registred = jedis.hexists("ncore_data", uuid.toString());
        } catch (Exception ex) {
            registred = false;
        }
        return registred;
    }

    public boolean isRegistred(UUID uuid, String server) {
        boolean registred;
        try (Jedis jedis = pool.getResource()) {
            registred = jedis.hexists("ncore_" + server + "_stats", uuid.toString());
        }
        return registred;
    }

    public boolean isRegistred(String name) {
        boolean registred;
        try (Jedis jedis = pool.getResource()) {
            registred = jedis.hexists("ncore_nicks", name);
        }
        return registred;
    }

    public boolean isRegistred(String name, String server) {
        boolean registred;
        try (Jedis jedis = pool.getResource()) {
            registred = jedis.hexists("ncore_" + server + "_stats", core.getUUID(name).toString());
        }
        return registred;
    }

    public String getName(UUID uuid) {
        String name;
        try (Jedis jedis = pool.getResource()) {
            name = jedis.hget("ncore_uuids", uuid.toString());
        }
        return name;
    }

    public UUID getUUID(String name) {
        UUID uuid;
        try (Jedis jedis = pool.getResource()) {
            uuid = UUID.fromString(jedis.hget("ncore_nicks", name));
        }
        return uuid;
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public class PubSubListener implements Runnable {

        private JedisPubSubHandler jpsh;

        @Override
        public void run() {
            boolean broken = false;
            try (Jedis rsc = pool.getResource()) {
                try {
                    jpsh = new JedisPubSubHandler();
                    rsc.subscribe(jpsh, "core-auth", "core-command", "core-globalcmd", "core-message", "core-staff-message");
                } catch (Exception e) {
                    core.log("PubSub error, attempting to recover.");
                    try {
                        jpsh.unsubscribe();
                    } catch (Exception ignore) {
                    }
                    broken = true;
                }
            }
            if (broken) {
                run();
            }
        }

        public void addChannel(String... channel) {
            jpsh.subscribe(channel);
        }

        public void removeChannel(String... channel) {
            jpsh.unsubscribe(channel);
        }

        public void poison() {
            jpsh.unsubscribe();
        }
    }

    private class JedisPubSubHandler extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            core.debug("Redis Log: Recived a message in channel: " + channel);
            core.debug("Redis Log: Message is:");
            core.debug(message);
            switch (channel) {
                case "core-auth":
                    if (core.getMethods().isOnline(UUID.fromString(message.split(":")[1]))) {
                        if (message.contains("connect:")) {
                            core.getPlayer(UUID.fromString(message.split(":")[1])).setAuthed(true);
                        } else if (message.contains("disconnect:")) {
                            core.getPlayer(UUID.fromString(message.split(":")[1])).setAuthed(false);
                        }
                    }
                    break;
                case "core-command":
                    if (core.getGson().fromJson(message, JsonObject.class).get("server").getAsString().equals(core.getServerInfo().getServerName())) {
                        core.getMethods().executeCommand(core.getGson().fromJson(message, JsonObject.class).get("command").getAsString());
                    }
                    break;
                case "core-gobalcmd":
                    core.getMethods().executeCommand(message);
                    break;
                case "core-message":
                    if (core.isBungee()) {
                        JsonObject jsonmessage = core.getGson().fromJson(message, JsonObject.class);
                        if (core.getMethods().isOnline(UUID.fromString(jsonmessage.get("user").getAsString()), true)) {
                            core.getMethods().sendMessage(UUID.fromString(jsonmessage.get("user").getAsString()), core.rep(jsonmessage.get("message").getAsString()));
                        }
                    }
                    break;
                case "core-staff-message":
                    if (core.isBungee()) {
                        JsonObject staffmsg = core.getGson().fromJson(message, JsonObject.class);
                        core.getPlayers().asMap().forEach((uuid, player) -> {
                            if (player.hasPermission(staffmsg.get("permission").getAsString())) {
                                core.getMethods().sendMessage(uuid, staffmsg.get("message").getAsString());
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
            core.getRedisListeners().forEach(listener -> {
                listener.onMessage(channel, message);
            });
        }
    }
}
