package com.github.beelzebu.matrix.api.command;

import com.github.beelzebu.matrix.api.Matrix;
import java.util.ArrayList;
import java.util.Arrays;
import net.md_5.bungee.api.ChatColor;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public abstract class MatrixCommand extends RegistrableCommand {

    private final String permission;
    private boolean async;

    public MatrixCommand(String command, String permission, String... aliases) {
        this(command, permission, true, aliases);
    }

    public MatrixCommand(String command, String permission, boolean async, String... aliases) {
        super((Plugin) Matrix.getAPI().getPlugin().getBootstrap(), command, permission, async, aliases);
        if (async) {
            Matrix.getLogger().info("Registering async command: " + command);
        }
        if (permission != null) {
            setPermission(permission);
        }
        setAliases(new ArrayList<>(Arrays.asList(aliases)));
        this.permission = permission;
        this.async = async;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (sender instanceof Player && permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo tienes permisos para usar este comando."));
            return true;
        }
        if (async && Bukkit.isPrimaryThread()) {
            Matrix.getAPI().getPlugin().getBootstrap().getScheduler().executeAsync(() -> onCommand(sender, args));
        } else {
            Matrix.getAPI().getPlugin().getBootstrap().getScheduler().executeSync(() -> onCommand(sender, args));
        }
        return true;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
}
