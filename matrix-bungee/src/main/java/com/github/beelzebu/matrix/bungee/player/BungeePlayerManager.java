package com.github.beelzebu.matrix.bungee.player;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.player.AbstractPlayerManager;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class BungeePlayerManager extends AbstractPlayerManager<ProxiedPlayer> {

    public BungeePlayerManager(MatrixBungeeAPI api) {
        super(api);
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayer(ProxiedPlayer proxiedPlayer) {
        String hexId = api.getMetaInjector().getId(proxiedPlayer);
        if (hexId != null) {
            return getPlayerById(hexId);
        }
        return getPlayer(proxiedPlayer.getUniqueId());
    }

    @Override
    public CompletableFuture<String> getHexId(ProxiedPlayer proxiedPlayer) {
        return CompletableFuture.completedFuture(api.getMetaInjector().getId(proxiedPlayer));
    }

    @Override
    public CompletableFuture<UUID> getUniqueId(ProxiedPlayer proxiedPlayer) {
        return CompletableFuture.completedFuture(proxiedPlayer.getUniqueId());
    }

    @Override
    public CompletableFuture<String> getName(ProxiedPlayer proxiedPlayer) {
        return CompletableFuture.completedFuture(proxiedPlayer.getName());
    }

    @Override
    public CompletableFuture<Boolean> isOnline(ProxiedPlayer proxiedPlayer, @Nullable String groupName, @Nullable String serverName) {
        if (groupName == null && serverName == null) {
            return CompletableFuture.completedFuture(proxiedPlayer.isConnected());
        }
        return isOnlineById(api.getMetaInjector().getId(proxiedPlayer), groupName, serverName);
    }

    @Override
    public CompletableFuture<Void> setOnline(ProxiedPlayer proxiedPlayer) {
        return setOnlineById(api.getMetaInjector().getId(proxiedPlayer));
    }

    @Override
    public CompletableFuture<Void> setOffline(ProxiedPlayer proxiedPlayer) {
        return setOfflineById(api.getMetaInjector().getId(proxiedPlayer));
    }
}
