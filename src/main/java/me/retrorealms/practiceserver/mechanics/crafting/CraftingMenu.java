package me.retrorealms.practiceserver.mechanics.crafting;

import me.retrorealms.practiceserver.mechanics.crafting.items.CustomItemHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CraftingMenu {
    private final CraftingHandler craftingHandler;
    private final CustomItemHandler itemSystem;

    public CraftingMenu(CraftingHandler craftingHandler, CustomItemHandler itemSystem) {
        this.craftingHandler = craftingHandler;
        this.itemSystem = itemSystem;
    }

    public void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Crafting Menu");

        int slot = 0;
        for (Recipe recipe : craftingHandler.getRecipes().values()) {
            ItemStack resultItem = recipe.getResult().clone();
            ItemMeta meta = resultItem.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();

            lore.add("");
            lore.add(ChatColor.YELLOW + "Required ingredients:");
            for (ItemStack ingredient : recipe.getIngredients()) {
                lore.add(ChatColor.GRAY + "- " + ingredient.getItemMeta().getDisplayName() + " x" + ingredient.getAmount());
            }
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to craft!");

            meta.setLore(lore);
            resultItem.setItemMeta(meta);

            inventory.setItem(slot, resultItem);
            slot++;
        }

        player.openInventory(inventory);
    }
}