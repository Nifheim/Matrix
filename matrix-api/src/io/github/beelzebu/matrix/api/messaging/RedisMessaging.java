package io.github.beelzebu.matrix.api.messaging;

import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import java.util.UUID;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class RedisMessaging {

    private final MatrixAPI api = Matrix.getAPI();
    @Getter
    private final JedisPool pool;
    @Getter
    private final PubSubListener pubSubListener;

    public RedisMessaging() {
        pool = new JedisPool(new JedisPoolConfig(), api.getConfig().getString("Redis.Host"), api.getConfig().getInt("Redis.Port"), 0, api.getConfig().getString("Redis.Password"));
        api.getPlugin().runAsync(pubSubListener = new PubSubListener());
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        } catch (JedisException ex) {
            api.debug(ex);
        }
    }

    public void setex(String key, int seconds, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(key, seconds, value);
        } catch (JedisException ex) {
            api.debug(ex);
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
                    rsc.subscribe(jpsh, "__keyevent@0__:expired", "api-auth", "api-command", "api-globalcmd", "api-message", "api-staff-message");
                } catch (Exception e) {
                    api.log("PubSub error, attempting to recover.");
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
            api.debug("Redis Log: Recived a message in channel: " + channel);
            api.debug("Redis Log: Message is:");
            api.debug(message);
            switch (channel) {
                case "api-auth":
                    if (api.getPlugin().isOnline(UUID.fromString(message.split(":")[1]), false)) {
                        if (message.contains("connect:")) {
                            api.getPlayer(UUID.fromString(message.split(":")[1])).setAuthed(true);
                        } else if (message.contains("disconnect:")) {
                            api.getPlayer(UUID.fromString(message.split(":")[1])).setAuthed(false);
                        }
                    }
                    break;
                case "api-command":
                    if (api.getGson().fromJson(message, JsonObject.class).get("server").getAsString().equals(api.getServerInfo().getServerName())) {
                        api.getPlugin().executeCommand(api.getGson().fromJson(message, JsonObject.class).get("command").getAsString());
                    }
                    break;
                case "api-gobalcmd":
                    api.getPlugin().executeCommand(message);
                    break;
                case "api-message":
                    if (api.isBungee()) {
                        JsonObject jsonmessage = api.getGson().fromJson(message, JsonObject.class);
                        if (api.getPlugin().isOnline(UUID.fromString(jsonmessage.get("user").getAsString()), true)) {
                            api.getPlugin().sendMessage(UUID.fromString(jsonmessage.get("user").getAsString()), api.rep(jsonmessage.get("message").getAsString()));
                        }
                    }
                    break;
                case "api-staff-message":
                    if (api.isBungee()) {
                        JsonObject staffmsg = api.getGson().fromJson(message, JsonObject.class);
                        api.getPlayers().stream().filter(player -> player.hasPermission(staffmsg.get("permission").getAsString())).forEach(player -> api.getPlugin().sendMessage(player.getUniqueId(), staffmsg.get("message").getAsString()));
                    }
                    break;
                default:
                    break;
            }
            api.getRedisListeners().forEach(listener -> listener.onMessage(channel, message));
        }
    }
}
