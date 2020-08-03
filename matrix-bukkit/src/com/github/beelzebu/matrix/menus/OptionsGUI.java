package com.github.beelzebu.matrix.menus;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.MatrixAPI;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.player.PlayerOptionType;
import cl.indiopikaro.jmatrix.api.server.ServerType;
import cl.indiopikaro.jmatrix.api.util.StringUtils;
import com.github.beelzebu.matrix.api.ItemBuilder;
import com.github.beelzebu.matrix.api.menu.GUIManager;
import com.github.beelzebu.matrix.player.options.FlyOption;
import com.github.beelzebu.matrix.player.options.Option;
import com.github.beelzebu.matrix.player.options.SpeedOption;
import com.github.beelzebu.matrix.util.CompatUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        setItem(10, new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).displayname(I18n.tl(Message.MENU_OPTIONS_SPEED_NAME, locale)).color(Color.RED).lore(speedLore).build(), p -> {
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
