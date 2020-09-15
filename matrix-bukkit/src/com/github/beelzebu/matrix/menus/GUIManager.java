package com.github.beelzebu.matrix.menus;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.menu.BaseGUI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public final class GUIManager {

    private static GUIManager instance;

    public static GUIManager getInstance() {
        return instance == null ? instance = new GUIManager() : instance;
    }

    private final Map<UUID, Map<Class<? extends BaseGUI>, WeakReference<BaseGUI>>> guiMap = new HashMap<>();

    private GUIManager() {
        Bukkit.getPluginManager().registerEvents(new GUIManagerListener(), (Plugin) Matrix.getAPI().getPlugin().getBootstrap());
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseGUI> T getGUI(UUID uniqueId, Class<T> guiClazz) {
        Map<Class<? extends BaseGUI>, WeakReference<BaseGUI>> classWeakReferenceMap = guiMap.computeIfAbsent(uniqueId, k -> new HashMap<>());
        T gui = null;
        try {
            gui = (T) classWeakReferenceMap.getOrDefault(guiClazz, new WeakReference<>(guiClazz.getConstructor(MatrixPlayer.class).newInstance(Matrix.getAPI().getPlayer(uniqueId)))).get();
            classWeakReferenceMap.put(guiClazz, new WeakReference<>(gui = guiClazz.getConstructor(MatrixPlayer.class).newInstance(Matrix.getAPI().getPlayer(uniqueId))));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return gui;
    }

    private class GUIManagerListener implements Listener {

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent e) {
            guiMap.remove(e.getPlayer().getUniqueId());
        }

        @EventHandler
        public void onPlayerQuit(PlayerLocaleChangeEvent e) {
            guiMap.remove(e.getPlayer().getUniqueId());
        }
    }
}
