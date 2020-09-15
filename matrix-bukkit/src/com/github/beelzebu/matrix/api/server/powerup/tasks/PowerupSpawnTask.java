package com.github.beelzebu.matrix.api.server.powerup.tasks;

import com.github.beelzebu.matrix.bukkit.manager.PowerupManager;

public class PowerupSpawnTask implements Runnable {

    @Override
    public synchronized void run() {
        PowerupManager.getInstance().getPowerups().forEach((powerup) -> {
            PowerupManager.getInstance().spawnPowerup(powerup);
        });
    }
}
