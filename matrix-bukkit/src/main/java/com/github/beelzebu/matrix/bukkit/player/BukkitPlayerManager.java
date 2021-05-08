package com.github.beelzebu.matrix.bukkit.player;

import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.bukkit.util.BukkitMetaInjector;
import com.github.beelzebu.matrix.player.AbstractPlayerManager;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class BukkitPlayerManager extends AbstractPlayerManager<Player> {

    public BukkitPlayerManager(MatrixBukkitAPI api, BukkitMetaInjector metaInjector) {
        super(api, metaInjector);
    }

    @Override
    public @NotNull UUID getUniqueId(@NotNull Player player) {
        return player.getUniqueId();
    }

    @Override
    public @NotNull String getName(@NotNull Player player) {
        return player.getName();
    }

    @Override
    protected @Nullable Player getPlatformPlayer(@NotNull UUID uniqueId) {
        return Bukkit.getPlayer(uniqueId);
    }

    @Override
    protected @Nullable Player getPlatformPlayerByName(@NotNull String name) {
        return Bukkit.getPlayer(name);
    }

    @Override
    protected @Nullable Player getPlatformPlayerById(String hexId) {
        for (Player player : Bukkit.getOnlinePlayers()) {
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
