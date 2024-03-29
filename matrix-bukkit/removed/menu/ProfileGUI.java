package com.github.beelzebu.matrix.bukkit.menus;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.bukkit.util.placeholders.Placeholders;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.nifheim.bukkit.util.CompatUtil;
import net.nifheim.bukkit.util.ItemBuilder;
import net.nifheim.bukkit.util.SkullCreator;
import net.nifheim.bukkit.util.menu.BaseMenu;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/**
 * TODO: remove
 *
 * @author Beelzebu
 */
@SuppressWarnings("deprecation")
public class ProfileGUI extends BaseMenu {

    private final @NotNull MatrixPlayer matrixPlayer;
    private final Set<Item> items = new HashSet<>();
    private final @NotNull String locale;

    public ProfileGUI(@NotNull MatrixPlayer matrixPlayer) {
        super(54, I18n.tl(Message.MENU_SOCIAL_TITLE, matrixPlayer.getLastLocale()));
        this.matrixPlayer = matrixPlayer;
        this.locale = matrixPlayer.getLastLocale();
        setItems();
    }

    private void socialItems(@NotNull Inventory inv) {
        ItemStack purpleGlassPane = new ItemBuilder(CompatUtil.getInstance().getPurpleGlassPane()).build();
        ItemStack profileItem = new ItemBuilder(CompatUtil.getInstance().getPlayerHead()).amount(1).displayname(I18n.tl(Message.MENU_SOCIAL_PROFILE_NAME, locale)).build();
        SkullMeta profileMeta = (SkullMeta) profileItem.getItemMeta();
        if (CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_12)) {
            profileMeta.setOwningPlayer(Bukkit.getPlayer(matrixPlayer.getUniqueId()));
        } else {
            profileMeta.setOwner(matrixPlayer.getName());
        }
        List<String> profileLore = new ArrayList<>();
        for (String line : I18n.tls(Message.MENU_SOCIAL_PROFILE_LORE, locale)) {
            profileLore.add(Placeholders.rep(Bukkit.getPlayer(matrixPlayer.getUniqueId()), line));
        }
        profileMeta.setLore(profileLore);
        profileItem.setItemMeta(profileMeta);
        inv.setItem(2, profileItem);

        ItemStack friendsItem = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/9c269351c0468a5ea6c52329b7eb5625beecdb377bd6e597e5be68fd61752");
        ItemMeta friendsMeta = friendsItem.getItemMeta();
        friendsMeta.setDisplayName(I18n.tl(Message.MENU_SOCIAL_FRIENDS_NAME, locale));
        List<String> friendsLore = new ArrayList<>();
        for (String line : I18n.tls(Message.MENU_SOCIAL_FRIENDS_LORE, locale)) {
            friendsLore.add(Placeholders.rep(Bukkit.getPlayer(matrixPlayer.getUniqueId()), line));
        }
        friendsMeta.setLore(friendsLore);
        friendsItem.setItemMeta(friendsMeta);
        inv.setItem(3, friendsItem);

        ItemStack partyItem = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/485b9c8bffe726a73a609db9953bce7b9cd9389f3dfdcbd6a31d7987dbd7b88");
        ItemMeta partyMeta = partyItem.getItemMeta();
        partyMeta.setDisplayName(I18n.tl(Message.MENU_SOCIAL_PARTY_NAME, locale));
        List<String> partyLore = new ArrayList<>();
        for (String line : I18n.tls(Message.MENU_SOCIAL_PARTY_LORE, locale)) {
            partyLore.add(Placeholders.rep(Bukkit.getPlayer(matrixPlayer.getUniqueId()), line));
        }
        partyMeta.setLore(partyLore);
        partyItem.setItemMeta(partyMeta);
        inv.setItem(4, partyItem);

        ItemStack clanItem = SkullCreator.itemFromUrl("http://textures.minecraft.net/texture/c3f191fc90e193b3d6ac5dc50b614bd918dfec94bf29e9ddcc7eddf63a2a");
        ItemMeta clanMeta = clanItem.getItemMeta();
        clanMeta.setDisplayName(I18n.tl(Message.MENU_SOCIAL_CLAN_NAME, locale));
        List<String> clanLore = new ArrayList<>();
        for (String line : I18n.tls(Message.MENU_SOCIAL_CLAN_LORE, locale)) {
            clanLore.add(Placeholders.rep(Bukkit.getPlayer(matrixPlayer.getUniqueId()), line));
        }
        clanMeta.setLore(clanLore);
        clanItem.setItemMeta(clanMeta);
        inv.setItem(5, clanItem);

        for (int j = 9; j < 18; ++j) {
            inv.setItem(j, purpleGlassPane);
        }
    }

    private void setItems() {
        //AbstractConfig messages = I18n.getMessagesFile(matrixPlayer.getLastLocale());
        //messages.getKeys("Social.Profile.Items").forEach(itemPath -> items.add(getItem(messages, "Social.Profile.Items." + itemPath)));
        socialItems(getInv());
        items.forEach(this::setItem);
    }
}
