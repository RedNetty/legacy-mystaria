package me.retrorealms.practiceserver.mechanics.item;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class Untradeable
        implements Listener {
    public void onEnable() {
        PracticeServer.log.info("[Untradeable] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Untradeable] has been disabled.");
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.SADDLE || e.getEntity().getItemStack().getType() == Material.WRITTEN_BOOK || e.getEntity().getItemStack().getType() == Material.QUARTZ) {
            e.getEntity().remove();
            e.setCancelled(true);
        }
        if (e.getEntity().getItemStack().getItemMeta().hasLore() && e.getEntity().getItemStack().getItemMeta().getLore().contains(ChatColor.GRAY + "Untradeable")) {
            e.getEntity().remove();
            e.setCancelled(true);
        }
    }

    public static boolean isItemTradeable(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.toLowerCase().contains("untradeable")) continue;
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getItemMeta().hasLore() && e.getItemDrop().getItemStack().getItemMeta().getLore().contains(ChatColor.GRAY + "Untradeable")) {
            e.getItemDrop().remove();
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.0f);
            e.getPlayer().sendMessage(ChatColor.GRAY + "This item was" + ChatColor.GRAY + ChatColor.ITALIC + " untradeable" + ChatColor.GRAY + ", so it has " + ChatColor.GRAY + ChatColor.UNDERLINE + "vanished.");
        }
        if (e.getItemDrop().getItemStack().getType() == Material.WRITTEN_BOOK) {
            e.setCancelled(true);
        }
        if (e.getItemDrop().getItemStack().getType() == Material.QUARTZ) {
            e.setCancelled(true);
        }
        if (e.getItemDrop().getItemStack().getType() == Material.ENCHANTED_BOOK) {
            e.setCancelled(true);
        }
        if (e.getItemDrop().getItemStack().getItemMeta().hasLore() && e.getItemDrop().getItemStack().getItemMeta().getLore().contains(ChatColor.GRAY + "Permanent Untradeable")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getItemMeta().hasLore() && e.getItem().getItemStack().getItemMeta().getLore().contains(ChatColor.GRAY + "Untradeable")) {
            e.setCancelled(true);
        }
        if (e.getItem().getItemStack().getType() == Material.WRITTEN_BOOK) {
            e.setCancelled(true);
        }
        if (e.getItem().getItemStack().getType() == Material.QUARTZ) {
            e.setCancelled(true);
        }
        if (e.getItem().getItemStack().getItemMeta().hasLore() && e.getItem().getItemStack().getItemMeta().getLore().contains(ChatColor.GRAY + "Permanent Untradeable")) {
            e.setCancelled(true);
        }
        if (e.getItem().getItemStack().getItemMeta().hasLore() && e.getItem().getItemStack().getItemMeta().getLore().contains("notarealitem")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().getItemMeta().hasLore() && e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + "Untradeable")) {
                Player p = (Player) e.getWhoClicked();
                e.setCancelled(true);
                if (p.getOpenInventory().getTopInventory().getTitle().contains("Bank Chest (1/1)")) {
                    p.sendMessage(ChatColor.RED + "You " + ChatColor.RED + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " bank this item, as it is part of your spawn kit.");
                }
            }
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
                e.setCancelled(true);
            }
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.QUARTZ) {
                e.setCancelled(true);
            }
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().getItemMeta().hasLore() && e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GRAY + "Permenant Untradeable")) {
                e.setCancelled(true);
            }
        }
    }
}

