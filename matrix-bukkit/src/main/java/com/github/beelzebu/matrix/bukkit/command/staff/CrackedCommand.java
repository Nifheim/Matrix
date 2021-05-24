package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import net.md_5.bungee.api.chat.TextComponent;
import net.nifheim.bukkit.util.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class CrackedCommand extends MatrixCommand {

    public CrackedCommand() {
        super("cracked", "matrix.command.cracked");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length != 1) {
            sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_TARGET, I18n.DEFAULT_LOCALE)));
            return;
        }
        String name = args[0];
        sender.sendMessage("Buscando la cuenta de " + name + "...");
        Matrix.getAPI().getPlayerManager().getPlayerByName(name).thenAccept(matrixPlayer -> {
            if (matrixPlayer == null) {
                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_TARGET, I18n.DEFAULT_LOCALE).replace("%target%", name)));
                return;
            }
            sender.sendMessage("La cuenta de " + matrixPlayer.getName() + " será establecida como no premium.");
            matrixPlayer.setPremium(false);
            sender.sendMessage("La cuenta de " + matrixPlayer.getName() + " fue establecida como no premium.");
        });
    }
}
