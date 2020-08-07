package com.github.beelzebu.matrix.command.staff;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.server.lobby.LobbyData;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Beelzebu
 */
public class PowerupsCommand extends MatrixCommand {

    private final LobbyData data = LobbyData.getInstance();

    public PowerupsCommand() {
        super("powerups", null);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 5) {
            sender.sendMessage(StringUtils.replace("%prefix% Usa /powerups <mensaje> <PotionEffectType> <segundos> <amplificador> <chance>"));
            sender.sendMessage("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
        }
        if (sender instanceof Player) {
            String powerup = "";
            Location pLoc = ((Player) sender).getLocation();
            powerup += pLoc.getWorld() + ";";
            powerup += pLoc.getX() + ";";
            powerup += pLoc.getY() + ";";
            powerup += pLoc.getZ() + ";";
            if (args[0] == null) {
                return;
            }
            powerup += StringUtils.replace(args[0]);
            ItemStack is = ((Player) sender).getInventory().getItemInMainHand();
            if (is == null || is.getType() == Material.AIR) {
                return;
            }
            powerup += is.getType() + ";";
            powerup += is.getData() + ";";
            if (args[1] == null) {
                return;
            }
            powerup += PotionEffectType.getByName(args[1]) + ";";
            if (args[2] == null) {
                return;
            }
            powerup += Integer.valueOf(args[2]) * 20 + ";";
            if (args[3] == null) {
                return;
            }
            powerup += args[3] + ";";
            if (args[4] == null) {
                return;
            }
            powerup += args[4] + ";";
            List<String> powerups = data.getConfig().getStringList("Powerups");
            powerups.add(powerup);
            data.getConfig().set("Powerups", powerups);
            data.saveConfig();
        }
    }
}
