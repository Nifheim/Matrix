package com.github.beelzebu.matrix.command.staff;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.api.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author Beelzebu
 */
public class LaunchPadsCommand extends MatrixCommand {

    private final LobbyData data = LobbyData.getInstance();

    public LaunchPadsCommand() {
        super("launchpad", "matrix.staff.admin");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            Location loc = p.getLocation();
            if (args[0].equalsIgnoreCase("create")) {
                data.createLaunchpad(loc, new Vector(0, 0, 0));
                p.sendMessage(StringUtils.replace("%prefix% LaunchPad creado"));
            }
        }
    }
}
