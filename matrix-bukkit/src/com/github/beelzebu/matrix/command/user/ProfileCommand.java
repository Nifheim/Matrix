package com.github.beelzebu.matrix.command.user;

import com.github.beelzebu.matrix.api.commands.MatrixCommand;
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
            new ProfileGUI(player, api.getString("Social.Profile.Name", player.getLocale())).open(player);
        }
    }
}
