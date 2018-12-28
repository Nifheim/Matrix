package io.github.beelzebu.matrix.api.messaging;

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
        pool = new JedisPool(new JedisPoolConfig(), api.getConfig().getString("Redis.Host"), api.getConfig().getInt("Redis.Port"), 0, api.getConfig().getString("Redis.Password"));
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
        sendMessage(redisMessage.getChannel(), Matrix.GSON.toJson(redisMessage, redisMessage.getClass()));
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
            try (Jedis rsc = pool.getResource()) {
                try {
                    jpsh = new JedisPubSubHandler();
                    rsc.subscribe(jpsh, MATRIX_CHANNEL);
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
    }

    private class JedisPubSubHandler extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            if (!Objects.equals(channel, MATRIX_CHANNEL)) {
                return;
            }
            RedisMessage redisMessage = Matrix.GSON.fromJson(message, RedisMessage.class);
            if (messages.contains(redisMessage.getUniqueId())) {
                return;
            }
            String subChannel = redisMessage.getChannel();
            api.debug("Redis Log: Received a message in channel: " + subChannel);
            api.debug("Redis Log: Message is:");
            api.debug(message);
            switch (subChannel) {
                case "api-field-update":
                    FieldUpdate fieldMessage = Matrix.GSON.fromJson(message, FieldUpdate.class);
                    if (api.getPlugin().isOnline(fieldMessage.getPlayer(), true)) {
                        api.getPlayer(fieldMessage.getPlayer()).setField(fieldMessage.getField(), Matrix.GSON.fromJson(fieldMessage.getJsonValue(), Object.class));
                    }
                    break;
                case "api-command":
                    CommandMessage commandMessage = Matrix.GSON.fromJson(message, CommandMessage.class);
                    if (commandMessage.isGlobal()) {
                        api.getPlugin().executeCommand(commandMessage.getCommand());
                    } else if (commandMessage.isBungee() && api.isBungee()) {
                        api.getPlugin().executeCommand(commandMessage.getCommand());
                    } else if (commandMessage.isBukkit() && !api.isBungee()) {
                        api.getPlugin().executeCommand(commandMessage.getCommand());
                    }
                    break;
                case "api-message":
                    TargetedMessage targetedMessage = Matrix.GSON.fromJson(message, TargetedMessage.class);
                    if (api.isBungee()) {
                        if (api.getPlugin().isOnline(targetedMessage.getTarget(), true)) {
                            api.getPlugin().sendMessage(targetedMessage.getTarget(), api.rep(targetedMessage.getMessage()));
                        }
                    }
                    break;
                case "api-staff-message":
                    StaffChatMessage staffChatMessage = Matrix.GSON.fromJson(message, StaffChatMessage.class);
                    if (api.isBungee()) {
                        api.getPlayers().stream().filter(p -> api.hasPermission(p, staffChatMessage.getPermission())).forEach(p -> api.getPlugin().sendMessage(p.getUniqueId(), staffChatMessage.getMessage()));
                    }
                    break;
                default:
                    break;
            }
            api.getRedisListeners().forEach(listener -> listener.onMessage(subChannel, message));
        }
    }
}
