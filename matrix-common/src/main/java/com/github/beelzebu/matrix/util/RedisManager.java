package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.config.MatrixConfiguration;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @author Beelzebu
 */
public class RedisManager {

    private final @NotNull JedisPool pool;

    public RedisManager(MatrixConfiguration.RedisConfiguration redisConfiguration) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfiguration.getMaxActive());
        config.setMaxIdle(Math.min(redisConfiguration.getMaxActive(), redisConfiguration.getMaxIdle()));
        config.setMinIdle(Math.min(redisConfiguration.getMaxActive(), redisConfiguration.getMinIdle()));
        config.setMaxWaitMillis(1000);
        config.setBlockWhenExhausted(redisConfiguration.isBlockWhenExhausted());
        config.setTestOnBorrow(redisConfiguration.isTestOnBorrow());
        config.setTestWhileIdle(redisConfiguration.isTestWhileIdle());
        if (redisConfiguration.getPassword() == null || redisConfiguration.getPassword().trim().isEmpty()) {
            pool = new JedisPool(config, redisConfiguration.getHost(), redisConfiguration.getPort(), Protocol.DEFAULT_TIMEOUT, null, redisConfiguration.getDatabase());
        } else {
            pool = new JedisPool(config, redisConfiguration.getHost(), redisConfiguration.getPort(), Protocol.DEFAULT_TIMEOUT, redisConfiguration.getPassword(), redisConfiguration.getDatabase());
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
