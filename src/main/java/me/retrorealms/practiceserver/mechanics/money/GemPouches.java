/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerPickupItemEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package me.retrorealms.practiceserver.mechanics.money;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.profession.Mining;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GemPouches
        implements Listener {
    public void onEnable() {
        PracticeServer.log.info("[GemPouches] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[GemPouches] has been disabled.");
    }

    public static ItemStack gemPouch(int tier) {
        String name = "";
        String lore = "";
        if (tier == 1) {
            name = ChatColor.WHITE + "Small Gem Pouch" + ChatColor.GREEN + ChatColor.BOLD + " 0g";
            lore = ChatColor.GRAY + "A small linen pouch that holds " + ChatColor.BOLD + "100g";
        } else if (tier == 2) {
            name = ChatColor.GREEN + "Medium Gem Sack" + ChatColor.GREEN + ChatColor.BOLD + " 0g";
            lore = ChatColor.GRAY + "A medium wool sack that holds " + ChatColor.BOLD + "150g";
        } else if (tier == 3) {
            name = ChatColor.AQUA + "Large Gem Satchel" + ChatColor.GREEN + ChatColor.BOLD + " 0g";
            lore = ChatColor.GRAY + "A large leather satchel that holds " + ChatColor.BOLD + "200g";
        } else if (tier == 4) {
            name = ChatColor.LIGHT_PURPLE + "Gigantic Gem Container" + ChatColor.GREEN + ChatColor.BOLD + " 0g";
            lore = ChatColor.GRAY + "A giant container that holds " + ChatColor.BOLD + "300g";
        } else if (tier == 5) {
            name = ChatColor.YELLOW + "Legendary Gem Container" + ChatColor.GREEN + ChatColor.BOLD + " 0g";
            lore = ChatColor.GRAY + "A giant container that holds " + ChatColor.BOLD + "500g";
        } else if (tier == 6) {
            name = ChatColor.RED + "Insane Gem Container" + ChatColor.GREEN + ChatColor.BOLD + " 0g";
            lore = ChatColor.GRAY + "A giant container that holds " + ChatColor.BOLD + "100000g";
        }
        ItemStack is = new ItemStack(Material.INK_SACK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);

        if (name.contains("Insane"))
            im.setLore(Arrays.asList(lore, "", ChatColor.RED + "Soulbound"));
        else im.setLore(Collections.singletonList(lore));

        is.setItemMeta(im);
        return is;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();

        if (itemStack.hasItemMeta()) {

            if (!itemStack.getItemMeta().hasDisplayName()) return;

            Player player = event.getPlayer();

            boolean soulbound = false;

            if (!itemStack.getItemMeta().hasLore()) return;

            for (String string : itemStack.getItemMeta().getLore())
                if (string.toLowerCase().contains("soulbound"))
                    soulbound = true;

            String itemName = itemStack.getItemMeta().getDisplayName();

            if (soulbound)
                player.sendMessage(ChatColor.RED + "You've dropped your " + ChatColor.UNDERLINE + itemName);

            if (itemStack.getType() == Material.SADDLE) event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getName().equals("container.crafting")) {
            int amt;
            if (e.isLeftClick() && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.INK_SACK && GemPouches.isGemPouch(e.getCurrentItem()) && e.getCursor() != null && e.getCursor().getType() == Material.EMERALD) {
                if (e.getCurrentItem().getAmount() != 1) {
                    return;
                }
                e.setCancelled(true);
                amt = GemPouches.getCurrentValue(e.getCurrentItem());
                int max = GemPouches.getMaxValue(e.getCurrentItem());
                int add = e.getCursor().getAmount();
                if (amt < max) {
                    if (amt + add > max) {
                        e.getCursor().setAmount(add - (max - amt));
                        GemPouches.setPouchBal(e.getCurrentItem(), max);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    } else {
                        e.setCursor(null);
                        GemPouches.setPouchBal(e.getCurrentItem(), amt + add);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    }
                }
            }
            if (e.isRightClick() && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.INK_SACK && GemPouches.isGemPouch(e.getCurrentItem()) && (e.getCursor() == null || e.getCursor().getType() == Material.AIR)) {
                if (e.getCurrentItem().getAmount() != 1) {
                    return;
                }
                e.setCancelled(true);
                amt = GemPouches.getCurrentValue(e.getCurrentItem());
                if (amt <= 0) {
                    return;
                }
                if (amt > 64) {
                    e.setCursor(Money.makeGems(64));
                    GemPouches.setPouchBal(e.getCurrentItem(), amt -= 64);
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    e.setCursor(Money.makeGems(amt));
                    GemPouches.setPouchBal(e.getCurrentItem(), 0);
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void onItemPickup(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if (e.isCancelled()) {
            return;
        }
        if(e.getItem().getItemStack().hasItemMeta()) {
            ItemMeta meta = e.getItem().getItemStack().getItemMeta();
            for (ItemFlag itemFlag : ItemFlag.values()) {
                meta.addItemFlags(itemFlag);
                e.getItem().getItemStack().setItemMeta(meta);
            }
        }
        if (e.getItem().getItemStack().getType() != Material.EMERALD) {
            return;
        }
        int add = e.getItem().getItemStack().getAmount();
        if (Toggles.getToggles(p.getUniqueId()).contains("Gems")) {
            Economy.depositPlayer(p.getUniqueId(), add);
            if (Toggles.getToggles(p.getUniqueId()).contains("Debug")) {
                p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "                    +" + ChatColor.GREEN.toString() + add + ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "G");

            }
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            e.setCancelled(true);
            e.getItem().remove();
            return;
        }
        ItemStack[] arritemStack = p.getInventory().getContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack is = arritemStack[n2];
            if (is != null && GemPouches.isGemPouch(is)) {
                if (is.getAmount() != 1) {
                    return;
                }
                int amt = GemPouches.getCurrentValue(is);
                int max = GemPouches.getMaxValue(is);
                if (add > 0 && amt < max) {
                    if (amt + add > max) {
                        ItemStack newis = e.getItem().getItemStack();
                        newis.setAmount(add -= max - amt);
                        e.getItem().setItemStack(newis);
                        GemPouches.setPouchBal(is, max);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        e.setCancelled(true);
                        int adding = max - amt;
                        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "                    +" + ChatColor.GREEN.toString() + adding + ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "G");
                    } else {
                        e.getItem().remove();
                        GemPouches.setPouchBal(is, amt + add);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        e.setCancelled(true);
                        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "                    +" + ChatColor.GREEN.toString() + add + ChatColor.GREEN + ChatColor.BOLD + "G");
                        add = 0;
                    }
                }
            }
            ++n2;
        }
    }

    //yeah this is absolute garbage but w/e it works
    public static void onItemPickup(Player p, ItemStack itemStack) {
        int add = itemStack.getAmount();
        if (Toggles.getToggles(p.getUniqueId()).contains("Gems")) {
            Economy.depositPlayer(p.getUniqueId(), add);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return;
        }
        ItemStack[] arritemStack = p.getInventory().getContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack is = arritemStack[n2];
            if (is != null && GemPouches.isGemPouch(is)) {
                if (is.getAmount() != 1) {
                    return;
                }
                int amt = GemPouches.getCurrentValue(is);
                int max = GemPouches.getMaxValue(is);
                if (add > 0 && amt < max) {
                    if (amt + add > max) {
                        ItemStack newis = itemStack;
                        newis.setAmount(add -= max - amt);
                        itemStack = newis;
                        GemPouches.setPouchBal(is, max);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        int adding = max - amt;
                        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "                    +" + ChatColor.GREEN.toString() + adding + ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "G");
                    } else {
                        GemPouches.setPouchBal(is, amt + add);
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "                    +" + ChatColor.GREEN.toString() + add + ChatColor.GREEN + ChatColor.BOLD + "G");
                        add = 0;
                    }
                }
            }
            ++n2;
        }
        if (add > 0) {
            itemStack.setAmount(add);
            Mining.addToInv(p, itemStack);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "                    +" + ChatColor.GREEN.toString() + add + ChatColor.GREEN + ChatColor.BOLD + "G");
        }
        /**
         if(amount != 0){
         ItemMeta gm = gem.getItemMeta();
         gm.setDisplayName(ChatColor.WHITE + "Gem");
         gm.setLore(Arrays.asList(ChatColor.GRAY + "The currency of Andalucia"));
         gem.setItemMeta(gm);
         p.getInventory().addItem(gem);
         amount -= gemAmount;
         }

         }*/
    }
