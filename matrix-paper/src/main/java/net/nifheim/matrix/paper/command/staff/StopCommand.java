package net.nifheim.matrix.paper.command.staff;

import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerType;
import net.nifheim.matrix.paper.MatrixPaper;
import net.nifheim.matrix.paper.command.MatrixCommand;
import net.nifheim.matrix.paper.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class StopCommand extends MatrixCommand {

    private final MatrixPaper plugin;
    private final ServerInfo serverInfo;

    public StopCommand(MatrixPaper plugin, ServerInfo serverInfo) {
        super(plugin, "stop", "matrix.admin", true, "minecraft:stop", "bukkit:stop", "spigot:stop", "restart", "spigot:restart");
        this.plugin = plugin;
        this.serverInfo = serverInfo;
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            String lobby = serverInfo.getDefaultLobbyName();
            if (lobby == null) {
                lobby = "limbo";
            }
            if (serverInfo.getServerType() != ServerType.LOBBY && serverInfo.getServerType() != ServerType.AUTH) {
                Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> Bukkit.setWhitelist(true));
                while (true) {
                    if (!Bukkit.getOnlinePlayers().isEmpty()) {
                        try {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                BungeeUtil.move(player, lobby);
                            }
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
                Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> Bukkit.setWhitelist(false));
            }
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> Bukkit.shutdown());
        }
    }
}
