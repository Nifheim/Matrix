package com.github.beelzebu.matrix.command.user;

import com.github.beelzebu.matrix.api.commands.MatrixCommand;
import com.github.beelzebu.matrix.menus.OptionsGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OptionsCommand extends MatrixCommand {

    public OptionsCommand() {
        super("options", null, false, "opciones");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            new OptionsGUI((Player) sender, api.getString("Options.Title", ((Player) sender).getLocale())).open((Player) sender);
        }
    }
}