//    ItemStack itemStack = e.getItem().getItemStack();
//        if (itemStack.getType() == Material.EMERALD) {
//        e.getItem().remove();
//
//        p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "                    +" + ChatColor.GREEN + itemStack.getAmount() + ChatColor.GREEN + ChatColor.BOLD + "G");
//        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
//        if(Toggles.getToggles(p.getName()).contains("gems")) {
//            Economy.depositPlayer(p.getUniqueId(), itemStack.getAmount());
//        }else {
//            p.getInventory().addItem(itemStack);
//        }
//    }

    static int getMaxValue(ItemStack is) {
        List<String> lore;
        if (is != null && is.getType() != Material.AIR && is.getType() == Material.INK_SACK && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 0 && ((String) lore.get(0)).contains("g")) {
            try {
                String line = ChatColor.stripColor(lore.get(0));
                return Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1, line.lastIndexOf("g")));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    static int getCurrentValue(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getType() == Material.INK_SACK && is.getItemMeta().hasDisplayName()) {
            try {
                String line = ChatColor.stripColor((String) is.getItemMeta().getDisplayName());
                return Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1, line.lastIndexOf("g")));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    static void setPouchBal(ItemStack is, int bal) {
        if (is.getItemMeta().hasDisplayName()) {
            String name = is.getItemMeta().getDisplayName();
            name = name.substring(0, name.lastIndexOf(" "));
            name = String.valueOf(name) + " " + bal + "g";
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(name);
            is.setItemMeta(im);
        }
    }

    public static boolean isGemPouch(ItemStack is) {
        if (is == null) {
            return false;
        }
        if (is.getType() != Material.INK_SACK) {
            return false;
        }
        if (is.getDurability() != 0) {
            return false;
        }
        if (!is.getItemMeta().hasDisplayName()) {
            return false;
        }
        if (!is.getItemMeta().getDisplayName().contains("g")) {
            return false;
        }
        if (GemPouches.getMaxValue(is) == 0) {
            return false;
        }
        return true;
    }
}

