package io.github.beelzebu.matrix.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.beelzebu.matrix.MatrixAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

public class Responder extends Command {

    private final MatrixAPI core = MatrixAPI.getInstance();

    public Responder() {
        super("responder", "matrix.staff.helper");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 1 && core.getMethods().isOnline(args[0])) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length - 1; i++) {
                sb.append(args[i]).append(" ");
            }
            sb.append(args[args.length - 1]);
            try (Jedis jedis = core.getRedis().getPool().getResource()) {
                String json = "{\"user\":\"" + core.getUUID(args[0]) + "\",\"bungee\":true,\"message\":\"&c" + (sender instanceof ProxiedPlayer ? core.getDisplayName(((ProxiedPlayer) sender).getUniqueId(), true) : "Consola") + "&f: " + sb.toString() + "\"}";
                try {
                    JsonObject test = core.getGson().fromJson(json, JsonObject.class);
                    test.get("user").getAsString();
                    System.out.print(test.toString());
                } catch (JsonParseException ex) {
                }
                jedis.publish("core-message", json);
                sender.sendMessage(core.rep("&6Haz enviado el siguiente mensaje a &7" + args[0]));
                sender.sendMessage(sb.toString());
            }
        }
    }
}
