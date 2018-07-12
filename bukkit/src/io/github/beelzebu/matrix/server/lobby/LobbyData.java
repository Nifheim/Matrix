package io.github.beelzebu.matrix.server.lobby;

import io.github.beelzebu.matrix.Main;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public class LobbyData {

    private static LobbyData instance;
    private final Main plugin = Main.getInstance();
    private final YamlConfiguration data;
    @Getter
    private final List<LaunchPad> launchpads;

    private LobbyData() {
        data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));
        launchpads = new ArrayList<>();
        data.getStringList("LaunchPads").forEach((lp) -> {
            launchpads.add(LaunchPad.fromString(lp));
        });
    }

    public static LobbyData getInstance() {
        return instance != null ? instance : (instance = new LobbyData());
    }

    public FileConfiguration getConfig() {
        return data;
    }

    public void saveConfig() {
        try {
            data.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (IOException ex) {
            Logger.getLogger(LobbyData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Location getSpawn() {
        String[] coords = data.getString("Spawn.Location").split(",");
        return new Location(Bukkit.getWorld(data.getString("Spawn.World")), Double.valueOf(coords[0]), Double.valueOf(coords[1]), Double.valueOf(coords[2]), Float.valueOf(coords[3]), Float.valueOf(coords[4]));
    }

    public void createLaunchpad(Location loc, Vector vec) {
        List<String> launchp = data.getStringList("LaunchPads");
        launchp.add(new LaunchPad(loc, vec, true).toString());
        data.set("LaunchPads", launchp);
        saveConfig();
    }
}
