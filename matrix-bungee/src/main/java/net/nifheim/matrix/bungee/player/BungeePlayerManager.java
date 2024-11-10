package net.nifheim.matrix.bungee.player;

import net.nifheim.matrix.api.MatrixBungeeAPI;
import net.nifheim.matrix.bungee.util.BungeeMetaInjector;
import net.nifheim.matrix.common.player.AbstractPlayerManager;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jaime Suárez
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
    public @Nullable ProxiedPlayer getPlatformPlayer(UUID uniqueId) {
        return ProxyServer.getInstance().getPlayer(uniqueId);
    }

    @Override
    public @Nullable ProxiedPlayer getPlatformPlayerByName(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }

    @Override
    public @Nullable ProxiedPlayer getPlatformPlayerById(String hexId) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (Objects.equals(getMetaInjector().getId(player), hexId)) {
                return player;
            }
        }
        return null;
    }
}
