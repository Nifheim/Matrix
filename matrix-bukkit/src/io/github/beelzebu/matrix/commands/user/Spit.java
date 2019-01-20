package io.github.beelzebu.matrix.commands.user;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;

public class Spit extends MatrixCommand {

    public Spit() {
        super("spit", "matrix.command.spit", "escupir");
        async = false;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.launchProjectile(LlamaSpit.class, p.getEyeLocation().getDirection().normalize());
        }
    }
}
