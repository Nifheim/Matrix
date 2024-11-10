package net.nifheim.matrix.paper.command.user;

import net.nifheim.matrix.paper.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author Jaime Suárez
 */
public class SpitCommand extends MatrixCommand {

    public SpitCommand(Plugin plugin) {
        super(plugin, "spit", "matrix.command.spit", false, "escupir");
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player player) {
            player.launchProjectile(LlamaSpit.class, player.getEyeLocation().getDirection().normalize());
        }
    }
}
