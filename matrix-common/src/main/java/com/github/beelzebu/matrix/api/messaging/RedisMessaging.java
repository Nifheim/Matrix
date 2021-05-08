package com.github.beelzebu.matrix.api.messaging;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.Message;
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
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class RedisMessaging implements Messaging {

    public static final String MATRIX_MESSAGING = "matrix:1:message";
    private final Set<UUID> messages = new HashSet<>();
    private final Set<RedisMessageListener<? extends RedisMessage>> redisListeners = new LinkedHashSet<>();
    private final Set<MessageListener> messageListeners = new LinkedHashSet<>();
    private final RedisManager redisManager;
    private final @NotNull PubSubListener pubSubListener;

    public RedisMessaging(RedisManager redisManager, @NotNull Consumer<Runnable> runnableConsumer) {
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
    public void sendMessage(@NotNull RedisMessage redisMessage) {
        Objects.requireNonNull(redisMessage.getUniqueId(), "Can't send a message with null id");
        messages.add(redisMessage.getUniqueId());
        String jsonMessage = Matrix.GSON.toJson(redisMessage);
        sendMessage(MATRIX_MESSAGING, jsonMessage);
        Matrix.getLogger().debug("&7Sent: " + jsonMessage);
    }

    @Override
    public void sendMessage(Message message) {
        sendMessage(MATRIX_MESSAGING, Matrix.GSON.toJson(message));
    }

    public void registerListener(RedisMessageListener<? extends RedisMessage> redisMessageListener) {
        redisListeners.add(redisMessageListener);
    }

    @Override
    public void registerListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    @Override
    public void shutdown() {
        redisListeners.clear();
        if (getPubSubListener().jpsh != null) {
            getPubSubListener().jpsh.unsubscribe();
        }
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = redisManager.getResource()) {
            jedis.publish(channel, message);
        } catch (JedisException ex) {
            Matrix.getLogger().debug(ex);
        }
    }

    public @NotNull PubSubListener getPubSubListener() {
        return pubSubListener;
    }

    public class PubSubListener implements Runnable {

        private JedisPubSubHandler jpsh;

        @Override
        public void run() {
            boolean broken = false;
            try (Jedis jedis = redisManager.getResource()) {
                try {
                    jpsh = new JedisPubSubHandler();
                    jedis.subscribe(jpsh, MATRIX_MESSAGING);
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
            try {
                Message matrixMessage = Matrix.GSON.fromJson(message, Message.class);
                for (MessageListener messageListener : messageListeners) {
                    if (messageListener.getMessageType() == matrixMessage.getMessageType()) {
                        messageListener.onMessage(matrixMessage);
                    }
                }
                return;
            } catch (Exception ignored) {
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
            for (RedisMessageListener<?> listener : redisListeners) {
                if (listener.getType() == type) {
                    Matrix.getLogger().debug("Sending to listener: " + listener);
                    listener.$$onMessage0$$(redisMessage);
                }
            }
        }
    }
}
