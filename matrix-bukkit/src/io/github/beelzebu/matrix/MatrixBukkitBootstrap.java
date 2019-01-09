package io.github.beelzebu.matrix;

import de.slikey.effectlib.EffectManager;
import io.github.beelzebu.matrix.api.commands.CommandAPI;
import io.github.beelzebu.matrix.api.menus.GUIManager;
import io.github.beelzebu.matrix.api.player.Statistics;
import io.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.api.server.powerup.tasks.PowerupSpawnTask;
import io.github.beelzebu.matrix.commands.staff.Freeze;
import io.github.beelzebu.matrix.commands.staff.LaunchPads;
import io.github.beelzebu.matrix.commands.staff.Powerups;
import io.github.beelzebu.matrix.commands.staff.Reload;
import io.github.beelzebu.matrix.commands.user.Options;
import io.github.beelzebu.matrix.commands.user.Spit;
import io.github.beelzebu.matrix.commands.utils.AddLore;
import io.github.beelzebu.matrix.commands.utils.Matrix;
import io.github.beelzebu.matrix.commands.utils.RemoveLore;
import io.github.beelzebu.matrix.commands.utils.Rename;
import io.github.beelzebu.matrix.commands.utils.SyncCommand;
import io.github.beelzebu.matrix.config.BukkitConfiguration;
import io.github.beelzebu.matrix.listeners.DupepatchListener;
import io.github.beelzebu.matrix.listeners.GUIListener;
import io.github.beelzebu.matrix.listeners.InternalListener;
import io.github.beelzebu.matrix.listeners.ItemListener;
import io.github.beelzebu.matrix.listeners.LobbyListener;
import io.github.beelzebu.matrix.listeners.PlayerCommandPreprocessListener;
import io.github.beelzebu.matrix.listeners.PlayerDeathListener;
import io.github.beelzebu.matrix.listeners.PlayerJoinListener;
import io.github.beelzebu.matrix.listeners.PlayerQuitListener;
import io.github.beelzebu.matrix.listeners.StatsListener;
import io.github.beelzebu.matrix.listeners.ViewDistanceListener;
import io.github.beelzebu.matrix.listeners.VotifierListener;
import io.github.beelzebu.matrix.utils.ReadURL;
import io.github.beelzebu.matrix.utils.bungee.BungeeCleanupTask;
import io.github.beelzebu.matrix.utils.bungee.BungeeServerTracker;
import io.github.beelzebu.matrix.utils.placeholders.StatsPlaceholders;
import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MatrixBukkitBootstrap extends JavaPlugin implements MatrixBootstrap {

    private MatrixAPIImpl api;
    @Setter
    private boolean chatMuted = false;
    private EffectManager effectManager;
    private BukkitConfiguration configuration;
    private MatrixPluginBukkit matrixPlugin;

    @Override
    public void onLoad() {
        configuration = new BukkitConfiguration(new File(getDataFolder(), "config.yml"));
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            api.getPlayer(p.getUniqueId()).setStatistics(api.getPlayer(p.getUniqueId()).getStatistics(api.getServerInfo().getServerName()).orElseGet(() -> new Statistics(api.getServerInfo().getServerName(), p.getStatistic(Statistic.PLAYER_KILLS), p.getStatistic(Statistic.MOB_KILLS), p.getStatistic(Statistic.DEATHS), StatsListener.getBroken().getOrDefault(p.getUniqueId(), 0), StatsListener.getPlaced().getOrDefault(p.getUniqueId(), 0))));
            UUID inv = GUIManager.getOpenInventories().get(p.getUniqueId());
            if (inv != null) {
                p.closeInventory();
            }
        });
        api.shutdown();
    }

    @Override
    public void onEnable() {
        io.github.beelzebu.matrix.api.Matrix.setAPI(api = new MatrixBukkitAPI(matrixPlugin = new MatrixPluginBukkit(this)));
        api.setup();
        // Load things
        loadManagers();

        // Register events
        registerEvents(new GUIListener());
        registerEvents(new InternalListener());
        if (api.getServerInfo().getServerType().equals(ServerType.LOBBY) || api.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA)) {
            registerEvents(new ItemListener());
            registerEvents(new LobbyListener(this));
        }
        registerEvents(new PlayerCommandPreprocessListener(this));
        registerEvents(new PlayerDeathListener(this));
        registerEvents(new PlayerJoinListener(this));
        registerEvents(new PlayerQuitListener(this));
        if (api.getServerInfo().getServerType().equals(ServerType.SURVIVAL)) {
            registerEvents(new DupepatchListener(this));
            registerEvents(new StatsListener());
            registerEvents(new ViewDistanceListener(this));
        }
        if (isVotifier()) {
            registerEvents(new VotifierListener(this));
        }
        // Register commands
        CommandAPI.registerCommand(this, new Freeze());
        CommandAPI.registerCommand(this, new Reload());
        CommandAPI.registerCommand(this, new Options());
        CommandAPI.registerCommand(this, new Powerups());
        CommandAPI.registerCommand(this, new LaunchPads());
        CommandAPI.registerCommand(this, new RemoveLore());
        CommandAPI.registerCommand(this, new AddLore());
        CommandAPI.registerCommand(this, new Rename());
        CommandAPI.registerCommand(this, new Matrix());
        CommandAPI.registerCommand(this, new Spit());
        CommandAPI.registerCommand(this, new SyncCommand());

        Bukkit.getOnlinePlayers().forEach((p) -> Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + p.getName() + "&clave=" + getConfig().getString("clave"));
            } catch (Exception ex) {
                Logger.getLogger(MatrixBukkitBootstrap.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", p.getName());
            }
        }));
        BungeeServerTracker.startTask(5);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BungeeCleanupTask(), 600, 600);
        if (!api.getServerInfo().getServerType().equals(ServerType.SURVIVAL)) {
            Bukkit.getScheduler().runTaskTimer(this, new PowerupSpawnTask(), 0, 1200);
        }
    }

    public Boolean isVotifier() {
        if (Bukkit.getPluginManager().getPlugin("Votifier") != null) {
            return Bukkit.getPluginManager().getPlugin("Votifier").isEnabled();
        }
        return false;
    }

    private void loadManagers() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                api.log("LuckPerms found, hooking into it.");
            } else {
                Bukkit.shutdown();
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            api.log("PlaceholderAPI found, hooking into it.");
            // Utils
            StatsPlaceholders statsPlaceholders = new StatsPlaceholders(this);
            statsPlaceholders.hook();
        }
        effectManager = new EffectManager(this);
    }

    private void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
}
