package io.github.beelzebu.matrix.command;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import io.github.beelzebu.matrix.MatrixBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class Maintenance extends Command {

    private final MatrixBungee plugin;

    public Maintenance(MatrixBungee plugin) {
        super("maintenance", "matrix.staff.admin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(TextComponent.fromLegacyText("§4§lEl estado de mantenimiento fue cambiado a:§a " + !plugin.isMaintenance()));
        RedisBungee.getApi().sendChannelMessage("Maintenance", "switch");
    }
}
