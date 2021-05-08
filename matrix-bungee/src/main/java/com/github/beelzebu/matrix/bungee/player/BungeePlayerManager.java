package com.github.beelzebu.matrix.bungee.player;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.bungee.util.BungeeMetaInjector;
import com.github.beelzebu.matrix.player.AbstractPlayerManager;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class BungeePlayerManager extends AbstractPlayerManager<ProxiedPlayer> {

    public BungeePlayerManager(MatrixBungeeAPI api, BungeeMetaInjector metaInjector) {
        super(api, metaInjector);
    }

    @Override
    public @NotNull UUID getUniqueId(@NotNull ProxiedPlayer proxiedPlayer) {
        return proxiedPlayer.getUniqueId();
    }

    @Override
    public @NotNull String getName(@NotNull ProxiedPlayer proxiedPlayer) {
        return proxiedPlayer.getName();
    }

    @Override
    protected @Nullable ProxiedPlayer getPlatformPlayer(UUID uniqueId) {
        return ProxyServer.getInstance().getPlayer(uniqueId);
    }

    @Override
    protected @Nullable ProxiedPlayer getPlatformPlayerByName(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }

    @Override
    protected @Nullable ProxiedPlayer getPlatformPlayerById(String hexId) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            String playerId = getMetaInjector().getId(player);
            if (playerId == null) {
                continue;
            }
            if (Objects.equals(playerId, hexId)) {
                return player;
            }
        }
        return null;
    }
}
