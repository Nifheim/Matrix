package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.bungee.motd.MotdManager;
import com.github.beelzebu.matrix.bungee.tablist.TablistManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

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
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            if ("reload".equals(args[0])) {
                MotdManager.onEnable();
                bungeeBootstrap.getInfluencerManager().reloadInfluencers();
                bungeeBootstrap.getApi().reload();
                TablistManager.init();
            }
        }

    }
}
