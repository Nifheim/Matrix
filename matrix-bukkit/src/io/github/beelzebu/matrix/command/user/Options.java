package io.github.beelzebu.matrix.command.user;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import io.github.beelzebu.matrix.menus.OptionsGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Options extends MatrixCommand {

    public Options() {
        super("options", null, "opciones");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            new OptionsGUI((Player) sender, api.getString("Options.Title", ((Player) sender).getLocale())).open((Player) sender);
        }
    }
}
