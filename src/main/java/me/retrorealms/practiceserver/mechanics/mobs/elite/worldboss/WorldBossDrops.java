package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import me.retrorealms.practiceserver.mechanics.item.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.glow.GlowAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class WorldBossDrops {

    public static ItemStack createDrop(int itemType, String worldbossName) {
        WorldBoss boss = WorldBossHandler.bossFromString(worldbossName);


        Random r = new Random();
        //Initialize Variables
        String name = BossGearGenerator.getString(itemType, "name", worldbossName);
        List<String> itemLore = new ArrayList<>();
        String item_string = CreateDrop.getItem(itemType);
        String mat_string = CreateDrop.getMatString(boss.getTier(), itemType);
        int rarityID = BossGearGenerator.getInt(itemType, "rarity", worldbossName);
        String rarity_string = CreateDrop.getRarity(rarityID);

        //Create Variables
        double min_min = BossGearGenerator.getInt(itemType, "min-min", worldbossName);
        double min_max = BossGearGenerator.getInt(itemType, "min-max", worldbossName);
        double max_min = BossGearGenerator.getInt(itemType, "max-min", worldbossName);
        double max_max = BossGearGenerator.getInt(itemType, "max-max", worldbossName);
        if (min_max <= 0 || min_min <= 0) {
            min_max = 1;
            min_min = 1;
        }// go fuck urself
        int min = 0;
        int max = 0;
        if (CreateDrop.isWeapon(itemType)) {
            min = ThreadLocalRandom.current().nextInt((int) min_min, (int) min_max);
            max = ThreadLocalRandom.current().nextInt((int) max_min, (int) max_max);
        }


        int hp = BossGearGenerator.getHealth(itemType, worldbossName);


        int nrghp = ThreadLocalRandom.current().nextInt(3);
        int nrg = BossGearGenerator.getInt(itemType, "energy", worldbossName);
        int hps = BossGearGenerator.getInt(itemType, "hps", worldbossName);

        int armordpsValue = BossGearGenerator.getInt(itemType, "armor-dps-amt", worldbossName);

        boolean armordps = !CreateDrop.isWeapon(itemType) && BossGearGenerator.isArmor(itemType, worldbossName);

        //Create Item
        ItemStack newItem = new ItemStack(Material.getMaterial(mat_string + "_" + item_string));
        ItemMeta newItemMeta = newItem.getItemMeta();

        if (newItem.getType().toString().contains("LEATHER") && boss.getTier() == 6) {
            newItem = Items.setItemBlueLeather(newItem);
            newItemMeta = newItem.getItemMeta();
        }
        if (CreateDrop.isWeapon(itemType)) {
            itemLore.add(ChatColor.RED + "DMG: " + min + " - " + max);
        } else {
            if (!armordps) {
                itemLore.add(ChatColor.RED + "DPS: " + armordpsValue + " - " + armordpsValue + "%");
            } else {
                itemLore.add(ChatColor.RED + "ARMOR: " + armordpsValue + " - " + armordpsValue + "%");
            }
            itemLore.add(ChatColor.RED + "HP: +" + hp);
            if (hps == 0) {
                itemLore.add(ChatColor.RED + "ENERGY REGEN: +" + nrg + "%");
            } else {
                itemLore.add(ChatColor.RED + "HP REGEN: +" + hps + "/s");
            }
        }
        if (itemType > 4 && boss.getTier() == 6) {
            newItem = Items.setItemBlueLeather(newItem);
        }
        itemLore.add(rarity_string);
        newItemMeta.setLore(itemLore);
        newItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        newItem.setItemMeta(newItemMeta);

        return getItem(rarityID, itemType, newItem, worldbossName);
    }

    public static ItemStack getItem(int rarity, int itemType, ItemStack is, String worldbossName) {
        ItemMeta itemMeta = is.getItemMeta();
        List<String> oldlore = itemMeta.getLore();

        //Weapon Stats
        int pureamt = BossGearGenerator.getInt(itemType, "pure", worldbossName);
        int accamt = BossGearGenerator.getInt(itemType, "acc", worldbossName);
        int lifeamt = BossGearGenerator.getInt(itemType, "life", worldbossName);
        int vsMonstersAmt = BossGearGenerator.getInt(itemType, "vsmonsters", worldbossName);
        int vsPlayersAmt = BossGearGenerator.getInt(itemType, "vsplayers", worldbossName);
        int critamt = BossGearGenerator.getInt(itemType, "crit", worldbossName);
        int iceamt = BossGearGenerator.getInt(itemType, "ice", worldbossName);
        int fireamt = BossGearGenerator.getInt(itemType, "fire", worldbossName);
        int poisonamt = BossGearGenerator.getInt(itemType, "poison", worldbossName);
        //Weapon Stats
        int intamt = BossGearGenerator.getInt(itemType, "int", worldbossName);
        int vitamt = BossGearGenerator.getInt(itemType, "vit", worldbossName);
        int dexamt = BossGearGenerator.getInt(itemType, "dex", worldbossName);
        int thornsamt = BossGearGenerator.getInt(itemType, "thorns", worldbossName);
        int dodgeamt = BossGearGenerator.getInt(itemType, "dodge", worldbossName);
        int blockamt = BossGearGenerator.getInt(itemType, "block", worldbossName);
        int stramt = BossGearGenerator.getInt(itemType, "str", worldbossName);

        List<String> lore = new ArrayList<>();


        if (itemType == 0 || itemType == 1 || itemType == 2 || itemType == 3 || itemType == 4) {
            lore.add(oldlore.get(0));
            if (pureamt > 0) {
                lore.add(ChatColor.RED + "PURE DMG: +" + pureamt);
            }
            if (accamt > 0) {
                lore.add(ChatColor.RED + "ACCURACY: " + accamt + "%");
            }
            if (lifeamt > 0) {
                lore.add(ChatColor.RED + "LIFE STEAL: " + lifeamt + "%");
            }
            if (vsMonstersAmt > 0) {
                lore.add(ChatColor.RED + "VS MONSTERS: " + vsMonstersAmt + "%");
            }
            if (vsPlayersAmt > 0) {
                lore.add(ChatColor.RED + "VS PLAYERS: " + vsPlayersAmt + "%");
            }
            if (critamt > 0) {
                lore.add(ChatColor.RED + "CRITICAL HIT: " + critamt + "%");
            }
            if (iceamt > 0) {
                lore.add(ChatColor.RED + "ICE DMG: +" + iceamt);
            }
            if (poisonamt > 0) {
                lore.add(ChatColor.RED + "POISON DMG: +" + poisonamt);
            }
            if (fireamt > 0) {
                lore.add(ChatColor.RED + "FIRE DMG: +" + fireamt);
            }
        }
        if (itemType == 5 || itemType == 6 || itemType == 7 || itemType == 8) {
            lore.add(oldlore.get(0));
            lore.add(oldlore.get(1));
            lore.add(oldlore.get(2));
            if (intamt > 0) {
                lore.add(ChatColor.RED + "INT: +" + intamt);
            }
            if (stramt > 0) {
                lore.add(ChatColor.RED + "STR: +" + stramt);
            }
            if (vitamt > 0) {
                lore.add(ChatColor.RED + "VIT: +" + vitamt);
            }
            if (dexamt > 0) {
                lore.add(ChatColor.RED + "DEX: +" + dexamt);
            }
            if (dodgeamt > 0) {
                lore.add(ChatColor.RED + "DODGE: " + dodgeamt + "%");
            }
            if (thornsamt > 0) {
                lore.add(ChatColor.RED + "THORNS: " + thornsamt + "%");
            }
            if (blockamt > 0) {
                lore.add(ChatColor.RED + "BLOCK: " + blockamt + "%");
            }
        }
        String loreTag = BossGearGenerator.getString(itemType, "lore", worldbossName);
        if (!loreTag.equals("")) lore.add(ChatColor.GRAY.toString() + loreTag);
        lore.add(oldlore.get(oldlore.size() - 1));

        final ItemMeta im = is.getItemMeta();
        for (ItemFlag itemFlag : ItemFlag.values()) {
            im.addItemFlags(itemFlag);
        }
        im.setDisplayName(itemMeta.getDisplayName());
        im.setLore(lore);
        is.setItemMeta(im);
        is.addUnsafeEnchantment(Enchants.glow, 1);

        NBTAccessor nbtAccessor = new NBTAccessor(is).check();
        nbtAccessor.setDouble("namedElite", 1D);
        return nbtAccessor.update().clone();
    }
}
