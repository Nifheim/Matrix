package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.SettingsChangedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class LocaleListener implements Listener {

    private final MatrixBungeeAPI api;

    public LocaleListener(MatrixBungeeAPI api) {
        this.api = api;
    }

    @EventHandler(priority = 127)
    public void onPlayerJoin(@NotNull PostLoginEvent e) {
        api.getPlayerManager().getPlayer(e.getPlayer()).thenAccept(matrixPlayer -> {
            if (matrixPlayer != null) {
                matrixPlayer.setLastLocale(e.getPlayer().getLocale());
            }
        });
    }

    @EventHandler
    public void onLocaleChange(@NotNull SettingsChangedEvent e) {
        api.getPlayerManager().getPlayer(e.getPlayer()).thenAccept(matrixPlayer -> {
            if (matrixPlayer != null) {
                matrixPlayer.setLastLocale(e.getPlayer().getLocale());
            }
        });
    }

    @EventHandler(priority = 127)
    public void onDisconnect(@NotNull PlayerDisconnectEvent e) {
        api.getPlayerManager().getPlayer(e.getPlayer()).thenAccept(matrixPlayer -> {
            if (matrixPlayer != null) {
                matrixPlayer.setLastLocale(e.getPlayer().getLocale());
            }
        });
    }
}
