package com.github.beelzebu.matrix.command.user;

import com.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;

public class SpitCommand extends MatrixCommand {

    public SpitCommand() {
        super("spit", "matrix.command.spit", false, "escupir");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.launchProjectile(LlamaSpit.class, p.getEyeLocation().getDirection().normalize());
        }
    }
}
