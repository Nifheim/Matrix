package io.github.beelzebu.matrix.command;

import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.utils.PermsUtils;
import java.text.SimpleDateFormat;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class PlayerInfo extends Command {

    private final MatrixAPI core = MatrixAPI.getInstance();
    private final String name;

    public PlayerInfo(String cmd) {
        super(cmd);
        name = cmd;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        core.getMethods().runAsync(() -> {
            if (sender.hasPermission("matrix.staff.admin")) {
                if (args.length == 0) {
                    sender.sendMessage(core.rep("%prefix% &6Por favor usa &e/" + name + " <nombre>"));
                } else if (args.length == 1) {
                    if (core.getRedis().isRegistred(args[0])) {
                        Punishment ban = PunishmentManager.get().getBan(UUIDManager.get().getUUID(args[0]));
                        try (Jedis jedis = core.getRedis().getPool().getResource()) {
                            JsonObject data = core.getGson().fromJson(jedis.hget("ncore_data", core.getUUID(args[0]).toString()), JsonObject.class);
                            BaseComponent[] msg = TextComponent.fromLegacyText(core.rep(
                                    "%prefix% Información de &c" + args[0] + "&r\n"
                                            + " \n"
                                            + " &cUUID &8• &7" + core.getUUID(args[0]) + "&r\n"
                                            + " &cNick &8• &7" + data.get("displayname").getAsString() + "&r\n"
                                            + " &cNivel &8• &7" + data.get("level").getAsInt() + "&r\n"
                                            + " &cExperiencia &8• &7" + data.get("exp").getAsInt() + "&r\n"
                                            + " &cRango &8• &7" + (PermsUtils.getPrefix(core.getUUID(args[0])).equals("§3") ? "§3Usuario" : PermsUtils.getPrefix(core.getUUID(args[0]))) + "&r\n"
                                            + " &cÚltimo login &8• &7" + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(data.get("lastlogin").getAsLong()) + "&r\n"
                                            + " &cBaneado &8• &7" + (ban != null ? "si" : "no") + "&r\n"
                                            + (ban != null ? "   &cRazón &8• &7" + ban.getReason() + "&r\n" : "")
                                            + " &cIP &8• &7" + (data.get("ip") != null ? data.get("ip").getAsString() : "no registrada") + "&r\n"
                            ));
                            sender.sendMessage(msg);
                        } catch (JedisException ex) {
                        }
                    } else {
                        sender.sendMessage(core.rep("%prefix% No se ha encontrado a " + args[0] + " en la base de datos."));
                    }
                }
            }
        });
    }
}
