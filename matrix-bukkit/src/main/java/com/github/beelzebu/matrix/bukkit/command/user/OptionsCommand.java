package com.github.beelzebu.matrix.bukkit.command.user;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.bukkit.menus.GUIManager;
import com.github.beelzebu.matrix.bukkit.menus.OptionsGUI;
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
            Player player = (Player) sender;
            GUIManager.getInstance().getGUI(player.getUniqueId(), OptionsGUI.class).open(player);
        }
    }
}
