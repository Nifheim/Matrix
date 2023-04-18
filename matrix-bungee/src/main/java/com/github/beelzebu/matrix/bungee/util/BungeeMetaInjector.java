package com.github.beelzebu.matrix.bungee.util;

import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.util.MetaInjector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class BungeeMetaInjector extends MetaInjector<ProxiedPlayer> implements Listener {

    private final Map<UUID, Map<String, Object>> cachedMeta = new HashMap<>();
    private final @NotNull MatrixBungeeAPI api;

    public BungeeMetaInjector(@NotNull MatrixBungeeAPI api) {
        this.api = api;
        ProxyServer.getInstance().getPluginManager().registerListener(api.getPlugin().getBootstrap(), this);
    }

    @Override
    public <T> void setMeta(@NotNull ProxiedPlayer player, @NotNull String key, T meta) {
        Map<String, Object> playerMeta = cachedMeta.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>());
        playerMeta.put(key, meta);
        cachedMeta.put(player.getUniqueId(), playerMeta);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getMeta(@NotNull ProxiedPlayer player, @NotNull String key, @NotNull Class<T> clazz) {
        Map<String, Object> meta = cachedMeta.get(player.getUniqueId());
        if (meta == null) {
            return null;
        }
        return (T) meta.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull Collection<T> getMeta(@NotNull ProxiedPlayer player, @NotNull Class<T> clazz) {
        return (Collection<T>) cachedMeta.get(player.getUniqueId()).values().stream().filter(clazz::isInstance).collect(Collectors.toList());
    }

    @EventHandler
    public void onDisconnect(@NotNull PlayerDisconnectEvent e) {
        ProxiedPlayer proxiedPlayer = e.getPlayer();
        ProxyServer.getInstance().getScheduler().schedule(api.getPlugin().getBootstrap(), () -> {
            if (proxiedPlayer.isConnected()) {
                return;
            }
            cachedMeta.remove(proxiedPlayer.getUniqueId());
        }, 1, TimeUnit.MINUTES);
    }
}
