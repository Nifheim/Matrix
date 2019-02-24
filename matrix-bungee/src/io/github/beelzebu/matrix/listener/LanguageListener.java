package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.Objects;
import net.md_5.bungee.api.event.SettingsChangedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Beelzebu
 */
public class LanguageListener implements Listener {

    @EventHandler
    public void onLanguageChange(SettingsChangedEvent e) {
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(e.getPlayer().getUniqueId());
        if (matrixPlayer == null) {
            return;
        }
        String locale = e.getPlayer().getLocale().getISO3Language();
        if (!Objects.equals(matrixPlayer.getLastLocale(), locale)) {
            matrixPlayer.setLastLocale(locale);
        }
    }
}
