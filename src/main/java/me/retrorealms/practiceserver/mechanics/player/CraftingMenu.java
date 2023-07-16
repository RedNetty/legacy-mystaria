package me.retrorealms.practiceserver.mechanics.player;


import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.item.Journal;
import me.retrorealms.practiceserver.mechanics.teleport.Hearthstone;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;


public class CraftingMenu implements Listener {

    private static void addOrReplace(Player player, ItemStack itemStack, Material material) {
        int slot = player.getInventory().first(material);
        if (slot == -1) {
            player.getInventory().addItem(itemStack);
        } else {
            //Overwrite.
            player.getInventory().setItem(slot, itemStack);
        }
    }

    public void removeAchievements(Player player) {
        for (Iterator<Advancement> it = Bukkit.getServer().advancementIterator(); it.hasNext(); ) {
            Advancement advancement = it.next();
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                for (String criteria : progress.getAwardedCriteria()) {
                    progress.revokeCriteria(criteria);
                }
            }
        }
    }


//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onPLayerCraft(CraftItemEvent event) {
//        if (event.getInventory().getResult() != null) {
//            Bukkit.getLogger().info("Name: " + event.getInventory().getName() + " Holder: " + event.getInventory().getHolder());
//            event.setCancelled(true);
//        }
//    }

    public void startInitialization() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                        Inventory topInventory = player.getOpenInventory().getTopInventory();
                        if (topInventory.getItem(1) == null) {
                            topInventory.setItem(1, Hearthstone.hearthstone());
                            topInventory.setItem(2, Journal.journal());
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 25L, 25L);
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING
                && event.getInventory().getType() == InventoryType.CRAFTING) {
            if (event.getRawSlot() == 1) {
                Hearthstone.castHearth((Player) event.getWhoClicked());
                event.setCancelled(true);
            } else if (event.getRawSlot() == 2) {
                Journal.openJournal((Player) event.getWhoClicked());
                event.setCancelled(true);
            }
        }
    }

    public void removeItems(Player player) {
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
            if (player.getOpenInventory().getTopInventory().getItem(0) != null)
                player.getOpenInventory().getTopInventory().setItem(0, null);
            player.getOpenInventory().getTopInventory().setItem(1, null);
            player.getOpenInventory().getTopInventory().setItem(2, null);
            player.getOpenInventory().getTopInventory().setItem(3, null);
            player.getOpenInventory().getTopInventory().setItem(4, null);
        }


    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftingInventoryClose(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeItems(player);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftingInventoryClose(PlayerDeathEvent event) {
        Player player = event.getEntity();
        removeItems(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftingInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        removeItems(player);
    }
}
