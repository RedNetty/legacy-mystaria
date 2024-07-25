/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package me.retrorealms.practiceserver.mechanics.vendors;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.altars.Altar;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class Merchant implements Listener {



    @EventHandler
    public void onMerchantClick(PlayerInteractEntityEvent e) {
        if ((e.getRightClicked() instanceof HumanEntity)) {
            HumanEntity p = (HumanEntity) e.getRightClicked();
            if(p.getName().equals("Merchant")) {
                if ((e.getPlayer().getItemInHand().getType() != Material.AIR) && (Items.isItemTradeable(e.getPlayer().getItemInHand())) &&
                        ((Altar.isArmour(e.getPlayer().getItemInHand())) || (Altar.isWeapon(e.getPlayer().getItemInHand()) || (e.getPlayer().getItemInHand().getType().toString().contains("_ORE") && ProfessionMechanics.getOreTier(e.getPlayer().getItemInHand().getType()) > 0)))) {
                    merchant(e.getPlayer(), e.getPlayer().getItemInHand());
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
                }else {
                    e.getPlayer().sendMessage(ChatColor.RED + "You must be holding a tradeable item to trade.");
                }
            }
        }
    }

    @EventHandler
    public void confirmMerchant(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().equals("Merchant")) {
            e.setCancelled(true);
            if(e.getCurrentItem().getType() ==  Material.EMERALD_BLOCK) {
                p.closeInventory();
                scrapGear(p, p.getItemInHand());
                p.setItemInHand(null);
            }
        }
    }

    private void merchant(Player player, ItemStack is)
    {
        Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, "Merchant");
        ItemStack em = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta mem = em.getItemMeta();
        mem.setDisplayName(ChatColor.GREEN + "Confirm Trade");
        mem.setLore(
                Arrays.asList(new String[] {
                        ChatColor.GRAY + "You are trading the item ",
                        ChatColor.GRAY + "you are currently holding for gems" }));
        em.setItemMeta(mem);
        inv.setItem(1, is);
        inv.setItem(3, em);
        player.openInventory(inv);
    }

    public void scrapGear(Player p, ItemStack is) {
        double reward = 0;
        String strippedItemName = ChatColor.stripColor(is.getItemMeta().getDisplayName());
        if (is.getType() == Material.MAGMA_CREAM && is.getItemMeta().getDisplayName().equalsIgnoreCase("Orb of Alteration")) {
            // Handle normal orb trade-in
            int orbAmount = is.getAmount();
            reward = 500 * orbAmount;
        } else if (is.getType().toString().contains("_ORE") && ProfessionMechanics.getOreTier(is.getType()) > 0) {
            int oreAmount = is.getAmount();
            int oreTier = ProfessionMechanics.getOreTier(is.getType());
            reward = 20;
            reward = (int) ((reward * oreAmount * oreTier) * 1.23);
        } else {
            Random r = new Random();
            int t = Items.getTierFromColor(is);
            reward = (((t / 10D) + 1D) * (t * t)) * 12D;
            reward = r.nextInt((int) reward / 3) + (int) reward;
        }
        if ((ModerationMechanics.isDonator(p)) || (ModerationMechanics.isStaff(p))){
            double rew = (double) reward * 1.20D;
            p.getInventory().addItem(Money.createBankNote((int)rew));
        } else {
            p.getInventory().addItem(Money.createBankNote((int)reward));
        }
        p.sendMessage(ChatColor.GREEN + "You received " + ChatColor.BOLD + reward + " gems" + ChatColor.GREEN + " for your trade.");
    }

    public void onEnable(){
        System.out.println("[Merchant] has been enabled");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
    }

    public void onDisable(){
        System.out.println("[Merchant] has been disabled");
    }
}

