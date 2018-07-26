package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.networkxp.NetworkXP;
import io.github.beelzebu.matrix.utils.PermsUtils;
import java.text.SimpleDateFormat;
import java.util.Collection;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class PlayerInfo extends Command {

    private final MatrixAPI api = Matrix.getAPI();
    private final String name;

    public PlayerInfo(String cmd) {
        super(cmd);
        name = cmd;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        api.getPlugin().runAsync(() -> {
            if (sender.hasPermission("matrix.staff.admin")) {
                if (args.length == 0) {
                    sender.sendMessage(api.rep("%prefix% &6Por favor usa &e/" + name + " <nombre>"));
                } else if (args.length == 1) {
                    if (api.getDatabase().isRegistered(args[0])) {
                        Punishment ban = PunishmentManager.get().getBan(UUIDManager.get().getUUID(args[0]));
                        MatrixPlayer player = api.getPlayer(args[0]);
                        BaseComponent[] msg = TextComponent.fromLegacyText(api.rep(
                                "%prefix% Información de &c" + player.getName() + "&r\n"
                                        + " \n"
                                        + " &cUUID &8• &7" + player.getUniqueId() + "&r\n"
                                        + " &cDisplayname &8• &7" + player.getDisplayname() + "&r\n"
                                        + " &cNivel &8• &7" + NetworkXP.getLevelForXP(player.getExp()) + "&r\n"
                                        + " &cExperiencia &8• &7" + player.getExp() + "&r\n"
                                        + " &cRango &8• &7" + (PermsUtils.getPrefix(player.getUniqueId()).length() < 3 ? "Usuario" : PermsUtils.getPrefix(player.getUniqueId())) + "&r\n"
                                        + " &cÚltimo login &8• &7" + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(player.getLastLogin().getTime()) + "&r\n"
                                        + " &cBaneado &8• &7" + (ban != null ? "si" : "no") + "&r\n"
                                        + (ban != null ? "   &cRazón &8• &7" + ban.getReason() + "&r\n" : "")
                                        + " &cIP &8• &7" + (player.getIP() != null ? player.getIP() : (player.getIpHistory().stream().findFirst().isPresent() ? player.getIpHistory().stream().findFirst().get() : "no registrada")) + "&r\n"
                                        + " &cHistorial de IP(s) &8• &r\n" + createList(player.getIpHistory())
                        ));
                        sender.sendMessage(msg);
                    } else {
                        sender.sendMessage(api.rep("%prefix% No se ha encontrado a " + args[0] + " en la base de datos."));
                    }
                }
            }
        });
    }

    private String createList(Collection<String> collection) {
        StringBuilder list = new StringBuilder();
        collection.forEach(entry -> list.append(api.rep("  &f- &7" + entry + "\n")));
        return list.toString();
    }
}
