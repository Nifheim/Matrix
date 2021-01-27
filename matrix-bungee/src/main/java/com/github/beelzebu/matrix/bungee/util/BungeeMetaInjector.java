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

/**
 * @author Beelzebu
 */
public class BungeeMetaInjector extends MetaInjector<ProxiedPlayer> implements Listener {

    private final Map<UUID, Map<String, Object>> cachedMeta = new HashMap<>();
    private final MatrixBungeeAPI api;

    public BungeeMetaInjector(MatrixBungeeAPI api) {
        this.api = api;
        ProxyServer.getInstance().getPluginManager().registerListener(api.getPlugin().getBootstrap(), this);
    }

    @Override
    public <T> void setMeta(ProxiedPlayer player, String key, T meta) {
        Map<String, Object> playerMeta = cachedMeta.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>());
        playerMeta.put(key, meta);
        cachedMeta.put(player.getUniqueId(), playerMeta);
    }

    @Override
    public <T> T getMeta(ProxiedPlayer player, String key, Class<T> clazz) {
        Map<String, Object> meta = cachedMeta.get(player.getUniqueId());
        if (meta == null) {
            return null;
        }
        return (T) meta.get(key);
    }

    @Override
    public <T> Collection<T> getMeta(ProxiedPlayer player, Class<T> clazz) {
        return (Collection<T>) cachedMeta.get(player.getUniqueId()).values().stream().filter(clazz::isInstance).collect(Collectors.toList());
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer proxiedPlayer = e.getPlayer();
        ProxyServer.getInstance().getScheduler().schedule(api.getPlugin().getBootstrap(), () -> {
            if (proxiedPlayer.isConnected()) {
                return;
            }
            cachedMeta.remove(proxiedPlayer.getUniqueId());
        }, 1, TimeUnit.MINUTES);
    }
}
