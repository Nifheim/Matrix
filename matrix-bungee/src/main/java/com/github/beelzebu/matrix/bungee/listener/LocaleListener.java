package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.Matrix;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.SettingsChangedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Beelzebu
 */
public class LocaleListener implements Listener {

    @EventHandler(priority = 127)
    public void onPlayerJoin(PostLoginEvent e) {
        Matrix.getAPI().getDatabase().getPlayer(e.getPlayer().getUniqueId()).thenAccept(matrixPlayer -> {
            if (matrixPlayer != null) {
                matrixPlayer.setLastLocale(e.getPlayer().getLocale());
            }
        });
    }

    @EventHandler
    public void onLocaleChange(SettingsChangedEvent e) {
        Matrix.getAPI().getDatabase().getPlayer(e.getPlayer().getUniqueId()).thenAccept(matrixPlayer -> {
            if (matrixPlayer != null) {
                matrixPlayer.setLastLocale(e.getPlayer().getLocale());
            }
        });
    }

    @EventHandler(priority = 127)
    public void onDisconnect(PlayerDisconnectEvent e) {
        Matrix.getAPI().getDatabase().getPlayer(e.getPlayer().getUniqueId()).thenAccept(matrixPlayer -> {
            if (matrixPlayer != null) {
                matrixPlayer.setLastLocale(e.getPlayer().getLocale());
            }
        });
    }
}
