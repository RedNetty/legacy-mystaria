package me.retrorealms.practiceserver.mechanics.enchants;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Orbs implements Listener {

    public static int getItemTier(final ItemStack is) {
        if (is.getType().name().contains("WOOD_") || (is.getType().name().contains("LEATHER_") && !Items.isBlueLeather(is))) {
            return 1;
        }
        if (is.getType().name().contains("STONE_") || is.getType().name().contains("CHAINMAIL_")) {
            return 2;
        }
        if (is.getType().name().contains("IRON_")) {
            return 3;
        }
        if (is.getType().name().contains("DIAMOND_") && !is.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString())) {
            return 4;
        }
        if (is.getType().name().contains("GOLD_")) {
            return 5;
        }
        if (is.getType().name().contains("DIAMOND_") || is.getType().name().contains("LEATHER_")) {
            return 6;
        }
        return 0;
    }

    public static int getItemType(final ItemStack is) {
        if (is.getType().name().contains("_HOE")) {
            return 0;
        }
        if (is.getType().name().contains("_SPADE")) {
            return 1;
        }
        if (is.getType().name().contains("_SWORD")) {
            return 2;
        }
        if (is.getType().name().contains("_AXE")) {
            return 3;
        }
        if (is.getType().name().contains("_HELMET")) {
            return 4;
        }
        if (is.getType().name().contains("_CHESTPLATE")) {
            return 5;
        }
        if (is.getType().name().contains("_LEGGINGS")) {
            return 6;
        }
        if (is.getType().name().contains("_BOOTS")) {
            return 7;
        }
        return -1;
    }

    public static ItemStack randomizeStats(final ItemStack is) {
        String name = "";
        String oldName = is.getItemMeta().getDisplayName();
        ArrayList<String> rare = new ArrayList<>();
        int tier = getItemTier(is);
        final int item = getItemType(is);
        final List<String> oldlore = is.getItemMeta().getLore();
        final List<String> lore = new ArrayList<>();
        final Random random = new Random();
        final int elem = random.nextInt(3) + 1;
        final int pure = random.nextInt(3) + 1;
        final int life = random.nextInt(5) + 1;
        final int vsMonsters = random.nextInt(8) + 1;
        final int vsPlayers = random.nextInt(8) + 1;
        final int crit = random.nextInt(3) + 1;
        final int acc = random.nextInt(4) + 1;
        final int dodge = random.nextInt(3) + 1;
        final int block = random.nextInt(3) + 1;
        final int vit = random.nextInt(3) + 1;
        final int str = random.nextInt(3) + 1;
        final int intel = random.nextInt(3) + 1;
        final int dex = random.nextInt(3) + 1;
        final int thorns = random.nextInt(3) + 1;

        int dodgeamt = 0, blockamt = 0, vitamt = 0, vsMonstersAmt = 0, vsPlayersAmt = 0, stramt = 0, intamt = 0, dexamt = 0, elemamt = 0, pureamt = 0, lifeamt = 0, critamt = 0, thornsamt = 0, accamt = 0;


        int extra = ItemAPI.isProtected(is) ? 2 : 0;


        if (WepTrak.isStatTrak(is)) {
            for (int i = oldlore.size() - 7 - extra; i < oldlore.size(); i++) {
                String line = oldlore.get(i);
                if (line.contains("Normal Orbs Used: ")) {
                    int current = Integer.parseInt(line.split(": " + ChatColor.AQUA)[1]);
                    rare.add(ChatColor.GOLD + "Normal Orbs Used: " + ChatColor.AQUA + (current + 1));
                } else {
                    rare.add(oldlore.get(i));
                }

            }
        } else {
            for (int i = oldlore.size() - 1 - extra; i < oldlore.size(); i++) {
                if (oldlore.get(i).contains(ChatColor.GRAY.toString()) && !oldlore.get(i).toLowerCase().contains("common") && !oldlore.get(i).toLowerCase().contains("untradeable")) {
                    i--;
                }
                rare.add(oldlore.get(i));
            }
        }
        if (tier == 1) {
            tier = 1;
            dodgeamt = random.nextInt(5) + 1;
            blockamt = random.nextInt(5) + 1;
            vsMonstersAmt = random.nextInt(4) + 1;
            vsPlayersAmt = random.nextInt(4) + 1;
            vitamt = random.nextInt(15) + 1;
            stramt = random.nextInt(15) + 1;
            intamt = random.nextInt(15) + 1;
            dexamt = random.nextInt(15) + 1;
            elemamt = random.nextInt(4) + 1;
            pureamt = random.nextInt(4) + 1;
            lifeamt = random.nextInt(30) + 1;
            critamt = random.nextInt(3) + 1;
            accamt = random.nextInt(10) + 1;
            thornsamt = random.nextInt(2) + 1;
            if (item == 0) {
                name = "Staff";
                is.setType(Material.WOOD_HOE);
            }
            if (item == 1) {
                name = "Spear";
                is.setType(Material.WOOD_SPADE);
            }
            if (item == 2) {
                name = "Shortsword";
                is.setType(Material.WOOD_SWORD);
            }
            if (item == 3) {
                name = "Hatchet";
                is.setType(Material.WOOD_AXE);
            }
            if (item == 4) {
                name = "Leather Coif";
                is.setType(Material.LEATHER_HELMET);
            }
            if (item == 5) {
                name = "Leather Chestplate";
                is.setType(Material.LEATHER_CHESTPLATE);
            }
            if (item == 6) {
                name = "Leather Leggings";
                is.setType(Material.LEATHER_LEGGINGS);
            }
            if (item == 7) {
                name = "Leather Boots";
                is.setType(Material.LEATHER_BOOTS);
            }
        }
        if (tier == 2) {
            tier = 2;
            dodgeamt = random.nextInt(8) + 1;
            blockamt = random.nextInt(8) + 1;
            vitamt = random.nextInt(35) + 1;
            dexamt = random.nextInt(35) + 1;
            stramt = random.nextInt(35) + 1;
            vsMonstersAmt = random.nextInt(5) + 1;
            vsPlayersAmt = random.nextInt(4) + 1;
            intamt = random.nextInt(35) + 1;
            elemamt = random.nextInt(9) + 1;
            pureamt = random.nextInt(9) + 1;
            lifeamt = random.nextInt(15) + 1;
            critamt = random.nextInt(6) + 1;
            accamt = random.nextInt(12) + 1;
            thornsamt = random.nextInt(3) + 1;
            if (item == 0) {
                name = "Battletaff";
                is.setType(Material.STONE_HOE);
            }
            if (item == 1) {
                name = "Halberd";
                is.setType(Material.STONE_SPADE);
            }
            if (item == 2) {
                name = "Broadsword";
                is.setType(Material.STONE_SWORD);
            }
            if (item == 3) {
                name = "Great Axe";
                is.setType(Material.STONE_AXE);
            }
            if (item == 4) {
                name = "Medium Helmet";
                is.setType(Material.CHAINMAIL_HELMET);
            }
            if (item == 5) {
                name = "Chainmail";
                is.setType(Material.CHAINMAIL_CHESTPLATE);
            }
            if (item == 6) {
                name = "Chainmail Leggings";
                is.setType(Material.CHAINMAIL_LEGGINGS);
            }
            if (item == 7) {
                name = "Chainmail Boots";
                is.setType(Material.CHAINMAIL_BOOTS);
            }
        }
        if (tier == 3) {
            tier = 3;
            dodgeamt = random.nextInt(10) + 1;
            blockamt = random.nextInt(10) + 1;
            vitamt = random.nextInt(75) + 1;
            dexamt = random.nextInt(75) + 1;
            stramt = random.nextInt(75) + 1;
            vsMonstersAmt = random.nextInt(8) + 1;
            vsPlayersAmt = random.nextInt(7) + 1;
            intamt = random.nextInt(75) + 1;
            elemamt = random.nextInt(15) + 1;
            pureamt = random.nextInt(15) + 1;
            lifeamt = random.nextInt(12) + 1;
            critamt = random.nextInt(8) + 1;
            accamt = random.nextInt(25) + 1;
            thornsamt = random.nextInt(4) + 1;
            if (item == 0) {
                name = "Wizard Staff";
                is.setType(Material.IRON_HOE);
            }
            if (item == 1) {
                name = "Magic Polearm";
                is.setType(Material.IRON_SPADE);
            }
            if (item == 2) {
                name = "Magic Sword";
                is.setType(Material.IRON_SWORD);
            }
            if (item == 3) {
                name = "War Axe";
                is.setType(Material.IRON_AXE);
            }
            if (item == 4) {
                name = "Full Helmet";
                is.setType(Material.IRON_HELMET);
            }
            if (item == 5) {
                name = "Platemail";
                is.setType(Material.IRON_CHESTPLATE);
            }
            if (item == 6) {
                name = "Platemail Leggings";
                is.setType(Material.IRON_LEGGINGS);
            }
            if (item == 7) {
                name = "Platemail Boots";
                is.setType(Material.IRON_BOOTS);
            }
        }
        if (tier == 4) {
            tier = 4;
            dodgeamt = random.nextInt(12) + 1;
            blockamt = random.nextInt(12) + 1;
            vsMonstersAmt = random.nextInt(10) + 1;
            vsPlayersAmt = random.nextInt(9) + 1;
            vitamt = random.nextInt(115) + 1;
            dexamt = random.nextInt(115) + 1;
            stramt = random.nextInt(115) + 1;
            intamt = random.nextInt(115) + 1;
            elemamt = random.nextInt(25) + 1;
            pureamt = random.nextInt(25) + 1;
            lifeamt = random.nextInt(10) + 1;
            critamt = random.nextInt(10) + 1;
            accamt = random.nextInt(28) + 1;
            thornsamt = random.nextInt(5) + 1;
            if (item == 0) {
                name = "Ancient Staff";
                is.setType(Material.DIAMOND_HOE);
            }
            if (item == 1) {
                name = "Ancient Polearm";
                is.setType(Material.DIAMOND_SPADE);
            }
            if (item == 2) {
                name = "Ancient Sword";
                is.setType(Material.DIAMOND_SWORD);
            }
            if (item == 3) {
                name = "Ancient Axe";
                is.setType(Material.DIAMOND_AXE);
            }
            if (item == 4) {
                name = "Ancient Full Helmet";
                is.setType(Material.DIAMOND_HELMET);
            }
            if (item == 5) {
                name = "Magic Platemail";
                is.setType(Material.DIAMOND_CHESTPLATE);
            }
            if (item == 6) {
                name = "Magic Platemail Leggings";
                is.setType(Material.DIAMOND_LEGGINGS);
            }
            if (item == 7) {
                name = "Magic Platemail Boots";
                is.setType(Material.DIAMOND_BOOTS);
            }
        }
        if (tier == 5) {
            tier = 5;
            dodgeamt = random.nextInt(12) + 1;
            blockamt = random.nextInt(12) + 1;
            vitamt = random.nextInt(150) + 100;
            dexamt = random.nextInt(150) + 100;
            stramt = random.nextInt(150) + 100;
            intamt = random.nextInt(150) + 100;
            vsMonstersAmt = random.nextInt(12) + 1;
            vsPlayersAmt = random.nextInt(12) + 1;
            elemamt = random.nextInt(20) + 25;
            pureamt = random.nextInt(20) + 25;
            lifeamt = random.nextInt(4) + 4;
            critamt = random.nextInt(5) + 6;
            accamt = random.nextInt(10) + 25;
            thornsamt = random.nextInt(3) + 2;
            if (item == 0) {
                name = "Legendary Staff";
                is.setType(Material.GOLD_HOE);
            }
            if (item == 1) {
                name = "Legendary Polearm";
                is.setType(Material.GOLD_SPADE);
            }
            if (item == 2) {
                name = "Legendary Sword";
                is.setType(Material.GOLD_SWORD);
            }
            if (item == 3) {
                name = "Legendary Axe";
                is.setType(Material.GOLD_AXE);
            }
            if (item == 4) {
                name = "Legendary Full Helmet";
                is.setType(Material.GOLD_HELMET);
            }
            if (item == 5) {
                name = "Legendary Platemail";
                is.setType(Material.GOLD_CHESTPLATE);
            }
            if (item == 6) {
                name = "Legendary Platemail Leggings";
                is.setType(Material.GOLD_LEGGINGS);
            }
            if (item == 7) {
                name = "Legendary Platemail Boots";
                is.setType(Material.GOLD_BOOTS);
            }
        }
        if (tier == 6) {
            tier = 6;
            dodgeamt = random.nextInt(13) + 1;
            blockamt = random.nextInt(13) + 1;
            vitamt = random.nextInt(250) + 100;
            dexamt = random.nextInt(250) + 100;
            stramt = random.nextInt(250) + 100;
            intamt = random.nextInt(250) + 100;
            vsMonstersAmt = random.nextInt(6) + 6;
            vsPlayersAmt = random.nextInt(6) + 6;
            elemamt = random.nextInt(30) + 40;
            pureamt = random.nextInt(30) + 40;
            lifeamt = random.nextInt(4) + 4;
            critamt = random.nextInt(5) + 6;
            accamt = random.nextInt(20) + 20;
            thornsamt = random.nextInt(2) + 3;
            if (item == 0) {
                name = "Frozen Staff";
                is.setType(Material.DIAMOND_HOE);
            }
            if (item == 1) {
                name = "Frozen Polearm";
                is.setType(Material.DIAMOND_SPADE);
            }
            if (item == 2) {
                name = "Frozen Sword";
                is.setType(Material.DIAMOND_SWORD);
            }
            if (item == 3) {
                name = "Frozen Axe";
                is.setType(Material.DIAMOND_AXE);
            }
            if (item == 4) {
                name = "Frozen Full Helmet";
                is.setType(Material.LEATHER_HELMET);
            }
            if (item == 5) {
                name = "Frozen Platemail";
                is.setType(Material.LEATHER_CHESTPLATE);
            }
            if (item == 6) {
                name = "Frozen Platemail Leggings";
                is.setType(Material.LEATHER_LEGGINGS);
            }
            if (item == 7) {
                name = "Frozen Platemail Boots";
                is.setType(Material.LEATHER_BOOTS);
            }
            if (item > 3) Items.setItemBlueLeather(is);
        }
        if (item == 0 || item == 1 || item == 2 || item == 3) {
            lore.add(oldlore.get(0));
            if (item == 3 && pure == 1) {
                lore.add(ChatColor.RED + "PURE DMG: +" + pureamt);
                name = "Pure " + name;
            }
            if (item == 2 && acc == 1) {
                lore.add(ChatColor.RED + "ACCURACY: " + accamt + "%");
                name = "Accurate " + name;
            }
            if (vsMonsters == 1) {
                lore.add(ChatColor.RED + "VS MONSTERS: " + vsMonstersAmt + "%");
            }
            if (vsPlayers == 1) {
                lore.add(ChatColor.RED + "VS PLAYERS: " + vsPlayersAmt + "%");
            }
            if (life == 1) {
                lore.add(ChatColor.RED + "LIFE STEAL: " + lifeamt + "%");
                name = "Vampyric " + name;
            }
            if (crit == 1) {
                lore.add(ChatColor.RED + "CRITICAL HIT: " + critamt + "%");
                name = "Deadly " + name;
            }
            if (elem == 3) {
                lore.add(ChatColor.RED + "ICE DMG: +" + elemamt);
                name = name + " of Ice";
            }
            if (elem == 2) {
                lore.add(ChatColor.RED + "POISON DMG: +" + elemamt);
                name = name + " of Poison";
            }
            if (elem == 1) {
                lore.add(ChatColor.RED + "FIRE DMG: +" + elemamt);
                name = name + " of Fire";
            }
        }
        if (item == 4 || item == 5 || item == 6 || item == 7) {
            lore.add(oldlore.get(0));
            lore.add(oldlore.get(1));
            lore.add(oldlore.get(2));
            if (intel == 1) {
                lore.add(ChatColor.RED + "INT: +" + intamt);
            }
            if (str == 1) {
                lore.add(ChatColor.RED + "STR: +" + stramt);
            }
            if (vit == 1) {
                lore.add(ChatColor.RED + "VIT: +" + vitamt);
            }
            if (dex == 1) {
                lore.add(ChatColor.RED + "DEX: +" + dexamt);
            }
            if (dodge == 1) {
                lore.add(ChatColor.RED + "DODGE: " + dodgeamt + "%");
            }
            if (thorns == 1) {
                lore.add(ChatColor.RED + "THORNS: " + thornsamt + "%");
            }
            if (oldlore.get(2).contains("HP REGEN:")) {
                name = "Mending " + name;
            }
            if (block == 1) {
                lore.add(ChatColor.RED + "BLOCK: " + blockamt + "%");
            }
            if (oldlore.get(2).contains("ENERGY REGEN:")) {
                name = name + " of Fortitude";
            }
        }
        int plus = Enchants.getPlus(is);
        NBTAccessor nbt = new NBTAccessor(is).check();
        if (oldName != null && nbt.getInteger("namedElite") == 1) {
            name = oldName;
            for (String line : oldlore) {
                if (line.startsWith(ChatColor.GRAY.toString())) {
                    lore.add(line);
                }
            }
        }


        lore.addAll(rare);
        if (tier == 1) {
            name = ChatColor.WHITE + name;
        }
        if (tier == 2) {
            name = ChatColor.GREEN + name;
        }
        if (tier == 3) {
            name = ChatColor.AQUA + name;
        }
        if (tier == 4) {
            name = ChatColor.LIGHT_PURPLE + name;
        }
        if (tier == 5) {
            name = ChatColor.YELLOW + name;
        }
        if (tier == 6) {
            name = ChatColor.BLUE + name;
        }

        if (ChatColor.stripColor(name).contains("[+")) {
            name = name.replace("[+" + plus + "]", "[+" + plus + "]");
        } else if (!ChatColor.stripColor(name).contains("[+") && plus >= 1) {
            name = ChatColor.RED + "[+" + Enchants.getPlus(is) + "] " + name;
        }
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    public static boolean hasLoreLine(ItemStack stack) {
        final List<String> oldlore = stack.getItemMeta().getLore();
        for (String line : oldlore) {
            if (line.contains(ChatColor.GRAY.toString()) && line.contains(ChatColor.ITALIC.toString())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack randomizeLegendaryStats(final ItemStack is, Player p) {
        String name = "";
        String oldName = is.getItemMeta().getDisplayName();
        List<String> rare = new ArrayList<>();
        int tier = getItemTier(is);
        final int item = getItemType(is);
        final List<String> oldlore = is.getItemMeta().getLore();
        final List<String> lore = new ArrayList<String>();
        final Random random = new Random();
        final int elem = random.nextInt(3) + 1;
        int vsPlayers = random.nextInt(2) + 1;
        int vsMonsters = random.nextInt(2) + 1;
        final int pure = random.nextInt(2) + 1;
        final int life = random.nextInt(2) + 1;
        final int crit = random.nextInt(1) + 1;
        final int acc = random.nextInt(2) + 1;
        final int dodge = random.nextInt(2) + 1;
        final int block = random.nextInt(2) + 1;
        final int vit = random.nextInt(2) + 1;
        final int str = random.nextInt(2) + 1;
        final int intel = random.nextInt(2) + 1;
        final int dex = random.nextInt(2) + 1;
        final int thorns = random.nextInt(2) + 1;
        int dodgeamt = 0;
        int blockamt = 0;
        int vitamt = 0;
        int dexamt = 0;
        int stramt = 0;
        int vsPlayersAmt = 0;
        int vsMonstersAmt = 0;
        int intamt = 0;
        int elemamt = 0;
        int pureamt = 0;
        int lifeamt = 0;
        int critamt = 0;
        int thornsamt = 0;
        int accamt = 0;
        int dodgeamt2 = 0;
        int blockamt2 = 0;
        int vitamt2 = 0;
        int dexamt2 = 0;
        int stramt2 = 0;
        int vsPlayersAmt2 = 0;
        int vsMonstersAmt2 = 0;
        int intamt2 = 0;
        int elemamt2 = 0;
        int pureamt2 = 0;
        int lifeamt2 = 0;
        int critamt2 = 0;
        int thornsamt2 = 0;
        int accamt2 = 0;
        int extra = 0;
        if (ItemAPI.isProtected(is)) {
            extra = 2;
        }
        if (hasLoreLine(is)) {
            extra = 1;
        }

        if (WepTrak.isStatTrak(is)) {
            for (int i = oldlore.size() - 7 - extra; i < oldlore.size(); i++) {
                String line = oldlore.get(i);
                if (line.contains("Normal Orbs Used: ")) {
                    int current = Integer.parseInt(line.split(": " + ChatColor.AQUA)[1]);
                    rare.add(ChatColor.GOLD + "Normal Orbs Used: " + ChatColor.AQUA + (current + 1));
                } else {
                    rare.add(oldlore.get(i));
                }

            }
        } else {
            for (int i = oldlore.size() - 1 - extra; i < oldlore.size(); i++) {
                try {
                    if (oldlore.get(i).contains(ChatColor.GRAY.toString()) && !oldlore.get(i).toLowerCase().contains("common") && !oldlore.get(i).toLowerCase().contains("untradeable")) {
                        i--;
                    }
                } catch (Exception e) {
                }
                rare.add(oldlore.get(i));
            }
        }
        if (tier == 1) {
            dodgeamt = random.nextInt(5) + 1;
            blockamt = random.nextInt(5) + 1;
            vsMonstersAmt = random.nextInt(4) + 1;
            vsPlayersAmt = random.nextInt(4) + 1;
            vitamt = random.nextInt(15) + 1;
            dexamt = random.nextInt(15) + 1;
            stramt = random.nextInt(15) + 1;
            intamt = random.nextInt(15) + 1;
            elemamt = random.nextInt(4) + 1;
            pureamt = random.nextInt(4) + 1;
            lifeamt = random.nextInt(30) + 1;
            critamt = random.nextInt(3) + 1;
            accamt = random.nextInt(10) + 1;
            thornsamt = random.nextInt(2) + 1;
            if (item == 0) {
                name = "Staff";
                is.setType(Material.WOOD_HOE);
            }
            if (item == 1) {
                name = "Spear";
                is.setType(Material.WOOD_SPADE);
            }
            if (item == 2) {
                name = "Shortsword";
                is.setType(Material.WOOD_SWORD);
            }
            if (item == 3) {
                name = "Hatchet";
                is.setType(Material.WOOD_AXE);
            }
            if (item == 4) {
                name = "Leather Coif";
                is.setType(Material.LEATHER_HELMET);
            }
            if (item == 5) {
                name = "Leather Chestplate";
                is.setType(Material.LEATHER_CHESTPLATE);
            }
            if (item == 6) {
                name = "Leather Leggings";
                is.setType(Material.LEATHER_LEGGINGS);
            }
            if (item == 7) {
                name = "Leather Boots";
                is.setType(Material.LEATHER_BOOTS);
            }
        }
        if (tier == 2) {
            tier = 2;
            dodgeamt = random.nextInt(8) + 1;
            blockamt = random.nextInt(8) + 1;
            vitamt = random.nextInt(35) + 1;
            dexamt = random.nextInt(35) + 1;
            stramt = random.nextInt(35) + 1;
            vsMonstersAmt = random.nextInt(5) + 1;
            vsPlayersAmt = random.nextInt(4) + 1;
            intamt = random.nextInt(35) + 1;
            elemamt = random.nextInt(9) + 1;
            pureamt = random.nextInt(9) + 1;
            lifeamt = random.nextInt(15) + 1;
            critamt = random.nextInt(6) + 1;
            accamt = random.nextInt(12) + 1;
            thornsamt = random.nextInt(3) + 1;
            PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
            for (int i = 0; i < pp.orbrolls; i++) {
                dodgeamt2 = random.nextInt(8) + 1;
                blockamt2 = random.nextInt(8) + 1;
                vitamt2 = random.nextInt(35) + 1;
                dexamt2 = random.nextInt(35) + 1;
                stramt2 = random.nextInt(35) + 1;
                vsMonstersAmt2 = random.nextInt(5) + 1;
                vsPlayersAmt2 = random.nextInt(4) + 1;
                intamt2 = random.nextInt(35) + 1;
                elemamt2 = random.nextInt(9) + 1;
                pureamt2 = random.nextInt(9) + 1;
                lifeamt2 = random.nextInt(15) + 1;
                critamt2 = random.nextInt(6) + 1;
                accamt2 = random.nextInt(12) + 1;
                thornsamt2 = random.nextInt(3) + 1;
                if (dodgeamt2 > dodgeamt) dodgeamt = dodgeamt2;
                if (blockamt2 > blockamt) blockamt = blockamt2;
                if (vsMonstersAmt2 > vsMonstersAmt) vsMonstersAmt = vsMonstersAmt2;
                if (vsPlayersAmt2 > vsPlayersAmt) vsPlayersAmt = vsPlayersAmt2;
                if (vitamt2 > vitamt) vitamt = vitamt2;
                if (dexamt2 > dexamt) dexamt = dexamt2;
                if (stramt2 > stramt) stramt = stramt2;
                if (intamt2 > intamt) intamt = intamt2;
                if (elemamt2 > elemamt) elemamt = elemamt2;
                if (pureamt2 > pureamt) pureamt = pureamt2;
                if (lifeamt2 > lifeamt) lifeamt = lifeamt2;
                if (critamt2 > critamt) critamt = critamt2;
                if (accamt2 > accamt) accamt = accamt2;
                if (thornsamt2 > thornsamt) thornsamt = thornsamt2;
            }
            if (item == 0) {
                name = "Battletaff";
                is.setType(Material.STONE_HOE);
            }
            if (item == 1) {
                name = "Halberd";
                is.setType(Material.STONE_SPADE);
            }
            if (item == 2) {
                name = "Broadsword";
                is.setType(Material.STONE_SWORD);
            }
            if (item == 3) {
                name = "Great Axe";
                is.setType(Material.STONE_AXE);
            }
            if (item == 4) {
                name = "Medium Helmet";
                is.setType(Material.CHAINMAIL_HELMET);
            }
            if (item == 5) {
                name = "Chainmail";
                is.setType(Material.CHAINMAIL_CHESTPLATE);
            }
            if (item == 6) {
                name = "Chainmail Leggings";
                is.setType(Material.CHAINMAIL_LEGGINGS);
            }
            if (item == 7) {
                name = "Chainmail Boots";
                is.setType(Material.CHAINMAIL_BOOTS);
            }
        }
        if (tier == 3) {
            tier = 3;
            dodgeamt = random.nextInt(10) + 1;
            blockamt = random.nextInt(10) + 1;
            vitamt = random.nextInt(75) + 1;
            dexamt = random.nextInt(75) + 1;
            stramt = random.nextInt(75) + 1;
            vsMonstersAmt = random.nextInt(8) + 1;
            vsPlayersAmt = random.nextInt(7) + 1;
            intamt = random.nextInt(75) + 1;
            elemamt = random.nextInt(15) + 1;
            pureamt = random.nextInt(15) + 1;
            lifeamt = random.nextInt(12) + 1;
            critamt = random.nextInt(8) + 1;
            accamt = random.nextInt(25) + 1;
            thornsamt = random.nextInt(4) + 1;
            PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
            for (int i = 0; i < pp.orbrolls; i++) {
                dodgeamt2 = random.nextInt(10) + 1;
                blockamt2 = random.nextInt(10) + 1;
                vitamt2 = random.nextInt(75) + 1;
                dexamt2 = random.nextInt(75) + 1;
                stramt2 = random.nextInt(75) + 1;
                vsMonstersAmt2 = random.nextInt(8) + 1;
                vsPlayersAmt2 = random.nextInt(7) + 1;
                intamt2 = random.nextInt(75) + 1;
                elemamt2 = random.nextInt(15) + 1;
                pureamt2 = random.nextInt(15) + 1;
                lifeamt2 = random.nextInt(12) + 1;
                critamt2 = random.nextInt(8) + 1;
                accamt2 = random.nextInt(25) + 1;
                thornsamt2 = random.nextInt(4) + 1;
                if (dodgeamt2 > dodgeamt) dodgeamt = dodgeamt2;
                if (blockamt2 > blockamt) blockamt = blockamt2;
                if (vsMonstersAmt2 > vsMonstersAmt) vsMonstersAmt = vsMonstersAmt2;
                if (vsPlayersAmt2 > vsPlayersAmt) vsPlayersAmt = vsPlayersAmt2;
                if (vitamt2 > vitamt) vitamt = vitamt2;
                if (dexamt2 > dexamt) dexamt = dexamt2;
                if (stramt2 > stramt) stramt = stramt2;
                if (intamt2 > intamt) intamt = intamt2;
                if (elemamt2 > elemamt) elemamt = elemamt2;
                if (pureamt2 > pureamt) pureamt = pureamt2;
                if (lifeamt2 > lifeamt) lifeamt = lifeamt2;
                if (critamt2 > critamt) critamt = critamt2;
                if (accamt2 > accamt) accamt = accamt2;
                if (thornsamt2 > thornsamt) thornsamt = thornsamt2;
            }
            if (item == 0) {
                name = "Wizard Staff";
                is.setType(Material.IRON_HOE);
            }
            if (item == 1) {
                name = "Magic Polearm";
                is.setType(Material.IRON_SPADE);
            }
            if (item == 2) {
                name = "Magic Sword";
                is.setType(Material.IRON_SWORD);
            }
            if (item == 3) {
                name = "War Axe";
                is.setType(Material.IRON_AXE);
            }
            if (item == 4) {
                name = "Full Helmet";
                is.setType(Material.IRON_HELMET);
            }
            if (item == 5) {
                name = "Platemail";
                is.setType(Material.IRON_CHESTPLATE);
            }
            if (item == 6) {
                name = "Platemail Leggings";
                is.setType(Material.IRON_LEGGINGS);
            }
            if (item == 7) {
                name = "Platemail Boots";
                is.setType(Material.IRON_BOOTS);
            }
        }
        if (tier == 4) {
            tier = 4;
            dodgeamt = random.nextInt(12) + 1;
            blockamt = random.nextInt(12) + 1;
            vsMonstersAmt = random.nextInt(10) + 1;
            vsPlayersAmt = random.nextInt(9) + 1;
            vitamt = random.nextInt(115) + 1;
            dexamt = random.nextInt(115) + 1;
            stramt = random.nextInt(115) + 1;
            intamt = random.nextInt(115) + 1;
            elemamt = random.nextInt(25) + 1;
            pureamt = random.nextInt(25) + 1;
            lifeamt = random.nextInt(10) + 1;
            critamt = random.nextInt(10) + 1;
            accamt = random.nextInt(28) + 1;
            thornsamt = random.nextInt(5) + 1;
            PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
            for (int i = 0; i < pp.orbrolls; i++) {
                dodgeamt2 = random.nextInt(12) + 1;
                blockamt2 = random.nextInt(12) + 1;
                vsMonstersAmt2 = random.nextInt(10) + 1;
                vsPlayersAmt2 = random.nextInt(9) + 1;
                vitamt2 = random.nextInt(115) + 1;
                dexamt2 = random.nextInt(115) + 1;
                stramt2 = random.nextInt(115) + 1;
                intamt2 = random.nextInt(115) + 1;
                elemamt2 = random.nextInt(25) + 1;
                pureamt2 = random.nextInt(25) + 1;
                lifeamt2 = random.nextInt(10) + 1;
                critamt2 = random.nextInt(10) + 1;
                accamt2 = random.nextInt(28) + 1;
                thornsamt2 = random.nextInt(5) + 1;
                if (dodgeamt2 > dodgeamt) dodgeamt = dodgeamt2;
                if (blockamt2 > blockamt) blockamt = blockamt2;
                if (vsMonstersAmt2 > vsMonstersAmt) vsMonstersAmt = vsMonstersAmt2;
                if (vsPlayersAmt2 > vsPlayersAmt) vsPlayersAmt = vsPlayersAmt2;
                if (vitamt2 > vitamt) vitamt = vitamt2;
                if (dexamt2 > dexamt) dexamt = dexamt2;
                if (stramt2 > stramt) stramt = stramt2;
                if (intamt2 > intamt) intamt = intamt2;
                if (elemamt2 > elemamt) elemamt = elemamt2;
                if (pureamt2 > pureamt) pureamt = pureamt2;
                if (lifeamt2 > lifeamt) lifeamt = lifeamt2;
                if (critamt2 > critamt) critamt = critamt2;
                if (accamt2 > accamt) accamt = accamt2;
                if (thornsamt2 > thornsamt) thornsamt = thornsamt2;
            }
            if (item == 0) {
                name = "Ancient Staff";
                is.setType(Material.DIAMOND_HOE);
            }
            if (item == 1) {
                name = "Ancient Polearm";
                is.setType(Material.DIAMOND_SPADE);
            }
            if (item == 2) {
                name = "Ancient Sword";
                is.setType(Material.DIAMOND_SWORD);
            }
            if (item == 3) {
                name = "Ancient Axe";
                is.setType(Material.DIAMOND_AXE);
            }
            if (item == 4) {
                name = "Ancient Full Helmet";
                is.setType(Material.DIAMOND_HELMET);
            }
            if (item == 5) {
                name = "Magic Platemail";
                is.setType(Material.DIAMOND_CHESTPLATE);
            }
            if (item == 6) {
                name = "Magic Platemail Leggings";
                is.setType(Material.DIAMOND_LEGGINGS);
            }
            if (item == 7) {
                name = "Magic Platemail Boots";
                is.setType(Material.DIAMOND_BOOTS);
            }
        }
        if (tier == 5) {
            tier = 5;
            dodgeamt = random.nextInt(6) + 6;
            blockamt = random.nextInt(6) + 6;
            vitamt = random.nextInt(130) + 120;
            dexamt = random.nextInt(130) + 120;
            stramt = random.nextInt(130) + 120;
            vsMonstersAmt = random.nextInt(7) + 5;
            vsPlayersAmt = random.nextInt(7) + 5;
            intamt = random.nextInt(130) + 120;
            elemamt = random.nextInt(30) + 25;
            pureamt = random.nextInt(30) + 25;
            lifeamt = random.nextInt(8) + 1;
            critamt = random.nextInt(11) + 1;
            accamt = random.nextInt(20) + 15;
            thornsamt = random.nextInt(5) + 1;
            PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
            for (int i = 0; i < pp.orbrolls; i++) {
                dodgeamt2 = random.nextInt(12) + 1;
                blockamt2 = random.nextInt(12) + 1;
                vitamt2 = random.nextInt(250) + 1;
                dexamt2 = random.nextInt(250) + 1;
                stramt2 = random.nextInt(250) + 1;
                vsMonstersAmt2 = random.nextInt(12) + 1;
                vsPlayersAmt2 = random.nextInt(12) + 1;
                intamt2 = random.nextInt(250) + 1;
                elemamt2 = random.nextInt(55) + 1;
                pureamt2 = random.nextInt(55) + 1;
                lifeamt2 = random.nextInt(8) + 1;
                critamt2 = random.nextInt(11) + 1;
                accamt2 = random.nextInt(35) + 1;
                thornsamt2 = random.nextInt(5) + 1;
                if (dodgeamt2 > dodgeamt) dodgeamt = dodgeamt2;
                if (blockamt2 > blockamt) blockamt = blockamt2;
                if (vsMonstersAmt2 > vsMonstersAmt) vsMonstersAmt = vsMonstersAmt2;
                if (vsPlayersAmt2 > vsPlayersAmt) vsPlayersAmt = vsPlayersAmt2;
                if (vitamt2 > vitamt) vitamt = vitamt2;
                if (dexamt2 > dexamt) dexamt = dexamt2;
                if (stramt2 > stramt) stramt = stramt2;
                if (intamt2 > intamt) intamt = intamt2;
                if (elemamt2 > elemamt) elemamt = elemamt2;
                if (pureamt2 > pureamt) pureamt = pureamt2;
                if (lifeamt2 > lifeamt) lifeamt = lifeamt2;
                if (critamt2 > critamt) critamt = critamt2;
                if (accamt2 > accamt) accamt = accamt2;
                if (thornsamt2 > thornsamt) thornsamt = thornsamt2;
            }
            if (item == 0) {
                name = "Legendary Staff";
                is.setType(Material.GOLD_HOE);
            }
            if (item == 1) {
                name = "Legendary Polearm";
                is.setType(Material.GOLD_SPADE);
            }
            if (item == 2) {
                name = "Legendary Sword";
                is.setType(Material.GOLD_SWORD);
            }
            if (item == 3) {
                name = "Legendary Axe";
                is.setType(Material.GOLD_AXE);
            }
            if (item == 4) {
                name = "Legendary Full Helmet";
                is.setType(Material.GOLD_HELMET);
            }
            if (item == 5) {
                name = "Legendary Platemail";
                is.setType(Material.GOLD_CHESTPLATE);
            }
            if (item == 6) {
                name = "Legendary Platemail Leggings";
                is.setType(Material.GOLD_LEGGINGS);
            }
            if (item == 7) {
                name = "Legendary Platemail Boots";
                is.setType(Material.GOLD_BOOTS);
            }
        }
        if (tier == 6) {
            tier = 6;
            dodgeamt = random.nextInt(13) + 1;
            blockamt = random.nextInt(13) + 1;
            vitamt = random.nextInt(150) + 200;
            dexamt = random.nextInt(150) + 200;
            stramt = random.nextInt(150) + 200;
            vsMonstersAmt = random.nextInt(12) + 1;
            vsPlayersAmt = random.nextInt(12) + 1;
            intamt = random.nextInt(150) + 200;
            elemamt = random.nextInt(30) + 40;
            pureamt = random.nextInt(30) + 50;
            lifeamt = random.nextInt(6) + 6;
            critamt = random.nextInt(6) + 5;
            accamt = random.nextInt(20) + 20;
            thornsamt = random.nextInt(5) + 1;
            PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
            for (int i = 0; i < pp.orbrolls; i++) {
                dodgeamt2 = random.nextInt(13) + 1;
                blockamt2 = random.nextInt(13) + 1;
                vitamt2 = random.nextInt(150) + 200;
                dexamt2 = random.nextInt(150) + 200;
                stramt2 = random.nextInt(150) + 200;
                vsMonstersAmt2 = random.nextInt(12) + 1;
                vsPlayersAmt2 = random.nextInt(12) + 1;
                intamt2 = random.nextInt(150) + 200;
                elemamt2 = random.nextInt(70) + 1;
                pureamt2 = random.nextInt(70) + 1;
                lifeamt2 = random.nextInt(8) + 1;
                critamt2 = random.nextInt(11) + 1;
                accamt2 = random.nextInt(40) + 1;
                thornsamt2 = random.nextInt(5) + 1;
                if (dodgeamt2 > dodgeamt) dodgeamt = dodgeamt2;
                if (blockamt2 > blockamt) blockamt = blockamt2;
                if (vsMonstersAmt2 > vsMonstersAmt) vsMonstersAmt = vsMonstersAmt2;
                if (vsPlayersAmt2 > vsPlayersAmt) vsPlayersAmt = vsPlayersAmt2;
                if (vitamt2 > vitamt) vitamt = vitamt2;
                if (dexamt2 > dexamt) dexamt = dexamt2;
                if (stramt2 > stramt) stramt = stramt2;
                if (intamt2 > intamt) intamt = intamt2;
                if (elemamt2 > elemamt) elemamt = elemamt2;
                if (pureamt2 > pureamt) pureamt = pureamt2;
                if (lifeamt2 > lifeamt) lifeamt = lifeamt2;
                if (critamt2 > critamt) critamt = critamt2;
                if (accamt2 > accamt) accamt = accamt2;
                if (thornsamt2 > thornsamt) thornsamt = thornsamt2;
            }
            if (item == 0) {
                name = "Frozen Staff";
                is.setType(Material.DIAMOND_HOE);
            }
            if (item == 1) {
                name = "Frozen Polearm";
                is.setType(Material.DIAMOND_SPADE);
            }
            if (item == 2) {
                name = "Frozen Sword";
                is.setType(Material.DIAMOND_SWORD);
            }
            if (item == 3) {
                name = "Frozen Axe";
                is.setType(Material.DIAMOND_AXE);
            }
            if (item == 4) {
                name = "Frozen Full Helmet";
                is.setType(Material.LEATHER_HELMET);
            }
            if (item == 5) {
                name = "Frozen Platemail";
                is.setType(Material.LEATHER_CHESTPLATE);
            }
            if (item == 6) {
                name = "Frozen Platemail Leggings";
                is.setType(Material.LEATHER_LEGGINGS);
            }
            if (item == 7) {
                name = "Frozen Platemail Boots";
                is.setType(Material.LEATHER_BOOTS);
            }
            if (item > 3) Items.setItemBlueLeather(is);
        }
        if (Enchants.getPlus(is) < 4) {
            final double beforehp = Damage.getHp(is);
            final double beforehpgen = Damage.getHps(is);
            final int beforenrg = Damage.getEnergy(is);
            final double beforemin = Damage.getDamageRange(is).get(0);
            final double beforemax = Damage.getDamageRange(is).get(1);
            switch (getItemType(is)) {
                case 0:
                case 1:
                case 2:
                case 3:
                    double addedmin2 = 0;
                    double addedmax2 = 0;
                    switch (Enchants.getPlus(is)) {
                        case 0:
                            addedmin2 = beforemin * 20 / 100;
                            addedmax2 = beforemax * 20 / 100;
                            break;
                        case 1:
                            addedmin2 = beforemin * 15 / 100;
                            addedmax2 = beforemax * 15 / 100;
                            break;
                        case 2:
                            addedmin2 = beforemin * 10 / 100;
                            addedmax2 = beforemax * 10 / 100;
                            break;
                        case 3:
                            addedmin2 = beforemin * 5 / 100;
                            addedmax2 = beforemax * 5 / 100;
                            break;
                    }
                    if (addedmin2 < 1.0) {
                        addedmin2 = 1.0;
                    }
                    final int min2 = (int) (beforemin + addedmin2);
                    if (addedmax2 < 1.0) {
                        addedmax2 = 1.0;
                    }
                    final int max2 = (int) (beforemax + addedmax2);
                    oldlore.set(0, ChatColor.RED + "DMG: " + min2 + " - " + max2);
                    is.addUnsafeEnchantment(Enchants.glow, 1);
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                    double added2 = 0;
                    int addedEnergy = beforenrg;
                    double addedhps2 = 0;
                    switch (Enchants.getPlus(is)) {
                        case 0:
                            added2 = beforehp * 20 / 100;
                            addedEnergy += 4;
                            addedhps2 = beforehpgen * 20 / 100;
                            break;
                        case 1:
                            added2 = beforehp * 15 / 100;
                            addedhps2 = beforehpgen * 15 / 100;
                            addedEnergy += 3;
                            break;
                        case 2:
                            added2 = beforehp * 10 / 100;
                            addedhps2 = beforehpgen * 10 / 100;
                            addedEnergy += 2;
                            break;
                        case 3:
                            added2 = beforehp * 5 / 100;
                            addedhps2 = beforehpgen * 5 / 100;
                            addedEnergy += 1;
                            break;
                    }
                    if (added2 < 1.0) {
                        added2 = 1.0;
                    }
                    final int newhp2 = (int) (beforehp + added2);
                    oldlore.set(1, ChatColor.RED + "HP: +" + newhp2);
                    if (oldlore.get(2).contains("ENERGY REGEN")) {
                        oldlore.set(2, ChatColor.RED + "ENERGY REGEN: +" + addedEnergy + "%");
                    } else if (oldlore.get(2).contains("HP REGEN")) {
                        if (addedhps2 < 1.0) {
                            addedhps2 = 1.0;
                        }
                        final int newhps2 = (int) (beforehpgen + addedhps2);
                        oldlore.set(2, ChatColor.RED + "HP REGEN: +" + newhps2 + "/s");
                    }
                    is.addUnsafeEnchantment(Enchants.glow, 1);
                    break;
            }
        }
        if (item == 0 || item == 1 || item == 2 || item == 3) {
            lore.add(oldlore.get(0));
            if (item == 3 && pure == 1) {
                lore.add(ChatColor.RED + "PURE DMG: +" + pureamt);
                name = "Pure " + name;
            }
            if (item == 2 && acc == 1) {
                lore.add(ChatColor.RED + "ACCURACY: " + accamt + "%");
                name = "Accurate " + name;
            }
            if (life == 1) {
                lore.add(ChatColor.RED + "LIFE STEAL: " + lifeamt + "%");
                name = "Vampyric " + name;
            }
            if (vsMonsters == 1) {
                lore.add(ChatColor.RED + "VS MONSTERS: " + vsMonstersAmt + "%");
            }
            if (vsPlayers == 1) {
                lore.add(ChatColor.RED + "VS PLAYERS: " + vsPlayersAmt + "%");
            }
            if (crit == 1) {
                lore.add(ChatColor.RED + "CRITICAL HIT: " + critamt + "%");
                name = "Deadly " + name;
            }
            if (elem == 3) {
                lore.add(ChatColor.RED + "ICE DMG: +" + elemamt);
                name = name + " of Ice";
            }
            if (elem == 2) {
                lore.add(ChatColor.RED + "POISON DMG: +" + elemamt);
                name = name + " of Poison";
            }
            if (elem == 1) {
                lore.add(ChatColor.RED + "FIRE DMG: +" + elemamt);
                name = name + " of Fire";
            }
        }
        if (item == 4 || item == 5 || item == 6 || item == 7) {
            lore.add(oldlore.get(0));
            lore.add(oldlore.get(1));
            lore.add(oldlore.get(2));
            if (intel == 1) {
                lore.add(ChatColor.RED + "INT: +" + intamt);
            }
            if (str == 1) {
                lore.add(ChatColor.RED + "STR: +" + stramt);
            }
            if (vit == 1) {
                lore.add(ChatColor.RED + "VIT: +" + vitamt);
            }
            if (dex == 1) {
                lore.add(ChatColor.RED + "DEX: +" + dexamt);
            }
            if (dodge == 1) {
                lore.add(ChatColor.RED + "DODGE: " + dodgeamt + "%");
                name = "Agile " + name;
            }
            if (thorns == 1) {
                lore.add(ChatColor.RED + "THORNS: " + thornsamt + "%");
                name = "Thorny " + name;
            }
            if (oldlore.get(2).contains("HP REGEN:")) {
                name = "Mending " + name;
            }
            if (block == 1) {
                lore.add(ChatColor.RED + "BLOCK: " + blockamt + "%");
                name = "Protective " + name;
            }
            if (oldlore.get(2).contains("ENERGY REGEN:")) {
                name = name + " of Fortitude";
            }
        }
        int plus = Enchants.getPlus(is);
        NBTAccessor nbt = new NBTAccessor(is).check();
        if (oldName != null && nbt.getInteger("namedElite") == 1) {
            name = oldName;
            for (String line : oldlore) {
                if (line.startsWith(ChatColor.GRAY.toString())) {
                    lore.add(line);
                }
            }
        }


        lore.addAll(rare);
        if (tier == 1) {
            name = ChatColor.WHITE + name;
        }
        if (tier == 2) {
            name = ChatColor.GREEN + name;
        }
        if (tier == 3) {
            name = ChatColor.AQUA + name;
        }
        if (tier == 4) {
            name = ChatColor.LIGHT_PURPLE + name;
        }
        if (tier == 5) {
            name = ChatColor.YELLOW + name;
        }
        if (tier == 6) {
            name = ChatColor.BLUE + name;
        }
        int newPlus = 0;
        if (plus <= 4) newPlus = 4;
        if (plus > 4) newPlus = plus;
        if (ChatColor.stripColor(name).contains("[+")) {
            name = name.replace("[+" + plus + "]", "[+" + newPlus + "]");
        } else if (!ChatColor.stripColor(name).contains("[+") && newPlus >= 4) {
            name = ChatColor.RED + "[+" + newPlus + "] " + name;
        } else {
            name = ChatColor.RED + "[+4] " + name;
        }

        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    public void onDisable() {

    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInvClick(final InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        if (!e.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            return;
        }
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }
        if(ItemVendors.isRecentlyInteracted(p)) {
            e.setCancelled(true);
            return;
        }
        if (e.getCursor() != null && e.getCursor().getType() == Material.MAGMA_CREAM && e.getCursor().getItemMeta().hasDisplayName() && e.getCursor().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Legendary Orb of Alteration") && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && !Duels.duelers.containsKey(p)) {
            final ItemStack is = e.getCurrentItem();
            if (is.getItemMeta().hasLore() && getItemTier(is) > 0 && getItemTier(is) < 7 && getItemType(is) > -1 && getItemType(is) <= 7) {
                e.setCancelled(true);
                if (e.getCursor().getAmount() > 1) {
                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                } else if (e.getCursor().getAmount() == 1) {
                    e.setCursor(null);
                }
                final int oldsize = is.getItemMeta().getLore().size();
                final ItemStack newis = randomizeLegendaryStats(is, p);
                final int newsize = newis.getItemMeta().getLore().size();
                if (newsize > oldsize) {
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
                    final Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    final FireworkMeta fwm = fw.getFireworkMeta();
                    final FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
                    fwm.addEffect(effect);
                    fwm.setPower(0);
                    fw.setFireworkMeta(fwm);
                } else {
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0f, 1.25f);
                    Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, p.getEyeLocation(), 20.0);
                }
                newis.setDurability((short) 0);
                e.setCurrentItem(newis);
                ItemVendors.addToRecentlyInteracted(p);
            }
        }

        if (e.getCursor() != null && e.getCursor().getType() == Material.MAGMA_CREAM && e.getCursor().getItemMeta().hasDisplayName() && e.getCursor().getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Orb of Alteration") && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && !Duels.duelers.containsKey(p)) {
            final ItemStack is = e.getCurrentItem();
            if (is.getItemMeta().hasLore() && getItemTier(is) > 0 && getItemTier(is) < 7 && getItemType(is) > -1 && getItemType(is) <= 7) {
                e.setCancelled(true);
                if (e.getCursor().getAmount() > 1) {
                    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                } else if (e.getCursor().getAmount() == 1) {
                    e.setCursor(null);
                }
                final int oldsize = is.getItemMeta().getLore().size();
                final ItemStack newis = randomizeStats(is);
                final int newsize = newis.getItemMeta().getLore().size();
                if (newsize > oldsize) {
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
                    final Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    final FireworkMeta fwm = fw.getFireworkMeta();
                    final FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
                    fwm.addEffect(effect);
                    fwm.setPower(0);
                    fw.setFireworkMeta(fwm);
                } else {
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0f, 1.25f);
                    Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, p.getEyeLocation(), 20.0);
                }
                newis.setDurability((short) 0);
                e.setCurrentItem(newis);
                ItemVendors.addToRecentlyInteracted(p);
            }
        }
    }
}
