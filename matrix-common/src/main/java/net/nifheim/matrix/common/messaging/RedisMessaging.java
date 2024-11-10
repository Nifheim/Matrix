package net.nifheim.matrix.common.messaging;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.MessagingService;
import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.messaging.message.FieldUpdateMessage;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;
import net.nifheim.matrix.common.messaging.message.ServerRequestMessage;
import net.nifheim.matrix.common.messaging.message.ServerUnregisterMessage;
import net.nifheim.matrix.common.messaging.message.StaffChatMessage;
import net.nifheim.matrix.common.messaging.message.TargetedMessage;
import net.nifheim.matrix.common.util.RedisManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @author Jaime Suárez
 */
public class RedisMessaging implements MessagingService {

    public static final String MATRIX_MESSAGING = "matrix:2:message";
    private final Set<UUID> messages = new HashSet<>();
    private final ConcurrentLinkedDeque<MessageListener> messageListeners = new ConcurrentLinkedDeque<>();
    private final RedisManager redisManager;
    private final Logger logger;
    private final @NotNull PubSubListener pubSubListener;

    public RedisMessaging(RedisManager redisManager, Logger logger, @NotNull Consumer<Runnable> runnableConsumer) {
        this.redisManager = redisManager;
        this.logger = logger;
        runnableConsumer.accept(pubSubListener = new PubSubListener());
    }

    @Override
    public void sendMessage(Message message) {
        messages.add(message.getUniqueId());
        sendMessage(MATRIX_MESSAGING, MatrixCommon.GSON.toJson(message));
    }

    @Override
    public void registerListener(MessageListener<? extends Message> messageListener) {
        messageListeners.add(messageListener);
    }

    @Override
    public void shutdown() {
        messageListeners.clear();
        if (getPubSubListener().jpsh != null) {
            getPubSubListener().jpsh.unsubscribe();
        }
    }

    @Override
    public boolean isActive() {
        return getPubSubListener().jpsh.isSubscribed();
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = redisManager.getResource()) {
            jedis.publish(channel, message);
        } catch (JedisException ex) {
            logger.error("Error sending message to Redis", ex);
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
                } catch (Exception ex) {
                    logger.info("PubSub error, attempting to recover.");
                    logger.debug(ex.getMessage(), ex);
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
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            if (jsonMessage.has("uniqueId")) {
                if (messages.contains(UUID.fromString(jsonMessage.get("uniqueId").getAsString()))) {
                    return;
                }
            }
            String subChannel = jsonMessage.get("channel").getAsString();
            Message matrixMessage = MatrixCommon.GSON.fromJson(jsonMessage, getClassForChannel(subChannel));
            if (messages.contains(matrixMessage.getUniqueId())) {
                return;
            }
            //logger.warn("Redis Log: Message is: {}", message);
            messageListeners.forEach(messageListener -> {
                try {
                    if (Objects.equals(messageListener.getChannel(), matrixMessage.getChannel())) {
                        //noinspection unchecked
                        messageListener.onMessage(matrixMessage);
                    }
                } catch (Exception ex) {
                    logger.error("Error processing message", ex);
                }
            });
        }

        private Class<? extends Message> getClassForChannel(String channel) {
            StandardChannel standardChannel = getChannel(channel);
            if (standardChannel == null) {
                return Message.class;
            }
            return switch (standardChannel) {
                case SERVER_REGISTER -> ServerRegisterMessage.class;
                case SERVER_UNREGISTER -> ServerUnregisterMessage.class;
                case MESSAGE_BROADCAST -> StaffChatMessage.class;
                case MESSAGE_TARGETED -> TargetedMessage.class;
                case UPDATE_FIELD -> FieldUpdateMessage.class;
                case SERVER_REQUEST -> ServerRequestMessage.class;
                default -> Message.class;
            };
        }

        private StandardChannel getChannel(String channel) {
            for (StandardChannel value : StandardChannel.values()) {
                if (value.getChannelName().equals(channel)) {
                    return value;
                }
            }
            return null;
        }
    }
}
