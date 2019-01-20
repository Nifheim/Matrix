package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class PluginsCommand extends MatrixCommand {

    public PluginsCommand() {
        super("plugins", null, "pl", "bukkit:plugins", "bukkit:pl");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(Matrix.getAPI().rep("Plugins (12): &aCoins&f, &aDamageIndicator&f, &bEssentialsXL&f, &aFactionsUtils&f, &aLoncoUtils&f, &aLuckPerms&f, &bMatrix-Bukkit&f, &bMatrix-Chat&f, &aNametagEdit&f, &aPlaceholderAPI&f, &aProtocolLib&f, &aViaVersion"));
        } else {
            Bukkit.dispatchCommand(sender, "plugman list");
        }
    }
}
