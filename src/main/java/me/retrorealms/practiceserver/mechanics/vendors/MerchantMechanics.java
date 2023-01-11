package me.retrorealms.practiceserver.mechanics.vendors;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.altars.Altar;
import me.retrorealms.practiceserver.mechanics.item.Durability;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.Repairing;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Trading;
import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class MerchantMechanics implements Listener {
    public static List<String> in_npc_shop;
    public static ItemStack divider;
    public static ItemStack T1_scrap;
    public static ItemStack T2_scrap;
    public static ItemStack T3_scrap;
    public static ItemStack T4_scrap;
    public static ItemStack T5_scrap;
    public static ItemStack T6_scrap;
    public static ItemStack orb_of_alteration;
    static Logger log;

    static {
        MerchantMechanics.log = Logger.getLogger("Minecraft");
        MerchantMechanics.in_npc_shop = new ArrayList<String>();
        MerchantMechanics.divider = new ItemStack(Material.THIN_GLASS, 1);
        MerchantMechanics.T1_scrap = Durability.scrap(1);
        MerchantMechanics.T2_scrap = Durability.scrap(2);
        MerchantMechanics.T3_scrap = Durability.scrap(3);
        MerchantMechanics.T4_scrap = Durability.scrap(4);
        MerchantMechanics.T5_scrap = Durability.scrap(5);
        MerchantMechanics.T6_scrap = Durability.scrap(6);
        MerchantMechanics.orb_of_alteration = Items.orb(false);
    }


    private static String generateTitle(final String lPName, final String rPName) {
        String title;
        for (title = "  " + lPName; title.length() + rPName.length() < 30; title = String.valueOf(title) + " ") {
        }
        return title = String.valueOf(title) + rPName;
    }


    public static boolean isTradeButton(final ItemStack is) {
        if (is == null) {
            return false;
        }
        if (is.getType() == Material.INK_SACK && (is.getDurability() == 8 || is.getDurability() == 10) && is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
            final String item_name = is.getItemMeta().getDisplayName();
            if (item_name.contains("Trade") || item_name.contains("Duel")) {
                return true;
            }
        }
        return false;
    }



    public void onEnable() {
        PracticeServer.log.info("[MerchantMechanics] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
        final ItemMeta im = MerchantMechanics.divider.getItemMeta();
        im.setDisplayName(" ");
        MerchantMechanics.divider.setItemMeta(im);
    }

    public void onDisable() {
        PracticeServer.log.info("[MerchantMechanics] has been disabled.");
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerCloseInventory(final InventoryCloseEvent e) {
        final Player closer = (Player) e.getPlayer();
        if (MerchantMechanics.in_npc_shop.contains(closer.getName())) {
            final Inventory tradeInv = e.getInventory();

            int slot_var = -1;
            while (slot_var < 26) {
                if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9 && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                    continue;
                }
                ItemStack i = tradeInv.getItem(slot_var);
                if (i == null || i.getType() == Material.AIR || isTradeButton(i)) {
                    continue;
                }
                if (i.getType() == Material.THIN_GLASS) {
                    continue;
                }
                if (i.getType() == Material.EMERALD) {
                    i = Money.makeGems(i.getAmount());
                }
                if (closer.getInventory().firstEmpty() == -1) {
                    closer.getWorld().dropItemNaturally(closer.getLocation(), i);
                } else {
                    closer.getInventory().setItem(closer.getInventory().firstEmpty(), i);
                }
            }
            closer.getOpenInventory().getTopInventory().clear();
            closer.updateInventory();
            closer.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append("Trade cancelled.").toString());
            MerchantMechanics.in_npc_shop.remove(closer.getName());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player closer = e.getPlayer();
        if (MerchantMechanics.in_npc_shop.contains(closer.getName())) {
            final Inventory tradeInv = closer.getOpenInventory().getTopInventory();
            int slot_var = -1;
            while (slot_var < 26) {
                if (++slot_var != 0 && slot_var != 1 && slot_var != 2 && slot_var != 3 && slot_var != 9 && slot_var != 10 && slot_var != 11 && slot_var != 12 && slot_var != 18 && slot_var != 19 && slot_var != 20 && slot_var != 21) {
                    continue;
                }
                ItemStack i = tradeInv.getItem(slot_var);
                if (i == null || i.getType() == Material.AIR || isTradeButton(i)) {
                    continue;
                }
                if (i.getType() == Material.THIN_GLASS) {
                    continue;
                }
                if (i.getType() == Material.EMERALD) {
                    i = Money.makeGems(i.getAmount());
                }
                if (closer.getInventory().firstEmpty() == -1) {
                    closer.getWorld().dropItemNaturally(closer.getLocation(), i);
                } else {
                    closer.getInventory().setItem(closer.getInventory().firstEmpty(), i);
                }
            }
            closer.getOpenInventory().getTopInventory().clear();
            closer.closeInventory();
            closer.updateInventory();
            MerchantMechanics.in_npc_shop.remove(closer.getName());
        }
    }
}