package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
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
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (matrixPlayer != null) {
            matrixPlayer.setLastLocale(e.getPlayer().getLocale());
        }
    }

    @EventHandler
    public void onLocaleChange(SettingsChangedEvent e) {
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (matrixPlayer != null) {
            matrixPlayer.setLastLocale(e.getPlayer().getLocale());
        }
    }

    @EventHandler(priority = 127)
    public void onDisconnect(PlayerDisconnectEvent e) {
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (matrixPlayer != null) {
            matrixPlayer.setLastLocale(e.getPlayer().getLocale());
        }
    }
}
