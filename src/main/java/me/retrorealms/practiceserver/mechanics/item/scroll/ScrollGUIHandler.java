package me.retrorealms.practiceserver.mechanics.item.scroll;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Giovanni on 7-5-2017.
 */
public class ScrollGUIHandler implements Listener {

    public void onEnable(){
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!event.getInventory().getName().toLowerCase().contains("dungeoneer")) return;

        event.setCancelled(true);

        ItemStack itemStack = event.getCurrentItem();

        if (itemStack != null && itemStack.getType() != null && itemStack.getType() != Material.AIR && itemStack.getType() == Material.EMPTY_MAP) {
            NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();

            if (!nbtAccessor.hasKey("guiPrice")) return;

            int price = nbtAccessor.getInteger("guiPrice");
            int tier = nbtAccessor.getInteger("guiTier");

            if (Money.hasEnoughGems(player, price)) {

                ItemVendors.buyingitem.put(player.getName(), ItemAPI.getScrollGenerator().next(tier).clone());
                ItemVendors.buyingprice.put(player.getName(), price);
                player.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                player.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + ItemAPI.getScrollGenerator().next(tier).clone().getItemMeta().getDisplayName());
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                player.closeInventory();
            }

        }
    }
}
