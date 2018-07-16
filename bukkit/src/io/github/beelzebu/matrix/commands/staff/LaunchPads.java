package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import io.github.beelzebu.matrix.api.server.lobby.LobbyData;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LaunchPads extends MatrixCommand {

    private final LobbyData data = LobbyData.getInstance();

    public LaunchPads() {
        super("launchpad", "matrix.staff.admin");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            Location loc = p.getLocation();
            if (args[0].equalsIgnoreCase("create")) {
                data.createLaunchpad(loc, new Vector(0, 0, 0));
                p.sendMessage(api.rep("%prefix% LaunchPad creado"));
            }
        }
    }
}
