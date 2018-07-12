package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeMatrixPlayer extends MatrixPlayer {

    private ProxiedPlayer player;

    public BungeeMatrixPlayer(UUID uuid) {
        super(uuid);
    }

    @Override
    public String getName() {
        return getPlayer().getName();
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        throw new UnsupportedOperationException("getOption is not finished yet.");
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        throw new UnsupportedOperationException("setOption is not finished yet.");
    }

    @Override
    public void setAllOptions(Map<PlayerOptionType, Boolean> options) {
        throw new UnsupportedOperationException("setAllOptions is not finished yet.");
    }

    @Override
    public Set<PlayerOptionType> getActiveOptions() {
        throw new UnsupportedOperationException("getActiveOptions is not finished yet.");
    }

    @Override
    public boolean isInLobby() {
        return getPlayer().getServer().getInfo().getName().contains("Lobby");
    }

    @Override
    public void setNick(String nick) {
        throw new UnsupportedOperationException("setNick is not finished yet.");
    }

    @Override
    public String getIP() {
        throw new UnsupportedOperationException("getIP is not finished yet.");
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }

    public ProxiedPlayer getPlayer() {
        return player != null ? player : (player = ProxyServer.getInstance().getPlayer(uniqueId));
    }
}
