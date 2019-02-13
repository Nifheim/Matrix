package io.github.beelzebu.matrix.api.messaging;

import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.messaging.message.CommandMessage;
import io.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import io.github.beelzebu.matrix.api.messaging.message.RedisMessage;
import io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import io.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class RedisMessaging {

    public static final String MATRIX_CHANNEL = "matrix-messaging";
    private final MatrixAPI api;
    @Getter
    private final JedisPool pool;
    @Getter
    private final PubSubListener pubSubListener;
    private final Set<UUID> messages = new HashSet<>();

    public RedisMessaging(MatrixAPI api) {
        this.api = api;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(1);
        config.setMaxTotal(101);
        pool = new JedisPool(config, api.getConfig().getString("Redis.Host"), api.getConfig().getInt("Redis.Port"), 0, api.getConfig().getString("Redis.Password"));
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
        }
        api.getPlugin().runAsync(pubSubListener = new PubSubListener());
    }

    /**
     * Send message using redis pub/sub, the message will be converted to JSON and converted to objects again in other
     * servers if they should manage it. Also it will save the unique id of this message so it won't be handled by the
     * listener of this instance.
     *
     * @param redisMessage message to send though redis.
     */
    public void sendMessage(@NonNull RedisMessage redisMessage) {
        Objects.requireNonNull(redisMessage.getUniqueId(), "Can't send a message with null id");
        messages.add(redisMessage.getUniqueId());
        String jsonMessage = Matrix.GSON.toJson(redisMessage, redisMessage.getClass());
        sendMessage(MATRIX_CHANNEL, jsonMessage);
        api.debug("&7Sent: " + jsonMessage);
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        } catch (JedisException ex) {
            api.debug(ex);
        }
    }

    public class PubSubListener implements Runnable {

        private JedisPubSubHandler jpsh;

        @Override
        public void run() {
            boolean broken = false;
            try (Jedis jedis = pool.getResource()) {
                try {
                    jpsh = new JedisPubSubHandler();
                    jedis.subscribe(jpsh, MATRIX_CHANNEL);
                } catch (Exception e) {
                    api.log("PubSub error, attempting to recover.");
                    api.debug(e);
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
    }

    private class JedisPubSubHandler extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            JsonObject jobj = Matrix.GSON.fromJson(message, JsonObject.class);
            if (messages.contains(UUID.fromString(jobj.get("uniqueId").getAsString()))) {
                return;
            }
            String subChannel = jobj.get("channel").getAsString();
            api.debug("Redis Log: Received a message in channel: " + subChannel);
            api.debug("Redis Log: Message is:");
            api.debug(message);
            switch (subChannel) {
                case "api-field-update":
                    FieldUpdate fieldMessage = Matrix.GSON.fromJson(message, FieldUpdate.class);
                    fieldMessage.read();
                    break;
                case "api-command":
                    CommandMessage commandMessage = Matrix.GSON.fromJson(message, CommandMessage.class);
                    commandMessage.read();
                    break;
                case "api-message":
                    TargetedMessage targetedMessage = Matrix.GSON.fromJson(message, TargetedMessage.class);
                    targetedMessage.read();
                    break;
                case "api-staff-chat":
                    StaffChatMessage staffChatMessage = Matrix.GSON.fromJson(message, StaffChatMessage.class);
                    staffChatMessage.read();
                    break;
                default:
                    break;
            }
            api.getRedisListeners().forEach(listener -> listener.onMessage(subChannel, message));
        }
    }
}
