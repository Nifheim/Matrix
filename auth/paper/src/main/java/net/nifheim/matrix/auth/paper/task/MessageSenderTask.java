package net.nifheim.matrix.auth.paper.task;

import net.kyori.adventure.text.Component;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.auth.paper.command.LoginCommand;
import net.nifheim.matrix.auth.paper.command.RegisterCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageSenderTask implements Runnable {

    private final PlayerManager playerManager = MatrixProvider.getAPI().getPlayerManager();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MatrixPlayer matrixPlayer = playerManager.getPlayerSync(player.getUniqueId());
            if (matrixPlayer == null) {
                player.kick(Component.text("Error validating your session"));
                return;
            }
            if (matrixPlayer.isLoggedIn()) {
                continue;
            }
            if (matrixPlayer.isRegistered()) {
                player.sendMessage(Component.translatable("login.usage", LoginCommand.command()));
            } else {
                player.sendMessage(Component.translatable("register.usage", RegisterCommand.command()));
            }
        }
    }
}
