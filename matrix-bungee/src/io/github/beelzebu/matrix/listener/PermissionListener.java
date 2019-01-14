package io.github.beelzebu.matrix.listener;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.UUID;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Beelzebu
 */
public class PermissionListener {

    public PermissionListener() {
        LuckPerms.getApi().getEventBus().subscribe(UserDataRecalculateEvent.class, this::onRecalculate);
    }

    private void onRecalculate(UserDataRecalculateEvent e) {
        UUID uniqueId = e.getUser().getUuid();
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uniqueId);
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
