package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.MatrixAPIImpl;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MaintenanceCommand extends Command {

    private final MatrixAPIImpl api;

    public MaintenanceCommand(MatrixAPIImpl api) {
        super("maintenance", "matrix.admin");
        this.api = api;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean status = !api.getMaintenanceManager().isMaintenance();
        sender.sendMessage(TextComponent.fromLegacyText("§4§lEl estado de mantenimiento fue cambiado a:§a " + status));
        api.getMaintenanceManager().setMaintenance(status);
    }
}
