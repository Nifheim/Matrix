package com.github.beelzebu.matrix.api.command;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.MatrixAPI;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public abstract class MatrixCommand extends Command {

    protected final MatrixAPI api = Matrix.getAPI();
    protected final String permission;
    private boolean async;

    public MatrixCommand(String command, String permission, String... aliases) {
        this(command, permission, true, aliases);
    }

    public MatrixCommand(String command, String permission, boolean async, String... aliases) {
        super(command);
        if (async) {
            Matrix.getLogger().info("Registering async command: " + command);
        }
        setPermission(permission);
        setAliases(new ArrayList<>(Arrays.asList(aliases)));
        this.permission = permission;
        this.async = async;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (permission == null || sender.hasPermission(permission)) {
            if (async && Bukkit.isPrimaryThread()) {
                api.getPlugin().runAsync(() -> onCommand(sender, args));
            } else {
                onCommand(sender, args);
            }
        } else {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_PERMS, sender instanceof Player ? api.getPlayer(((Player) sender).getUniqueId()).getLastLocale() : I18n.DEFAULT_LOCALE));
        }
        return true;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
}
