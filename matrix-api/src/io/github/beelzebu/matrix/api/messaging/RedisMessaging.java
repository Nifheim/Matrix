package io.github.beelzebu.matrix.api.messaging;

import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.messaging.message.AuthMessage;
import io.github.beelzebu.matrix.api.messaging.message.CommandMessage;
import io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import io.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
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

    private final MatrixAPI api;
    @Getter
    private final JedisPool pool;
    @Getter
    private final PubSubListener pubSubListener;

    public RedisMessaging(MatrixAPI api) {
        this.api = api;
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

    public class PubSubListener implements Runnable {

        private JedisPubSubHandler jpsh;

        @Override
        public void run() {
            boolean broken = false;
            try (Jedis rsc = pool.getResource()) {
                try {
                    jpsh = new JedisPubSubHandler();
                    rsc.subscribe(jpsh, "api-auth", "api-command", "api-message", "api-staff-chat");
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
            api.debug("Redis Log: Received a message in channel: " + channel);
            api.debug("Redis Log: Message is:");
            api.debug(message);
            switch (channel) {
                case "api-auth":
                    AuthMessage authMessage = api.getGson().fromJson(message, AuthMessage.class);
                    if (api.getPlugin().isOnline(authMessage.getUser(), true)) {
                        api.getPlayer(authMessage.getUser()).setAuthed(authMessage.isAuthed(), false);
                    }
                    break;
                case "api-command":
                    CommandMessage commandMessage = api.getGson().fromJson(message, CommandMessage.class);
                    if (commandMessage.isGlobal()) {
                        api.getPlugin().executeCommand(commandMessage.getCommand());
                    } else if (commandMessage.isBungee() && api.isBungee()) {
                        api.getPlugin().executeCommand(commandMessage.getCommand());
                    } else if (commandMessage.isBukkit() && !api.isBungee()) {
                        api.getPlugin().executeCommand(commandMessage.getCommand());
                    }
                    break;
                case "api-message":
                    TargetedMessage targetedMessage = api.getGson().fromJson(message, TargetedMessage.class);
                    if (api.isBungee()) {
                        if (api.getPlugin().isOnline(targetedMessage.getTarget(), true)) {
                            api.getPlugin().sendMessage(targetedMessage.getTarget(), api.rep(targetedMessage.getMessage()));
                        }
                    }
                    break;
                case "api-staff-message":
                    StaffChatMessage staffChatMessage = api.getGson().fromJson(message, StaffChatMessage.class);
                    if (api.isBungee()) {
                        api.getPlayers().stream().filter(p -> api.hasPermission(p, staffChatMessage.getPermission())).forEach(p -> api.getPlugin().sendMessage(p.getUniqueId(), staffChatMessage.getMessage()));
                    }
                    break;
                default:
                    break;
            }
            api.getRedisListeners().forEach(listener -> listener.onMessage(channel, message));
        }
    }
}
