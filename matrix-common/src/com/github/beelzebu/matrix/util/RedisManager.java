package com.github.beelzebu.matrix.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Beelzebu
 */
public class RedisManager {

    public static final String MATRIX_CHANNEL = "matrix-messaging";
    private final JedisPool pool;

    public RedisManager(String host, int port, String password) {
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
    }

    public JedisPool getPool() {
        return pool;
    }
}
