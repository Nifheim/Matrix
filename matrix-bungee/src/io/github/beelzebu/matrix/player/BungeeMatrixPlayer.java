package io.github.beelzebu.matrix.player;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
public class BungeeMatrixPlayer extends MongoMatrixPlayer {

    private transient final ProxiedPlayer player;

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void save() {
        if (getUniqueId() == null) {
            setUniqueId(player.getUniqueId());
        }
        setName(player.getName());
        if (getDisplayname() == null) {
            setDisplayname(player.getName());
        }
        setLastLogin(new Date());
        setIP(player.getAddress().getAddress().getHostAddress());
        super.save();
    }
}
