package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class RegisterHumildeCommand extends Command {

    public RegisterHumildeCommand() {
        super("registerhumilde", "martix.admin");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        try {
            Files.readAllLines(new File(Matrix.getAPI().getPlugin().getDataFolder(), "miembros.txt").toPath()).forEach(line -> {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lp user " + line + " parent add humilde humildadcraft");
                Matrix.getAPI().getDatabase().getPlayerByName(line).thenAccept(matrixPlayer -> {
                    if (matrixPlayer == null) {
                        matrixPlayer = new MongoMatrixPlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + line).getBytes()), line);
                    }
                    ((MongoMatrixPlayer) matrixPlayer).setRegistration(new Date());
                    matrixPlayer.setPremium(true);
                    matrixPlayer.setRegistered(true);
                    matrixPlayer.save().join();
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
