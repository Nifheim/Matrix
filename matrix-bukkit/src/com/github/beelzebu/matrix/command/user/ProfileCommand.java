package com.github.beelzebu.matrix.command.user;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.menus.GUIManager;
import com.github.beelzebu.matrix.menus.ProfileGUI;
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
            GUIManager.getInstance().getGUI(player.getUniqueId(), ProfileGUI.class).open(player);
        }
    }
}
