package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.bungee.motd.MotdManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class MatrixCommand extends Command {

    private final MatrixBungeeBootstrap bungeeBootstrap;

    public MatrixCommand(MatrixBungeeBootstrap bungeeBootstrap) {
        super("bmatrix", "matrix.command.reload", "bungeematrix");
        this.bungeeBootstrap = bungeeBootstrap;
    }

    @Override
    public void execute(CommandSender sender, String @NotNull [] args) {
        if (args.length >= 1) {
            if ("reload".equals(args[0])) {
                bungeeBootstrap.getApi().reload();
                MotdManager.onEnable();
            }
        }
    }
}
