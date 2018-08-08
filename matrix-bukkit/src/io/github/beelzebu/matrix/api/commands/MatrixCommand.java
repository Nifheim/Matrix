package io.github.beelzebu.matrix.api.commands;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.Messages;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class MatrixCommand extends Command {

    protected final MatrixAPI api = Matrix.getAPI();
    protected final String perm;

    public MatrixCommand(String command, String permission, String... aliases) {
        super(command);
        setPermission(permission);
        setAliases(Arrays.asList(aliases));
        perm = permission;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (perm == null || sender.hasPermission(perm)) {
            if (Bukkit.isPrimaryThread()) {
                api.getPlugin().runAsync(() -> onCommand(sender, args));
            } else {
                onCommand(sender, args);
            }
        } else {
            sender.sendMessage(api.getString(Messages.GENERAL_NO_PERMS, sender instanceof Player ? ((Player) sender).getLocale() : ""));
            //else {
        }
        //  sender.sendMessage(api.rep("&c&lHey!&7 Debes ser rango &c" + perm.split("\\.")[perm.split(".").length - 1] + "&7 o superior para usar este comando."));
        //}
        return true;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
}
