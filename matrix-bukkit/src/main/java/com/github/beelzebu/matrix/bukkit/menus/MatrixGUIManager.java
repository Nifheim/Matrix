package com.github.beelzebu.matrix.bukkit.menus;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.nifheim.bukkit.util.menu.BaseMenu;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class MatrixGUIManager {

    private static MatrixGUIManager instance;
    private final Map<UUID, Map<Class<? extends BaseMenu>, WeakReference<BaseMenu>>> guiMap = new HashMap<>();

    public static MatrixGUIManager getInstance() {
        return Objects.requireNonNull(instance, "Instance is not provided yet");
    }

    public MatrixGUIManager(Plugin plugin) {
        if (instance != null) {
            throw new RuntimeException("Instance already set");
        }
        Bukkit.getPluginManager().registerEvents(new GUIManagerListener(), plugin);
        instance = this;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseMenu> T getGUI(UUID uniqueId, Class<T> guiClazz) {
        Map<Class<? extends BaseMenu>, WeakReference<BaseMenu>> classWeakReferenceMap = guiMap.computeIfAbsent(uniqueId, k -> new HashMap<>());
        T gui = null;
        try {
            gui = (T) classWeakReferenceMap.getOrDefault(guiClazz, new WeakReference<>(guiClazz.getConstructor(MatrixPlayer.class).newInstance(Matrix.getAPI().getPlayerManager().getPlayer(uniqueId).join()))).get();
            classWeakReferenceMap.put(guiClazz, new WeakReference<>(gui = guiClazz.getConstructor(MatrixPlayer.class).newInstance(Matrix.getAPI().getPlayerManager().getPlayer(uniqueId).join())));
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
