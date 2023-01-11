package me.retrorealms.practiceserver.mechanics.profession;

import me.retrorealms.practiceserver.mechanics.inventory.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Giovanni on 26-5-2017.
 */
public class EnchantGUI extends Menu {

    EnchantGUI(Player player) {
        super(player, "Enchanter", 27);

        ItemStack surrounding = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 10);
        ItemMeta itemMeta = surrounding.getItemMeta();

        int slots[] = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

        itemMeta.setDisplayName("-");
        itemMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Drop a weapon in the middle slot."));
        surrounding.setItemMeta(itemMeta);


        for (int i : slots)
            this.setItem(i, surrounding);
    }

    @Override
    public void onClick(InventoryClickEvent event, int slot) {

    }
}
