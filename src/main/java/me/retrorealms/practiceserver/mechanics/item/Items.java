/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package me.retrorealms.practiceserver.mechanics.item;

import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static ItemStack orb(boolean inshop) {
        ItemStack orb = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta orbmeta = orb.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        orbmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Orb of Alteration");
        lore.add(ChatColor.GRAY + "Randomizes stats of selected equipment.");
        if (inshop) {
            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "500g");
        }
        orbmeta.setLore(lore);
        orb.setItemMeta(orbmeta);
        return orb;
    }

    public static ItemStack legendaryOrb(boolean inshop) {
        ItemStack orb = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta orbmeta = orb.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        orbmeta.setDisplayName(ChatColor.YELLOW + "Legendary Orb of Alteration");
        lore.add(ChatColor.GRAY + "Plus 4s Items that have a plus lower than 4.");
        lore.add(ChatColor.GRAY + "It also has a extremely high chance of good orbs.");
        if (inshop) {
            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "32000g");
        }
        orbmeta.setLore(lore);
        orb.setItemMeta(orbmeta);
        orb.addUnsafeEnchantment(Enchants.glow, 1);
        return orb;
    }

    public static org.inventivetalent.glow.GlowAPI.Color getColorFromTier(int tier) {
        org.inventivetalent.glow.GlowAPI.Color color = null;
        switch (tier) {
            case 0:
                return org.inventivetalent.glow.GlowAPI.Color.WHITE;
            case 1:
                return org.inventivetalent.glow.GlowAPI.Color.GREEN;
            case 2:
                return org.inventivetalent.glow.GlowAPI.Color.AQUA;
            case 3:
                return org.inventivetalent.glow.GlowAPI.Color.YELLOW;
        }
        return color;
    }

    public static ItemStack setItemBlueLeather(ItemStack itemStack) {
        LeatherArmorMeta leather = (LeatherArmorMeta) itemStack.getItemMeta();
        leather.setColor(Color.fromRGB(40, 40, 240));
        itemStack.setItemMeta(leather);
        return itemStack;
    }

    public static boolean isBlueLeather(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType().name().contains("LEATHER_")) {
            LeatherArmorMeta leather = (LeatherArmorMeta) itemStack.getItemMeta();
            if (leather.getColor().toString().equals(Color.fromRGB(40, 40, 240).toString())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack signNewCustomItem(final Material m, final String name, final String desc) {
        final ItemStack is = new ItemStack(m);
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        final List<String> new_lore = new ArrayList<String>();
        if (desc.contains(",")) {
            String[] split;
            for (int length = (split = desc.split(",")).length, i = 0; i < length; ++i) {
                final String s = split[i];
                new_lore.add(s);
            }
        } else {
            new_lore.add(desc);
        }
        im.setLore(new_lore);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack enchant(int tier, int type, boolean inshop) {
        ItemStack is = new ItemStack(Material.EMPTY_MAP);
        ItemMeta im = is.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        String name = "";
        int price = 0;
        if (tier == 1) {
            price = 50;
            name = ChatColor.WHITE + " Enchant ";
            if (type == 0) {
                name = String.valueOf(name) + "Wooden";
            }
            if (type == 1) {
                name = String.valueOf(name) + "Leather";
            }
        }
        if (tier == 2) {
            price = 150;
            name = ChatColor.GREEN + " Enchant ";
            if (type == 0) {
                name = String.valueOf(name) + "Stone";
            }
            if (type == 1) {
                name = String.valueOf(name) + "Chainmail";
            }
        }
        if (tier == 3) {
            price = 250;
            name = ChatColor.AQUA + " Enchant Iron";
        }
        if (tier == 4) {
            price = 350;
            name = ChatColor.LIGHT_PURPLE + " Enchant Diamond";
        }
        if (tier == 5) {
            price = 500;
            name = ChatColor.YELLOW + " Enchant Gold";
        }
        if (tier == 6) {
            price = 1000;
            name = ChatColor.BLUE + " Enchant Frozen";
        }
        if (type == 0) {
            //  price = (int) ((double) price * 1.5);
            name = String.valueOf(name) + " Weapon";
            lore.add(ChatColor.RED + "+5% DMG");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Weapon will VANISH if enchant above +3 FAILS.");
        }
        if (type == 1) {
            name = String.valueOf(name) + " Armor";
            lore.add(ChatColor.RED + "+5% HP");
            lore.add(ChatColor.RED + "+5% HP REGEN");
            lore.add(ChatColor.GRAY + "   - OR -");
            lore.add(ChatColor.RED + "+1% ENERGY REGEN");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Armor will VANISH if enchant above +3 FAILS.");
        }
        if (inshop) {
            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g");
        }
        im.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Scroll:" + name);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    public static int getTierFromColor(ItemStack is){
        String name = is.getItemMeta().getDisplayName();
        int tier = 0;
        if(name.contains(ChatColor.WHITE.toString())){
            tier = 1;
        } else if(name.contains(ChatColor.GREEN.toString())){
            tier = 2;
        } else if(name.contains(ChatColor.AQUA.toString())){
            tier = 3;
        } else if(name.contains(ChatColor.LIGHT_PURPLE.toString())){
            tier = 4;
        } else if(name.contains(ChatColor.YELLOW.toString())){
            tier = 5;
        } else if(name.contains(ChatColor.BLUE.toString())){
            tier = 6;
        }
        return tier;
    }

    public static boolean isArmor(ItemStack is) {
        if (is != null) {
            if (is.getType().name().contains("_HELMET")) {
                return true;
            }
            if (is.getType().name().contains("_CHESTPLATE")) {
                return true;
            }
            if (is.getType().name().contains("_LEGGINGS")) {
                return true;
            }
            if (is.getType().name().contains("_BOOTS")) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack setUntradable(ItemStack i){
        ItemMeta meta = i.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(ChatColor.GRAY + "Untradeable");
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i;
    }

    public static boolean isItemTradeable(final ItemStack i) {
        if (i != null && i.hasItemMeta() && i.getItemMeta().hasLore()) {
            final List<String> lore = i.getItemMeta().getLore();
            for (final String s : lore) {
                if (ChatColor.stripColor(s).toLowerCase().equalsIgnoreCase("untradeable")
                        || ChatColor.stripColor(s).toLowerCase().equalsIgnoreCase("permanent untradeable")) {
                    return false;
                }
            }
        }
        return true;
    }

}

