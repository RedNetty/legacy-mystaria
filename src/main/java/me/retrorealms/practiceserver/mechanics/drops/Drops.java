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

import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Drops {
    public static ItemStack createDrop(int tier, int item) {
        int rarity = randomRarity();
        //TODO
        return CreateDrop.createDrop(tier, item, rarity);
    }
    public static int randomRarity(){
        Random random = new Random();
        int rarity = 1;
        int r = random.nextInt(260);
        if (r < 5) {
            rarity = 4;
        } else if (r < 14) {
            rarity = 3;
        } else if (r < 100) {
            rarity = 2;
        } else if (r < 260) {
            rarity = 1;
        }
        return rarity;
    }

}