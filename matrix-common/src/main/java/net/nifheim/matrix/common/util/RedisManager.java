package net.nifheim.matrix.common.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import net.nifheim.matrix.common.config.sub.RedisConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @author Jaime Suárez
 */
public class RedisManager {

    private final @NotNull JedisPool pool;
    private final Logger logger;

    public RedisManager(RedisConfiguration redisConfiguration, Logger logger) {
        this.logger = logger;
        JedisPoolConfig config = getJedisPoolConfig(redisConfiguration);
        if (redisConfiguration.getPassword() == null || redisConfiguration.getPassword().trim().isEmpty()) {
            pool = new JedisPool(config, redisConfiguration.getHost(), redisConfiguration.getPort(), Protocol.DEFAULT_TIMEOUT, null, redisConfiguration.getDatabase());
        } else {
            pool = new JedisPool(config, redisConfiguration.getHost(), redisConfiguration.getPort(), Protocol.DEFAULT_TIMEOUT, redisConfiguration.getPassword(), redisConfiguration.getDatabase());
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
        }
    }

    @NotNull
    private static JedisPoolConfig getJedisPoolConfig(RedisConfiguration redisConfiguration) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfiguration.getMaxActive());
        config.setMaxIdle(Math.min(redisConfiguration.getMaxActive(), redisConfiguration.getMaxIdle()));
        config.setMinIdle(Math.min(redisConfiguration.getMaxActive(), redisConfiguration.getMinIdle()));
        config.setMaxWait(Duration.of(1000, ChronoUnit.MILLIS));
        config.setBlockWhenExhausted(redisConfiguration.isBlockWhenExhausted());
        config.setTestOnBorrow(redisConfiguration.isTestOnBorrow());
        config.setTestWhileIdle(redisConfiguration.isTestWhileIdle());
        return config;
    }

    public Jedis getResource() {
        return getResource(3);
    }

    public boolean isClosed() {
        return pool.isClosed();
    }

    private Jedis getResource(int tries) {
        try {
            return pool.getResource();
        } catch (ClassCastException e) {
            logger.error("Error obtaining resource, tried {} times", tries);
            if (tries >= 10) {
                return null;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                logger.error("Error sleeping", ex);
            }
            return getResource(++tries);
        }
    }

    public void shutdown() {
        pool.destroy();
        pool.close();
    }
}
