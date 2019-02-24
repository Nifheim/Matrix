package io.github.beelzebu.matrix.api.messaging;

import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.messaging.message.CommandMessage;
import io.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
import io.github.beelzebu.matrix.api.messaging.message.RedisMessage;
import io.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import io.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private final Set<RedisMessageListener<? extends RedisMessage>> listeners = new LinkedHashSet<>();

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
        String jsonMessage = Matrix.GSON.toJson(redisMessage);
        sendMessage(MATRIX_CHANNEL, jsonMessage);
        Matrix.getLogger().debug("&7Sent: " + jsonMessage);
    }

    public void registerListener(RedisMessageListener<? extends RedisMessage> redisMessageListener) {
        listeners.add(redisMessageListener);
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        } catch (JedisException ex) {
            Matrix.getLogger().debug(ex);
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
                    Matrix.getLogger().info("PubSub error, attempting to recover.");
                    Matrix.getLogger().debug(e);
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
            RedisMessageType type = RedisMessageType.valueOf(jobj.get("redisMessageType").getAsString());
            Matrix.getLogger().debug("Redis Log: Received a message in channel: " + type);
            Matrix.getLogger().debug("Redis Log: Message is:");
            Matrix.getLogger().debug(message);
            switch (type) {
                case FIELD_UPDATE:
                    FieldUpdate fieldMessage = Matrix.GSON.fromJson(message, FieldUpdate.class);
                    fieldMessage.read();
                    break;
                case COMMAND:
                    CommandMessage commandMessage = Matrix.GSON.fromJson(message, CommandMessage.class);
                    commandMessage.read();
                    break;
                case TARGETED_MESSAGE:
                    TargetedMessage targetedMessage = Matrix.GSON.fromJson(message, TargetedMessage.class);
                    targetedMessage.read();
                    break;
                case STAFF_CHAT:
                    StaffChatMessage staffChatMessage = Matrix.GSON.fromJson(message, StaffChatMessage.class);
                    staffChatMessage.read();
                    break;
                default:
                    break;
            }
            api.getRedisListeners().forEach(listener -> listener.onMessage(type.toString(), message));
            listeners.stream().filter(redisMessageListener -> redisMessageListener.getType().equals(type)).forEach(redisMessageListener -> redisMessageListener.$$onMessage0$$(RedisMessage.getFromType(type, message)));
        }
    }
}
