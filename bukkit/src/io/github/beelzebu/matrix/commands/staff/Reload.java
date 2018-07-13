package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;

public class Reload extends MatrixCommand {

    public Reload() {
        super("ncreload", "matrix.staff.admin");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        core.getConfig().reload();
        core.getMessagesMap().entrySet().forEach(ent -> ent.getValue().reload());
    }
}
