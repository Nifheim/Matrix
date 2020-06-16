package com.github.beelzebu.matrix.command.user;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.menus.OptionsGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class OptionsCommand extends MatrixCommand {

    public OptionsCommand() {
        super("options", null, false, "opciones");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            new OptionsGUI(api.getPlayer(((Player) sender).getUniqueId())).open((Player) sender);
        }
    }
}
