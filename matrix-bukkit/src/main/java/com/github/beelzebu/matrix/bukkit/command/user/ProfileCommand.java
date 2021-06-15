package com.github.beelzebu.matrix.bukkit.command.user;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.bukkit.menus.MatrixGUIManager;
import com.github.beelzebu.matrix.bukkit.menus.ProfileGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class ProfileCommand extends MatrixCommand {

    public ProfileCommand() {
        super("profile", null, false, "perfil");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            MatrixGUIManager.getInstance().getGUI(player.getUniqueId(), ProfileGUI.class).open(player);
        }
    }
}
