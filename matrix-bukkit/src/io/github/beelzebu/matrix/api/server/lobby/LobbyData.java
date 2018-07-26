package io.github.beelzebu.matrix.api.server.lobby;

import io.github.beelzebu.matrix.Main;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public class LobbyData {

    private static LobbyData instance;
    private final Main plugin = Main.getInstance();
    @Getter
    private final FileConfiguration config;
    @Getter
    private final List<LaunchPad> launchpads;

    private LobbyData() {
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "data.yml"));
        launchpads = new ArrayList<>();
        config.getStringList("LaunchPads").forEach(lp -> launchpads.add(LaunchPad.fromString(lp)));
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
        List<String> launchp = config.getStringList("LaunchPads");
        launchp.add(new LaunchPad(loc, vec, true).toString());
        config.set("LaunchPads", launchp);
        saveConfig();
    }
}
