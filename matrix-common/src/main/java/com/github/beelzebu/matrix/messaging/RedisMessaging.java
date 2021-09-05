package com.github.beelzebu.matrix.messaging;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.Messaging;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.util.RedisManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Beelzebu
 */
public class RedisMessaging implements Messaging {

    public static final String MATRIX_MESSAGING = "matrix:2:message";
    private final Set<UUID> messages = new HashSet<>();
    private final ConcurrentLinkedDeque<MessageListener> messageListeners = new ConcurrentLinkedDeque<>();
    private final RedisManager redisManager;
    private final @NotNull PubSubListener pubSubListener;

    public RedisMessaging(RedisManager redisManager, @NotNull Consumer<Runnable> runnableConsumer) {
        this.redisManager = redisManager;
        runnableConsumer.accept(pubSubListener = new PubSubListener());
    }

    @Override
    public void sendMessage(Message message) {
        messages.add(message.getUniqueId());
        sendMessage(MATRIX_MESSAGING, Matrix.GSON.toJson(message));
    }

    @Override
    public void registerListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    @Override
    public void shutdown() {
        messageListeners.clear();
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
        public synchronized void onMessage(String channel, String message) {
            Message matrixMessage = Matrix.GSON.fromJson(message, Message.class);
            if (messages.contains(matrixMessage.getUniqueId())) {
                return;
            }
            Matrix.getLogger().debug("Redis Log: Message is: " + message);
            for (MessageListener messageListener : messageListeners) {
                try {
                    if (messageListener.getMessageType() == matrixMessage.getMessageType()) {
                        messageListener.onMessage(matrixMessage);
                    }
                } catch (Exception exception) {
                    Matrix.getLogger().debug(exception);
                }
            }
        }
    }
}
