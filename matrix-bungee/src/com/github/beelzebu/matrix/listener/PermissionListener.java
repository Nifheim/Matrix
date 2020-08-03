package com.github.beelzebu.matrix.listener;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import java.util.UUID;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Beelzebu
 */
public class PermissionListener {

    public PermissionListener() {
        LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, this::onRecalculate);
    }

    private void onRecalculate(UserDataRecalculateEvent e) {
        UUID uniqueId = e.getUser().getUniqueId();
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uniqueId);
        if (proxiedPlayer == null) {
            return;
        }
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(uniqueId);
        boolean admin = matrixPlayer.isAdmin();
        if (proxiedPlayer.hasPermission("matrix.admin")) {
            if (admin) {
                return;
            }
            matrixPlayer.setAdmin(true);
        } else if (admin) {
            matrixPlayer.setAdmin(false);
        }
    }
}
