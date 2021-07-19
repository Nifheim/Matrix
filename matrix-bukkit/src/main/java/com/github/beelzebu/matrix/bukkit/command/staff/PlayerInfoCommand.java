package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bukkit.command.MatrixCommand;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.google.gson.GsonBuilder;
import java.util.Collection;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PlayerInfoCommand extends MatrixCommand {

    public PlayerInfoCommand() {
        super("playerinfo", "matrix.command.pinfo", false, "pinfo", "lookup");
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
        Matrix.getAPI().getPlugin().getBootstrap().getScheduler().executeAsync(() -> {
            if (args.length == 0) {
                sender.sendMessage(StringUtils.replace("%prefix% &6Por favor usa &e/" + getName() + " <nombre>"));
            } else {
                MatrixPlayer player = Matrix.getAPI().getPlayerManager().getPlayerByName(args[0]).join();
                if (player != null) {
                    sender.sendMessage(TextComponent.fromLegacyText(new GsonBuilder().setPrettyPrinting().create().toJson(player, MongoMatrixPlayer.class)));
                } else {
                    sender.sendMessage(StringUtils.replace("%prefix% No se ha encontrado a " + args[0] + " en la base de datos."));
                }
            }
        });
    }

    private @NotNull String createList(@NotNull Collection<String> collection) {
        StringBuilder list = new StringBuilder();
        collection.forEach(entry -> list.append(StringUtils.replace("  &f- &7" + entry + "\n")));
        return list.toString();
    }
}
