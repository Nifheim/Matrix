package net.nifheim.matrix.bungee.command;

import net.nifheim.matrix.common.api.MatrixCommon;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

public class MaintenanceCommand extends Command {

    private final MatrixCommon api;

    public MaintenanceCommand(MatrixCommon api) {
        super("maintenance", "matrix.admin");
        this.api = api;
    }

    @Override
    public void execute(@NotNull CommandSender sender, String[] args) {
        boolean status = !api.getMaintenanceManager().isMaintenance();
        sender.sendMessage(TextComponent.fromLegacyText("§4§lEl estado de mantenimiento fue cambiado a:§a " + status));
        api.getMaintenanceManager().setMaintenance(status);
    }
}
