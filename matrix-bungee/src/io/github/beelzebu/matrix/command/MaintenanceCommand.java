package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MaintenanceCommand extends Command {

    private final MatrixBungeeBootstrap plugin;

    public MaintenanceCommand(MatrixBungeeBootstrap plugin) {
        super("maintenance", "matrix.admin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean status = !plugin.isMaintenance();
        sender.sendMessage(TextComponent.fromLegacyText("§4§lEl estado de mantenimiento fue cambiado a:§a " + status));
        plugin.setMaintenance(status);
    }
}
