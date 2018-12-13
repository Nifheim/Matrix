package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.Date;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeMatrixPlayer extends MongoMatrixPlayer {

    private transient ProxiedPlayer player;

    public BungeeMatrixPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void setPremium(boolean premium) {
        super.setPremium(premium);
        setUniqueId(player.getUniqueId());
    }

    public ProxiedPlayer getPlayer() {
        return player == null ? player = ProxyServer.getInstance().getPlayer(uniqueId) : player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer() != null && getPlayer().hasPermission(permission);
    }

    @Override
    public MatrixPlayer save() {
        if (getPlayer() != null) {
            if (getUniqueId() == null) {
                setUniqueId(getPlayer().getUniqueId());
            }
            setName(getPlayer().getName());
            if (getDisplayname() == null) {
                setDisplayname(getPlayer().getName());
            }
            setIP(getPlayer().getAddress().getAddress().getHostAddress());
            setLastLocale(getPlayer().getLocale().toLanguageTag());
        }
        setLastLogin(new Date());
        return super.save();
    }
}
