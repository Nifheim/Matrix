package com.github.beelzebu.matrix.bukkit.menus;

import cl.indiopikaro.bukkitutil.api.ItemBuilder;
import cl.indiopikaro.bukkitutil.api.menu.BaseMenu;
import cl.indiopikaro.bukkitutil.util.CompatUtil;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.player.options.FlyOption;
import com.github.beelzebu.matrix.player.options.Option;
import com.github.beelzebu.matrix.player.options.SpeedOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Beelzebu
 */
public class OptionsGUI extends BaseMenu {

    private final MatrixAPI api = Matrix.getAPI();
    private final MatrixPlayer matrixPlayer;
    private final String locale;

    public OptionsGUI(MatrixPlayer matrixPlayer) {
        super(27, I18n.tl(Message.MENU_OPTIONS_TITLE, matrixPlayer.getLastLocale()));
        this.matrixPlayer = matrixPlayer;
        locale = matrixPlayer.getLastLocale();
        setItems();
    }

    private void setItems() {
        List<String> speedLore = new ArrayList<>();
        for (String line : I18n.tls(Message.MENU_OPTIONS_SPEED_LORE, locale)) {
            speedLore.add(rep(line, new SpeedOption(matrixPlayer)));
        }
        ItemBuilder speedItem = new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).displayname(I18n.tl(Message.MENU_OPTIONS_SPEED_NAME, locale)).lore(speedLore);
        if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_10)) {
            speedItem.color(Color.RED);
        } else {
            speedItem.potionType(PotionEffectType.HEAL);
        }
        setItem(10, speedItem.build(), p -> {
            boolean status = !matrixPlayer.getOption(PlayerOptionType.SPEED);
            if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
            } else {
                matrixPlayer.setOption(PlayerOptionType.SPEED, status);
            }
            p.closeInventory();
        });

        List<String> flyLore = new ArrayList<>();
        for (String line : I18n.tls(Message.MENU_OPTIONS_FLY_LORE, locale)) {
            flyLore.add(rep(line, new FlyOption(matrixPlayer)));
        }

        setItem(12, new ItemBuilder(Material.FEATHER).displayname(I18n.tl(Message.MENU_OPTIONS_FLY_NAME, locale)).lore(flyLore).build(), p -> {
            if (p.hasPermission("matrix.command.fly")) {
                boolean status = !matrixPlayer.getOption(PlayerOptionType.FLY);
                if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                    p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
                } else {
                    matrixPlayer.setOption(PlayerOptionType.FLY, status);
                }
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage(StringUtils.replace("&c&lLo sentimos!&7 no tienes permisos suficientes para usar esto."));
            }
            p.closeInventory();
        });

        List<String> hideLore = new ArrayList<>();
        hideLore.addAll(Arrays.asList(I18n.tls(Message.MENU_OPTIONS_HIDE_LORE, locale)));

        setItem(14, new ItemBuilder(CompatUtil.getInstance().getGreenDye()).displayname(I18n.tl(Message.MENU_OPTIONS_HIDE_NAME, locale)).lore(hideLore).build(), p -> {
            boolean fail = false;
            try {
                if (!api.getServerInfo().getServerType().equals(ServerType.LOBBY)) {
                    fail = true;
                } else {
                    Class.forName("de.simonsator.partyandfriendsgui.api.PartyFriendsAPI").getMethod("openHideInventory", Player.class).invoke(null, p);
                }
            } catch (ReflectiveOperationException ex) {
                fail = true;
            }
            if (fail) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage("§cAl parecer esta función no está disponible en este servidor.");
                p.closeInventory();
            }
        });

        // TODO: check implementation
        /*
        setItem(16, new ItemBuilder(CompatUtil.getInstance().getBookAndQuill()).displayname(StringUtils.replace("&8Nick")).lore(Arrays.asList("", StringUtils.replace("&7Haz click para cambiar"), StringUtils.replace("&7el color de tu nick."))).build(), p -> {
            if (p.hasPermission("matrix.command.nick")) {
                Bukkit.dispatchCommand(p, "nick");
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 1);
                p.sendMessage(StringUtils.replace("&c&lLo sentimos!&7 no tienes permisos suficientes para hacer esto."));
                p.closeInventory();
            }
        });
         */
    }

    private String rep(String str, Option opt) {
        return StringUtils.replace(str.replaceAll("%status%", opt.getStatus()));
    }
}
