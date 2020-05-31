package io.github.beelzebu.matrix.api.messaging;

import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.messaging.message.RedisMessage;
import io.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
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
    private final JedisPool pool;
    private final PubSubListener pubSubListener;
    private final Set<UUID> messages = new HashSet<>();
    private final Set<RedisMessageListener<? extends RedisMessage>> listeners = new LinkedHashSet<>();

    public RedisMessaging(String host, int port, String password, Consumer<Runnable> runnableConsumer) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(1);
        config.setMaxTotal(101);
        config.setBlockWhenExhausted(true);
        if (password == null || password.trim().isEmpty()) {
            pool = new JedisPool(config, host, port, 0);
        } else {
            pool = new JedisPool(config, host, port, 0, password);
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
        }
        runnableConsumer.accept(pubSubListener = new PubSubListener());
    }

    /**
     * Send message using redis pub/sub, the message will be converted to JSON and converted to objects again in other
     * servers if they should manage it. Also it will save the unique id of this message so it won't be handled by the
     * listener of this instance.
     *
     * @param redisMessage message to send though redis.
     */
    public void sendMessage(RedisMessage redisMessage) {
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

    public JedisPool getPool() {
        return pool;
    }

    public PubSubListener getPubSubListener() {
        return pubSubListener;
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
            RedisMessage redisMessage = RedisMessage.getFromType(type, message);
            if (redisMessage == null) {
                Matrix.getLogger().debug("RedisMessage is null.");
                return;
            }
            redisMessage.read();
            Matrix.getLogger().debug("Sending message to listeners...");
            for (RedisMessageListener<?> listener : listeners) {
                if (listener.getType() == type) {
                    Matrix.getLogger().debug("Sending to listener: " + listener);
                    listener.$$onMessage0$$(redisMessage);
                }
            }
        }
    }
}
