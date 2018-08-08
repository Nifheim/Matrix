package io.github.beelzebu.matrix.player;

import java.util.Date;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeMatrixPlayer extends MongoMatrixPlayer {

    private transient ProxiedPlayer player;

    public BungeeMatrixPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public ProxiedPlayer getPlayer() {
        return player == null ? player = ProxyServer.getInstance().getPlayer(uniqueId) : player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer() != null && getPlayer().hasPermission(permission);
    }

    @Override
    public void save() {
        if (getPlayer() != null) {
            if (getUniqueId() == null) {
                setUniqueId(getPlayer().getUniqueId());
            }
            setName(getPlayer().getName());
            if (getDisplayname() == null) {
                setDisplayname(getPlayer().getName());
            }
            setIP(getPlayer().getAddress().getAddress().getHostAddress());
        }
        setLastLogin(new Date());
        super.save();
    }
}
