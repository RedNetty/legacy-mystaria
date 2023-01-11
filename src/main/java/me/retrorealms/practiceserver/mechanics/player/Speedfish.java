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
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class Speedfish
        implements Listener {
    public void onEnable() {
        PracticeServer.log.info("[Speedfish] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Speedfish] has been disabled.");
    }

    public static ItemStack fish(int tier, boolean inshop) {
        ItemStack is = new ItemStack(Material.RAW_FISH);
        ItemMeta im = is.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        String name = "";
        int price = 0;
        if (tier == 1) {
            price = 25;
            name = ChatColor.WHITE + "Raw Shrimp of Lesser Agility";
            lore.add(ChatColor.RED + "SPEED (I) BUFF " + ChatColor.GRAY + "(15s)");
            lore.add(ChatColor.RED + "-10% HUNGER " + ChatColor.GRAY + "(instant)");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A raw and pink crustacean.");
        }
        if (tier == 2) {
            price = 50;
            name = ChatColor.WHITE + "Raw Herring of Greater Agility";
            lore.add(ChatColor.RED + "SPEED (I) BUFF " + ChatColor.GRAY + "(30s)");
            lore.add(ChatColor.RED + "-20% HUNGER " + ChatColor.GRAY + "(instant)");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A colourful and medium-sized fish.");
        }
        if (tier == 3) {
            price = 100;
            name = ChatColor.AQUA + "Raw Salmon of Lasting Agility";
            lore.add(ChatColor.RED + "SPEED (I) BUFF " + ChatColor.GRAY + "(30s)");
            lore.add(ChatColor.RED + "-30% HUNGER " + ChatColor.GRAY + "(instant)");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "An elongated fish with a long bill.");
        }
        if (tier == 4) {
            price = 200;
            name = ChatColor.LIGHT_PURPLE + "Raw Lobster of Bursting Agility";
            lore.add(ChatColor.RED + "SPEED (II) BUFF " + ChatColor.GRAY + "(15s)");
            lore.add(ChatColor.RED + "-40% HUNGER " + ChatColor.GRAY + "(instant)");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "An elongated fish with a long bill.");
        }
        if (tier == 5) {
            price = 300;
            name = ChatColor.YELLOW + "Raw Swordfish of Godlike Speed";
            lore.add(ChatColor.RED + "SPEED (II) BUFF " + ChatColor.GRAY + "(30s)");
            lore.add(ChatColor.RED + "-50% HUNGER " + ChatColor.GRAY + "(instant)");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "An elongated fish with a long bill.");
        }
        if (inshop) {
            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g");
        }
        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    int getSpeed(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.contains("SPEED")) continue;
                if (line.contains("(I)")) {
                    return 0;
                }
                if (!line.contains("(II)")) continue;
                return 1;
            }
        }
        return 0;
    }

    int getDuration(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.contains("SPEED")) continue;
                try {
                    return Integer.parseInt(line.split("\\(")[2].split("s\\)")[0]);
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    int getHunger(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.contains("HUNGER")) continue;
                try {
                    int amt = Integer.parseInt(line.split("-")[1].split("%")[0]);
                    return amt / 5;
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    @EventHandler
    public void onSpeedFish(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.getInventory().getItemInMainHand().getType() == Material.COOKED_FISH || p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Cooked") && p.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
                if (e.hasBlock() && (e.getClickedBlock().getType() == Material.FURNACE || e.getClickedBlock().getType() == Material.BURNING_FURNACE || e.getClickedBlock().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0.0, 1.0, 0.0)).getType() == Material.FIRE || e.getClickedBlock().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0.0, 1.0, 0.0)).getType() == Material.STATIONARY_LAVA)) {
                    return;
                }
                if (p.hasPotionEffect(PotionEffectType.SPEED)) {
                    e.setCancelled(true);
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, this.getDuration(p.getInventory().getItemInMainHand()) * 20, this.getSpeed(p.getInventory().getItemInMainHand())));
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                    if (p.getFoodLevel() + this.getHunger(p.getInventory().getItemInMainHand()) > 20) {
                        p.setFoodLevel(20);
                    } else {
                        p.setFoodLevel(p.getFoodLevel() + this.getHunger(p.getInventory().getItemInMainHand()));
                    }
                    if(!Duels.duelers.containsKey(p)) {
                        if (p.getInventory().getItemInMainHand().getAmount() > 1) {
                            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            e.setCancelled(true);
                            p.getInventory().setItemInMainHand(null);
                        }
                    }
                }
            } else if (p.getInventory().getItemInMainHand().getType() == Material.RAW_FISH && p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.FURNACE) {
                    return;
                }
                p.sendMessage(ChatColor.YELLOW + "To cook, " + ChatColor.UNDERLINE + "RIGHT CLICK" + ChatColor.YELLOW + " any heat source.");
                p.sendMessage(ChatColor.GRAY + "Ex. Fire, Lava, Furnace");
            }
        }
    }

    @EventHandler
    public void onFishCook(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && (e.getClickedBlock().getType() == Material.FURNACE || e.getClickedBlock().getType() == Material.BURNING_FURNACE || e.getClickedBlock().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0.0, 1.0, 0.0)).getType() == Material.FIRE || e.getClickedBlock().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0.0, 1.0, 0.0)).getType() == Material.STATIONARY_LAVA) && p.getInventory().getItemInMainHand().getType() == Material.RAW_FISH && p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {

            e.setCancelled(true);
            ItemStack is = p.getInventory().getItemInMainHand();
            ItemMeta im = is.getItemMeta();
            String name = im.getDisplayName();

            if (name.contains("Cooked")) {
                p.sendMessage(ChatColor.RED + "This juicy fish flesh has already been cooked.");
                return;
            }

            p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.0f);

            if (!name.contains("Tigerfish"))
                is.setType(Material.RAW_FISH);

            if (name.contains("Andalucian"))
                name = ChatColor.GRAY + "Cooked " + name;
            else
                name = String.valueOf(name.substring(0, 2)) + "Cooked " + name.substring(6, name.length());

            im.setDisplayName(name);
            is.setItemMeta(im);
            if (is.getType() == Material.RAW_FISH) {
                is.setType(Material.COOKED_FISH);
            }
            p.getInventory().setItemInMainHand(is);
        }
    }
}

