package io.github.beelzebu.matrix.menus;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.api.ItemBuilder;
import io.github.beelzebu.matrix.api.menus.GUIManager;
import io.github.beelzebu.matrix.networkxp.RewardManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RewardsMenu extends GUIManager {

    private final Player opener;
    private int slot;

    public RewardsMenu(Player player) {
        super(54, "&8Perfil de rango");
        opener = player;
        setItems();
    }

    private void setItems() {
        slot = 1;
        setItem(0, new ItemBuilder(new ItemStack(Material.SKULL_ITEM)).owner(opener.getName()).displayname("&6" + opener.getName()).lore("&7Tu nivel: &a").build());
        RewardManager.getRewardsByLevel().values().forEach(re -> {
            setItem(slot, new ItemBuilder(Material.STORAGE_MINECART).build());
            slot++;
        });
    }

    private String rep(String string) {
        return MatrixAPI.getInstance().rep(PlaceholderAPI.setPlaceholders(opener, string));
    }
}
