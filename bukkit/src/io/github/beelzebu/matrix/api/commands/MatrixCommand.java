package io.github.beelzebu.matrix.api.commands;

import io.github.beelzebu.matrix.MatrixAPI;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class MatrixCommand extends Command {

    protected final MatrixAPI core = MatrixAPI.getInstance();
    protected final String perm;

    public MatrixCommand(String command, String permission, String... aliases) {
        super(command);
        setPermission(permission);
        setAliases(Arrays.asList(aliases));
        perm = permission;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (perm == null ? true : sender.hasPermission(perm)) {
            if (Bukkit.isPrimaryThread()) {
                core.getMethods().runAsync(() -> onCommand(sender, args));
            } else {
                onCommand(sender, args);
            }
        } else {
            sender.sendMessage(core.rep("&c&lHey!&7 Debes ser rango &c" + perm.split("\\.")[perm.split(".").length - 1] + "&7 o superior para usar este comando."));
        }
        return true;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
}
