package com.github.beelzebu.matrix.bukkit.player;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.player.AbstractPlayerManager;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class BukkitPlayerManager extends AbstractPlayerManager<Player> {

    public BukkitPlayerManager(MatrixBukkitAPI api) {
        super(api);
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayer(Player player) {
        return getPlayerById(api.getMetaInjector().getId(player));
    }

    @Override
    public CompletableFuture<String> getHexId(Player player) {
        return CompletableFuture.completedFuture(api.getMetaInjector().getId(player));
    }

    @Override
    public CompletableFuture<UUID> getUniqueId(Player player) {
        return CompletableFuture.completedFuture(player.getUniqueId());
    }

    @Override
    public CompletableFuture<String> getName(Player player) {
        return CompletableFuture.completedFuture(player.getName());
    }

    @Override
    public CompletableFuture<Boolean> isOnline(Player player, @Nullable String groupName, @Nullable String serverName) {
        if (groupName == null && serverName == null) {
            return CompletableFuture.completedFuture(player.isOnline());
        }
        return isOnlineById(api.getMetaInjector().getId(player), groupName, serverName);
    }

    @Override
    public CompletableFuture<Void> setOnline(Player player) {
        return setOnlineById(api.getMetaInjector().getId(player));
    }

    @Override
    public CompletableFuture<Void> setOffline(Player player) {
        return setOfflineById(api.getMetaInjector().getId(player));
    }
}
