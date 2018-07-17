package io.github.beelzebu.matrix.api.server.powerup.tasks;

import io.github.beelzebu.matrix.manager.PowerupManager;

public class PowerupSpawnTask implements Runnable {

    @Override
    public synchronized void run() {
        PowerupManager.getInstance().getPowerups().forEach((powerup) -> {
            PowerupManager.getInstance().spawnPowerup(powerup);
        });
    }
}
