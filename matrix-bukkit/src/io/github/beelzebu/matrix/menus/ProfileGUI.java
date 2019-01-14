package io.github.beelzebu.matrix.menus;

import io.github.beelzebu.matrix.api.ItemBuilder;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.SkullURL;
import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.menus.GUIManager;
import io.github.beelzebu.matrix.utils.placeholders.Placeholders;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @author Beelzebu
 */
public class ProfileGUI extends GUIManager {

    private static final MatrixAPI core = Matrix.getAPI();
    private final Player player;
    private final Set<Item> items = new HashSet<>();

    public ProfileGUI(Player p, String name) {
        super(54, name);
        player = p;
        setItems();
    }

    public static void socialItems(Inventory inv, Player user, int color) {
        ItemStack cristal = new ItemBuilder(Material.STAINED_GLASS_PANE, 1).damage((byte) color).build();
        {
            ItemStack perfil = new ItemBuilder(Material.SKULL_ITEM, 1, core.getString("Social.Profile.Name", user.getLocale())).damage((byte) 3).build();
            SkullMeta meta = (SkullMeta) perfil.getItemMeta();
            meta.setOwningPlayer(user);
            List<String> lore = new ArrayList<>();
            core.getMessages(user.getLocale()).getStringList("Social.Profile.Lore").forEach(line -> {
                lore.add(Placeholders.rep(user, line));
            });
            meta.setLore(lore);
            perfil.setItemMeta(meta);
            inv.setItem(2, perfil);
        }
        {
            ItemStack amigos = SkullURL.getCustomSkull("http://textures.minecraft.net/texture/9c269351c0468a5ea6c52329b7eb5625beecdb377bd6e597e5be68fd61752");
            ItemMeta meta = amigos.getItemMeta();
            meta.setDisplayName(core.getString("Social.Friends.Name", user.getLocale()));
            List<String> lore = new ArrayList<>();
            core.getMessages(user.getLocale()).getStringList("Social.Friends.Lore").forEach(line -> lore.add(Placeholders.rep(user, line)));
            meta.setLore(lore);
            amigos.setItemMeta(meta);
            inv.setItem(3, amigos);
        }
        {
            ItemStack party = SkullURL.getCustomSkull("http://textures.minecraft.net/texture/485b9c8bffe726a73a609db9953bce7b9cd9389f3dfdcbd6a31d7987dbd7b88");
            ItemMeta meta = party.getItemMeta();
            meta.setDisplayName(core.getString("Social.Party.Name", user.getLocale()));
            List<String> lore = new ArrayList<>();
            core.getMessages(user.getLocale()).getStringList("Social.Party.Lore").forEach(line -> {
                lore.add(Placeholders.rep(user, line));
            });
            meta.setLore(lore);
            party.setItemMeta(meta);
            inv.setItem(4, party);
        }
        {
            ItemStack clan = SkullURL.getCustomSkull("http://textures.minecraft.net/texture/c3f191fc90e193b3d6ac5dc50b614bd918dfec94bf29e9ddcc7eddf63a2a");
            ItemMeta meta = clan.getItemMeta();
            meta.setDisplayName(core.getString("Social.Clan.Name", user.getLocale()));
            List<String> lore = new ArrayList<>();
            core.getMessages(user.getLocale()).getStringList("Social.Clan.Lore").forEach(line -> lore.add(Placeholders.rep(user, line)));
            meta.setLore(lore);
            clan.setItemMeta(meta);
            inv.setItem(5, clan);
        }
        for (int j = 9; j < 18; ++j) {
            inv.setItem(j, cristal);
        }
    }

    private void setItems() {
        AbstractConfig messages = core.getMessages(player.getLocale());
        messages.getKeys("Social.Profile.Items").forEach(itemPath -> items.add(getItem(messages, "Social.Profile.Items." + itemPath)));
        socialItems(getInv(), player, 10);
        items.forEach(this::setItem);
    }
}
