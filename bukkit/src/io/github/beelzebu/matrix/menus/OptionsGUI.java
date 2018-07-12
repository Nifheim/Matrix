package io.github.beelzebu.matrix.menus;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.api.ItemBuilder;
import io.github.beelzebu.matrix.api.menus.GUIManager;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.options.FlyOption;
import io.github.beelzebu.matrix.player.options.Option;
import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import io.github.beelzebu.matrix.player.options.SpeedOption;
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
import org.bukkit.inventory.ItemStack;

/**
 * @author Beelzebu
 */
public class OptionsGUI extends GUIManager {

    private final MatrixAPI core = MatrixAPI.getInstance();
    private final Player player;

    public OptionsGUI(Player p, String name) {
        super(27, name);
        player = p;
        setItems();
    }

    private void setItems() {
        MatrixPlayer np = core.getPlayer(player.getUniqueId());
        String lang = player.getLocale();
        for (int i = 0; i < 27; i++) {
            setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7));
        }

        List<String> speedlore = new ArrayList<>();
        core.getMessages(lang).getStringList("Options.Speed.Lore").forEach(line -> {
            speedlore.add(rep(line, new SpeedOption(np)));
        });

        setItem(10, new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).displayname(core.getString("Options.Speed.Name", lang)).color(Color.RED).lore(speedlore).build(), p -> {
            boolean status = !np.getOption(PlayerOptionType.SPEED);
            if (core.getPlayer(p.getUniqueId()).isInLobby()) {
                np.setOption(PlayerOptionType.SPEED, status);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 2);
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
            }
            p.closeInventory();
        });

        List<String> flylore = new ArrayList<>();
        core.getMessages(lang).getStringList("Options.Fly.Lore").forEach(line -> {
            flylore.add(rep(line, new FlyOption(np)));
        });

        setItem(12, new ItemBuilder(Material.FEATHER).displayname(core.getString("Options.Fly.Name", lang)).lore(flylore).build(), p -> {
            if (p.hasPermission("matrix.vip.count")) {
                if (core.getPlayer(p.getUniqueId()).isInLobby()) {
                    boolean status = !np.getOption(PlayerOptionType.FLY);
                    np.setOption(PlayerOptionType.FLY, status);
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 2);
                } else {
                    p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
                }
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage(core.rep("&c&lLo sentimos!&7 debes ser &8Count&7 o superior para poder usar esta opción."));
            }
            p.closeInventory();
        });

        setItem(14, new ItemBuilder(Material.INK_SACK).damage((short) 5).displayname(core.getString("Options.Hide.Name", lang)).lore(core.getMessages(lang).getStringList("Options.Hide.Lore")).build(), p -> {
            boolean fail = false;
            try {
                if (!np.isInLobby()) {
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

        setItem(16, new ItemBuilder(Material.BOOK_AND_QUILL).displayname(core.rep("&8Nick")).lore(Arrays.asList("", core.rep("&7Haz click para cambiar"), core.rep("&7el color de tu nick."))).build(), p -> {
            if (p.hasPermission("matrix.vip.duke")) {
                Bukkit.dispatchCommand(p, "nick");
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage(core.rep("&c&lLo sentimos!&7 debes ser &cDuke&7 o superior para poder usar esta opción."));
                p.closeInventory();
            }
        });
        if (player.hasPermission("nifheim.option.nick")) {
            setItem(26, new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).displayname(core.rep("&8Ocultar nick")).lore(np.getOption(PlayerOptionType.NICKNAME) + "").build(), p -> {
                boolean status = !np.getOption(PlayerOptionType.NICKNAME);
                np.setOption(PlayerOptionType.NICKNAME, status);
                p.closeInventory();
            });
        }
    }

    private String rep(String str, Option opt) {
        return core.rep(str.replaceAll("%status%", opt.getStatus()));
    }
}
