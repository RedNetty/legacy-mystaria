/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package me.retrorealms.practiceserver.mechanics.drops;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.enchants.Orbs;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CreateDrop {


    public static ItemStack createDrop(int tier, int item, int rarity) {


        Random r = new Random();
        //Initialize Variables
        String name = setName(item, tier);
        List<String> itemLore = new ArrayList<>();
        ArrayList<Integer> armour_base = setbase(30, 2.1);
        ArrayList<Integer> min_damages = setbase(5, 2);
        ArrayList<Integer> max_damages = setbase(6, 2);
        String item_string = getItem(item);
        String mat_string = getMatString(tier, item);
        String rarity_string = getRarity(rarity);

        //Create Variables
        double min_min = min_damages.get(tier) * ((rarity + .7) / 2.35);
        double min_max = min_min / 4D;
        double max_min = max_damages.get(tier) * ((rarity + .7) / 2.3);
        double max_max = max_min / 4D;
        if(min_max <= 0 || min_min <= 0) {
            min_max = 1;
            min_min = 1;
        }// go fuck urself
        if(tier == 6) {
            min_min = min_min / 1.208;
            min_max = min_max / 1.208;
            max_min = max_min / 1.208;
            max_max = max_max / 1.208;
        }
        int min = r.nextInt((int)min_max) + (int) min_min;
        int max = r.nextInt((int)max_max) + (int) max_min;
        if(rarity == 1 && tier >= 3) {
            min += r.nextInt(15);
            max += r.nextInt(15) + 15;
        }
        if(rarity <= 2) {
            int rand = (r.nextInt(2) + 1) * tier * rarity;
            min += rand;
            max += rand;

        }
        if(min > max) {
            int newMax = min;
            min = max;
            max = newMax;
        }
        min *= PracticeServer.MIN_DAMAGE_MULTIPLIER;
        max *= PracticeServer.MAX_DAMAGE_MULTIPLIER;
        double base_hp = 0;
        if(item == 5 || item == 8) {
            base_hp = (armour_base.get(tier) * (rarity / (1+(rarity / 10D)))) / 1.5D;
        }else{
            base_hp = armour_base.get(tier) * (rarity / (1+(rarity/10D)));
        }
        if(item == 1 || item == 2) {
           double minelm = min / 1.5D;
           min = (int) minelm;
            double maxelm = max / 1.5D;
            max = (int) maxelm;
        }
        int hp = (r.nextInt((int)base_hp /4)  + (int)base_hp);
        if(item == 6 || item == 7) hp *= PracticeServer.HP_MULTIPLIER;

        double dpsmax = tier*(1D+(tier/1.7D));
        double dpsmin = dpsmax / 1.5D;

        int nrghp = ThreadLocalRandom.current().nextInt(3);
        int nrg = tier;
        int hps = 0;
        if(nrghp > 0) {
            int nrgToTake = ThreadLocalRandom.current().nextInt(2);
            int nrgToGive = ThreadLocalRandom.current().nextInt(3);
            nrg = (int) (nrg - nrgToTake + nrgToGive + (ThreadLocalRandom.current().nextInt(tier) / 2));
            if (nrg == 0) nrg += 1;
            if (nrg > 6) nrg = 6;
        }else if(nrghp == 0) {
            hps = (int)(hp / 6.145);
        }

        int dpsi = (int)dpsmin;
        int dpsa = (int)dpsmax;

        int randomDps = r.nextInt(4) + 1;

        //Create Item
        ItemStack newItem = new ItemStack(Material.getMaterial(mat_string + "_" + item_string)) == null ? Items.orb(false).clone() : new ItemStack(Material.getMaterial(mat_string + "_"+item_string));
        ItemMeta newItemMeta = newItem.getItemMeta();

        if(newItem.getType().toString().contains("LEATHER") && tier == 6) {
            newItem = Items.setItemBlueLeather(newItem);
            newItemMeta = newItem.getItemMeta();
        }
        if(isWeapon(item)) {
            itemLore.add(ChatColor.RED + "DMG: " + min + " - " + max);
        }else{
            if(randomDps == 1) {
                itemLore.add(ChatColor.RED + "DPS: " + dpsi + " - " + dpsa + "%");
            }else{
                itemLore.add(ChatColor.RED + "ARMOR: " + dpsi + " - " + dpsa + "%");
            }
            itemLore.add(ChatColor.RED + "HP: +" + hp);
            if(hps == 0) {
                itemLore.add(ChatColor.RED + "ENERGY REGEN: +" + nrg + "%");
            }else{
                itemLore.add(ChatColor.RED + "HP REGEN: +" + hps + "/s");
            }
        }
        if(item > 4 && tier == 6){
            newItem = Items.setItemBlueLeather(newItem);
        }
        itemLore.add(rarity_string);
        newItemMeta.setLore(itemLore);
        newItemMeta.setDisplayName(ChatColor.RESET + name);
        newItem.setItemMeta(newItemMeta);
        newItem = Orbs.randomizeStats(newItem);

        return newItem;
    }

    //Set Base Variables
    public static ArrayList<Integer> setbase(double base, double multiplier){
        ArrayList<Integer> newBase = new ArrayList<Integer>();
        for(int i=1; i<=7; i++){
            newBase.add((int)base);
            base = base * multiplier;
        }
        return newBase;
    }

    //Check Item Type
    public static boolean isWeapon(int type){
        if(type <= 4) {
            return true;
        }else{
            return false;
        }
    }

    //Set Item Type
    public static String getItem(int item){
        switch (item) {
            case 1: return "HOE";
            case 2: return "SPADE";
            case 3: return "SWORD";
            case 4: return "AXE";
            case 5: return "HELMET";
            case 6: return "CHESTPLATE";
            case 7: return "LEGGINGS";
            case 8: return "BOOTS";
        }
        return "error";
    }

    //Set Items Material
    public static String getMatString(int tier, int item){
        switch (tier) {
            case 1:
                if(item <= 4) return "WOOD";
                else return "LEATHER";
            case 2:
                if(item <= 4){
                    return "STONE";
                }else{
                    return "CHAINMAIL";
                }
            case 3: return "IRON";
            case 4: return "DIAMOND";
            case 5: return "GOLD";
            case 6:
                if(item <= 4){
                    return "DIAMOND";
                }else{
                    return "LEATHER";
                }
            case 7:
                if(item <= 4){
                    return "IRON";
                }else {
                    return "LEATHER";
                }
        }
        return "error";
    }

    //Set Items Rarity
    public static String getRarity(int rarity){
        switch (rarity) {
            case 1: return ChatColor.GRAY + "Common";
            case 2: return ChatColor.GREEN + "Uncommon";
            case 3: return ChatColor.AQUA + "Rare";
            case 4: return ChatColor.YELLOW + "Unique";
        }
        return "error";
    }

    //Set Items Name
    public static String setName(int item, int tier){
        switch (tier) {
            case 1:
                switch (item) {
                    case 1: return ChatColor.WHITE +  "Staff";
                    case 2: return ChatColor.WHITE +  "Spear";
                    case 3: return ChatColor.WHITE +  "Shortsword";
                    case 4: return ChatColor.WHITE +  "Hatchet";
                    case 5: return ChatColor.WHITE +  "Leather Coif";
                    case 6: return ChatColor.WHITE +  "Leather Chestplate";
                    case 7: return ChatColor.WHITE +  "Leather Leggings";
                    case 8: return ChatColor.WHITE +  "Leather Boots";
                }
            case 2:
                switch (item) {
                    case 1: return ChatColor.GREEN +  "Battlestaff";
                    case 2: return ChatColor.GREEN +  "Halberd";
                    case 3: return ChatColor.GREEN +  "Broadsword";
                    case 4: return ChatColor.GREEN +  "Great Axe";
                    case 5: return ChatColor.GREEN +  "Medium Helmet";
                    case 6: return ChatColor.GREEN +  "Chainmail";
                    case 7: return ChatColor.GREEN +  "Chainmail Leggings";
                    case 8: return ChatColor.GREEN +  "Chainmail Boots";
                }
            case 3:
                switch (item) {
                    case 1: return ChatColor.AQUA +  "Wizard Staff";
                    case 2: return ChatColor.AQUA +  "Magic Polearm";
                    case 3: return ChatColor.AQUA +  "Magic Sword";
                    case 4: return ChatColor.AQUA +  "War Axe";
                    case 5: return ChatColor.AQUA +  "Full Helmet";
                    case 6: return ChatColor.AQUA +  "Platemail";
                    case 7: return ChatColor.AQUA +  "Platemail Leggings";
                    case 8: return ChatColor.AQUA +  "Platemail Boots";
                }
            case 4:
                return generateNamePrefix(item, "Anchient", ChatColor.LIGHT_PURPLE);
            case 5:
                return generateNamePrefix(item, "Legendary", ChatColor.YELLOW);
            case 6:
                return generateNamePrefix(item, "Frozen", ChatColor.BLUE);
            case 7:
                return generateNamePrefix(item, "Molten", ChatColor.DARK_RED);
        }
        return "Error, talk to owner";
    }

    public static String generateNamePrefix(int item, String name, ChatColor color){
        switch (item) {
            case 1: return color +  name + " Staff";
            case 2: return color +  name + " Polarm";
            case 3: return color +  name + " Sword";
            case 4: return color +  name + " Axe";
            case 5: return color +  name + " Platemail Helmet";
            case 6: return color +  name + " Platemail";
            case 7: return color +  name + " Platemail Leggings";
            case 8: return color +  name + " Platemail Boots";
        }
        return "error in generate name";
    }
}