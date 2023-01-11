/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerKickEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.util.Vector
 */
package me.retrorealms.practiceserver.mechanics.item;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class    Repairing
        implements Listener {
    static HashMap<String, ItemStack> repairing = new HashMap<String, ItemStack>();
    static HashMap<String, Integer> repaircost = new HashMap<String, Integer>();
    static HashMap<String, Item> ghostitem = new HashMap<String, Item>();

    public void onEnable() {
        PracticeServer.log.info("[Repairing] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Repairing] has been disabled.");
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!repairing.containsKey(p.getName())) continue;
            p.getInventory().addItem(new ItemStack[]{repairing.get(p.getName())});
            repairing.remove(p.getName());
            repaircost.remove(p.getName());
            ghostitem.get(p.getName()).remove();
            ghostitem.remove(p.getName());
        }
    }

    public static int getTier(ItemStack is) {
        int tier = 0;
        if (!is.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) && (is.getType().name().contains("WOOD_") || is.getType().name().contains("LEATHER_"))) {
            tier = 1;
        }
        else if (is.getType().name().contains("STONE_") || is.getType().name().contains("CHAINMAIL_")) {
            tier = 2;
        }
        else if (is.getType().name().contains("IRON_")) {
            tier = 3;
        }
        else if (!is.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) && is.getType().name().contains("DIAMOND_")) {
            tier = 4;
        }
        else if (is.getType().name().contains("GOLD_")) {
            tier = 5;
        }else if (is.getType().name().contains("LEATHER_") || is.getType().name().contains("DIAMOND_")) {
            return 6;
        }
        return tier;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (repairing.containsKey(p.getName())) {
            p.getInventory().addItem(new ItemStack[]{repairing.get(p.getName())});
            repairing.remove(p.getName());
            repairing.remove(p.getName());
            ghostitem.get(p.getName()).remove();
            ghostitem.remove(p.getName());
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        if (repairing.containsKey(p.getName())) {
            p.getInventory().addItem(new ItemStack[]{repairing.get(p.getName())});
            repairing.remove(p.getName());
            repairing.remove(p.getName());
            ghostitem.get(p.getName()).remove();
            ghostitem.remove(p.getName());
        }
    }
}

