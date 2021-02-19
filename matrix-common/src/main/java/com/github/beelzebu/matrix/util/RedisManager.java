package com.github.beelzebu.matrix.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @author Beelzebu
 */
public class RedisManager {

    private final JedisPool pool;

    public RedisManager(String host, int port, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(30);
        config.setMaxTotal(30);
        config.setMaxWaitMillis(1000);
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        if (password == null || password.trim().isEmpty()) {
            pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, null, 2);
        } else {
            pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, password, 2);
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
        }
    }

    public Jedis getResource() {
        return pool.getResource();
    }

    public void shutdown() {
        pool.destroy();
        pool.close();
    }
}
