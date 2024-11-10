package net.nifheim.matrix.common.util;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

/**
 * @author Jaime Suárez
 */
public class MaintenanceManager {

    private static final String MAINTENANCE_KEY = "matrix:maintenance";
    private final RedisManager redisManager;
    private final Logger logger;
    private boolean maintenance;
    private long lastCheck;

    public MaintenanceManager(RedisManager redisManager, Logger logger) {
        this.redisManager = redisManager;
        this.logger = logger;
        this.maintenance = isMaintenance();
    }

    public boolean isMaintenance() {
        if (lastCheck >= System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)) {
            return maintenance;
        }
        try (Jedis jedis = redisManager.getResource()) {
            lastCheck = System.currentTimeMillis();
            return (maintenance = jedis.exists(MAINTENANCE_KEY));
        } catch (Exception e) {
            logger.error("Error while checking maintenance status", e);
        }
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        setMaintenance(maintenance, 0);
    }

    private void setMaintenance(boolean maintenance, int retry) {
        if (retry >= 5) {
            return;
        }
        try (Jedis jedis = redisManager.getResource()) {
            if (maintenance) {
                jedis.set(MAINTENANCE_KEY, "0");
            } else {
                jedis.del(MAINTENANCE_KEY);
            }
        } catch (Exception e) {
            logger.error("Error while setting maintenance status", e);
            setMaintenance(maintenance, retry + 1);
            return;
        }
        this.maintenance = maintenance;
    }
}
