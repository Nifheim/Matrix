package com.github.beelzebu.matrix.messaging;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.Messaging;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessage;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.util.RedisManager;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class RedisMessaging implements Messaging {

    private final Set<UUID> messages = new HashSet<>();
    private final Set<RedisMessageListener<? extends RedisMessage>> listeners = new LinkedHashSet<>();
    private final RedisManager redisManager;
    private final PubSubListener pubSubListener;

    public RedisMessaging(RedisManager redisManager, Consumer<Runnable> runnableConsumer) {
        this.redisManager = redisManager;
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
        sendMessage(RedisManager.MATRIX_CHANNEL, jsonMessage);
        Matrix.getLogger().debug("&7Sent: " + jsonMessage);
    }

    public void registerListener(RedisMessageListener<? extends RedisMessage> redisMessageListener) {
        listeners.add(redisMessageListener);
    }

    @Override
    public void shutdown() {
        listeners.clear();
        if (getPubSubListener().jpsh != null) {
            getPubSubListener().jpsh.unsubscribe();
        }
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            jedis.publish(channel, message);
        } catch (JedisException ex) {
            Matrix.getLogger().debug(ex);
        }
    }

    public PubSubListener getPubSubListener() {
        return pubSubListener;
    }

    public class PubSubListener implements Runnable {

        private JedisPubSubHandler jpsh;

        @Override
        public void run() {
            boolean broken = false;
            try (Jedis jedis = redisManager.getPool().getResource()) {
                try {
                    jpsh = new JedisPubSubHandler();
                    jedis.subscribe(jpsh, RedisManager.MATRIX_CHANNEL);
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
