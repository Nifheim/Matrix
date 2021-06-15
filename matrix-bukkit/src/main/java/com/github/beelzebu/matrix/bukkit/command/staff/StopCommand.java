package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.bukkit.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class StopCommand extends MatrixCommand {

    public StopCommand() {
        super("stop", "matrix.admin", false, "minecraft:stop", "bukkit:stop", "spigot:stop", "restart", "spigot:restart");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Matrix.getAPI().getServerInfo().getLobbyServer().thenAcceptAsync(lobby -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BungeeUtil.move(player, lobby);
                }
                while (true) {
                    if (Bukkit.getOnlinePlayers().size() != 0) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
                Matrix.getAPI().getPlugin().getBootstrap().getScheduler().executeSync(Bukkit::shutdown);
            });
        }
    }
}
