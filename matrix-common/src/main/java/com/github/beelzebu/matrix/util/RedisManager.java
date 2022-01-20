package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.api.Matrix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @author Beelzebu
 */
public class RedisManager {

    private final @NotNull JedisPool pool;

    public RedisManager(String host, int port, @Nullable String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(30);
        config.setMaxWaitMillis(1000);
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        if (password == null || password.trim().isEmpty()) {
            pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, null, 3);
        } else {
            pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, password, 3);
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
        }
    }

    public Jedis getResource() {
        return getResource(3);
    }

    private Jedis getResource(int tries) {
        try {
            return pool.getResource();
        } catch (ClassCastException e) {
            Matrix.getLogger().warn("Error obtaining resource, tried " + tries + " times");
            if (tries >= 10) {
                return null;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return getResource(++tries);
        }
    }

    public void shutdown() {
        pool.destroy();
        pool.close();
    }
}
