package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.api.Matrix;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public class MaintenanceManager {

    private static final String MAINTENANCE_KEY = "matrix:maintenance";
    private final RedisManager redisManager;
    private boolean maintenance;
    private long lastCheck;

    public MaintenanceManager(RedisManager redisManager) {
        this.redisManager = redisManager;
        this.maintenance = isMaintenance();
    }

    public boolean isMaintenance() {
        if (lastCheck >= System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)) {
            return maintenance;
        }
        try (Jedis jedis = redisManager.getPool().getResource()) {
            lastCheck = System.currentTimeMillis();
            return (maintenance = jedis.exists(MAINTENANCE_KEY));
        } catch (Exception e) {
            Matrix.getLogger().debug(e);
        }
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        try (Jedis jedis = redisManager.getPool().getResource()) {
            if (maintenance) {
                jedis.set(MAINTENANCE_KEY, "0");
            } else {
                jedis.del(MAINTENANCE_KEY);
            }
        } catch (Exception e) {
            Matrix.getLogger().debug(e);
            setMaintenance(maintenance);
            return;
        }
        this.maintenance = maintenance;
    }
}
