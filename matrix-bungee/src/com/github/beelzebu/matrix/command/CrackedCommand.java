package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class CrackedCommand extends Command {

    public CrackedCommand() {
        super("cracked", "matrix.command.cracked");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_TARGET, I18n.DEFAULT_LOCALE)));
            return;
        }
        String name = args[0];
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(name);
        if (matrixPlayer == null) {
            sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_TARGET, I18n.DEFAULT_LOCALE).replace("%target%", name)));
            return;
        }
        matrixPlayer.setPremium(false);
        sender.sendMessage("cracked");
    }
}
