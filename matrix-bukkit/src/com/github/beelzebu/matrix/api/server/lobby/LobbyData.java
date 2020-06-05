package com.github.beelzebu.matrix.api.server.lobby;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public class LobbyData {

    private static LobbyData instance;
    private final MatrixBukkitBootstrap plugin = MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class);
    private final FileConfiguration config;
    private final List<LaunchPad> launchpads;

    private LobbyData() {
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));
        launchpads = new ArrayList<>();
        config.getStringList("LaunchPadsCommand").forEach(lp -> launchpads.add(LaunchPad.fromString(lp)));
    }

    public static LobbyData getInstance() {
        return instance != null ? instance : (instance = new LobbyData());
    }

    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (IOException ex) {
            Logger.getLogger(LobbyData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createLaunchpad(Location loc, Vector vec) {
        List<String> launchp = config.getStringList("LaunchPadsCommand");
        launchp.add(new LaunchPad(loc, vec, true).toString());
        config.set("LaunchPadsCommand", launchp);
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<LaunchPad> getLaunchpads() {
        return launchpads;
    }
}
