package io.github.beelzebu.matrix.menus;

import io.github.beelzebu.matrix.api.ItemBuilder;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.menus.GUIManager;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.api.util.StringUtils;
import io.github.beelzebu.matrix.player.options.FlyOption;
import io.github.beelzebu.matrix.player.options.Option;
import io.github.beelzebu.matrix.player.options.SpeedOption;
import io.github.beelzebu.matrix.util.CompatUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

/**
 * @author Beelzebu
 */
public class OptionsGUI extends GUIManager {

    private final MatrixAPI api = Matrix.getAPI();
    private final Player player;

    public OptionsGUI(Player p, String name) {
        super(27, name);
        player = p;
        setItems();
    }

    private void setItems() {
        MatrixPlayer np = api.getPlayer(player.getUniqueId());
        String lang = player.getLocale();

        List<String> speedlore = new ArrayList<>();
        api.getMessages(lang).getStringList("Options.Speed.Lore").forEach(line -> speedlore.add(rep(line, new SpeedOption(np))));

        setItem(10, new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).displayname(api.getString("Options.Speed.Name", lang)).color(Color.RED).lore(speedlore).build(), p -> {
            boolean status = !np.getOption(PlayerOptionType.SPEED);
            if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
            }
            np.setOption(PlayerOptionType.SPEED, status);
            p.closeInventory();
        });

        List<String> flylore = new ArrayList<>();
        api.getMessages(lang).getStringList("Options.Fly.Lore").forEach(line -> flylore.add(rep(line, new FlyOption(np))));

        setItem(12, new ItemBuilder(Material.FEATHER).displayname(api.getString("Options.Fly.Name", lang)).lore(flylore).build(), p -> {
            if (p.hasPermission("matrix.vip2")) {
                boolean status = !np.getOption(PlayerOptionType.FLY);
                if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                    p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
                }
                np.setOption(PlayerOptionType.FLY, status);
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage(StringUtils.replace("&c&lLo sentimos!&7 no tienes permisos suficientes para usar esto."));
            }
            p.closeInventory();
        });

        setItem(14, new ItemBuilder(CompatUtil.getInstance().getGreenDye()).displayname(api.getString("Options.Hide.Name", lang)).lore(api.getMessages(lang).getStringList("Options.Hide.Lore")).build(), p -> {
            boolean fail = false;
            try {
                if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                    fail = true;
                } else {
                    Class.forName("de.simonsator.partyandfriendsgui.api.PartyFriendsAPI").getMethod("openHideInventory", Player.class).invoke(null, p);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                fail = true;
            }
            if (fail) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
                p.closeInventory();
            }
        });

        setItem(16, new ItemBuilder(CompatUtil.getInstance().getBookAndQuill()).displayname(StringUtils.replace("&8Nick")).lore(Arrays.asList("", StringUtils.replace("&7Haz click para cambiar"), StringUtils.replace("&7el color de tu nick."))).build(), p -> {
            if (p.hasPermission("matrix.command.nick")) {
                Bukkit.dispatchCommand(p, "nick");
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage(StringUtils.replace("&c&lLo sentimos!&7 no tienes permisos suficientes para hacer esto."));
                p.closeInventory();
            }
        });
        if (player.hasPermission("matrix.command.vanish")) {
            setItem(26, new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).displayname(StringUtils.replace("&8Ocultar nick")).lore(np.getOption(PlayerOptionType.NICKNAME) + "").build(), p -> {
                boolean status = !np.getOption(PlayerOptionType.NICKNAME);
                np.setOption(PlayerOptionType.NICKNAME, status);
                p.closeInventory();
            });
        }
    }

    private String rep(String str, Option opt) {
        return StringUtils.replace(str.replaceAll("%status%", opt.getStatus()));
    }
}
