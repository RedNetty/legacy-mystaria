package me.retrorealms.practiceserver.mechanics.loot;

import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.Random;

public class LootDrops {
    public static ItemStack createLootDrop(int tier) {
        Random random = new Random();
        int dodrop = random.nextInt(150);

        if (dodrop < 50) {
            int whatenchant = random.nextInt(2);
            if (tier < 3 || tier >= 3 && whatenchant == 0) {
                return Items.enchant(tier, random.nextInt(2), false);
            }
            return Items.orb(false);
        }
        if (dodrop < 80) {
            int gemamt = 0;
            if (tier == 1) {
                gemamt = random.nextInt(9) + 8;
            }
            else if (tier == 2) {
                gemamt = random.nextInt(17) + 16;
            }
            else if (tier == 3) {
                gemamt = random.nextInt(33) + 32;
            }
            else if (tier == 4) {
                gemamt = random.nextInt(257) + 256;
            }
            else if (tier == 5) {
                gemamt = random.nextInt(513) + 512;
            }
            else if (tier == 6) {
                gemamt = random.nextInt(1029) + 1028;
            }
            if (gemamt > 64) {
                return Money.createBankNote(gemamt);
            }
            return Money.makeGems(gemamt);
        }
        if (dodrop < 130) {
            return potion(tier);
        }
        if (dodrop < 150) {
            int scrolltype;
            if (tier == 1) {
                scrolltype = random.nextInt(1);
                if (scrolltype == 0) {
                    return TeleportBooks.deadpeaks_book(false);
                }
                if (scrolltype == 1) {
                    return TeleportBooks.tripoli_book(false);
                }
            }
            if (tier == 2) {
                scrolltype = random.nextInt(1);
                if (scrolltype == 0) {
                    return TeleportBooks.deadpeaks_book(false);
                }
                if (scrolltype == 1) {
                    return TeleportBooks.tripoli_book(false);
                }
            }
            if (tier == 3) {
                scrolltype = random.nextInt(1);
                if (scrolltype == 0) {
                    return TeleportBooks.deadpeaks_book(false);
                }
                if (scrolltype == 1) {
                    return TeleportBooks.tripoli_book(false);
                }
            }
            if (tier == 4) {
                scrolltype = random.nextInt(1);
                if (scrolltype == 0) {
                    return TeleportBooks.deadpeaks_book(false);
                }
                if (scrolltype == 1) {
                    return TeleportBooks.avalonBook(false);
                }
            }
            if (tier == 5) {
                scrolltype = random.nextInt(1);
                if (scrolltype == 0) {
                    return TeleportBooks.avalonBook(false);
                }
                if (scrolltype == 1) {
                    return TeleportBooks.tripoli_book(false);
                }
            }
            if (tier == 6) {
                scrolltype = random.nextInt(1);
                if (scrolltype == 0) {
                    return TeleportBooks.avalonBook(false);
                }
                if (scrolltype == 1) {
                    return TeleportBooks.tripoli_book(false);
                }
            }
        }
        if (dodrop < 250) {
            int whatfoodtodrop = random.nextInt(5);
            if (whatfoodtodrop == 0) {
                return new ItemStack(Material.COOKED_BEEF);
            }
            if (whatfoodtodrop == 1) {
                return new ItemStack(Material.BREAD);
            }
            if (whatfoodtodrop == 2) {
                return new ItemStack(Material.PUMPKIN_PIE);
            }
            if (whatfoodtodrop == 3) {
                return new ItemStack(Material.APPLE);
            }
            if (whatfoodtodrop == 4) {
                return new ItemStack(Material.MELON);
            }
        }
        return new ItemStack(Material.AIR);
    }

    @SuppressWarnings("deprecation")
    private static ItemStack potion(int tier) {
        switch (tier) {
            case 1:
                Potion potion = new Potion(PotionType.REGEN);
                ItemStack toReturn = potion.toItemStack(1);
                PotionMeta potionMeta = (PotionMeta) toReturn.getItemMeta();
                potionMeta.setDisplayName(ChatColor.WHITE + "Minor Health Potion");
                potionMeta.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.GREEN + "15HP"));
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    potionMeta.addItemFlags(itemFlag);
                }
                toReturn.setItemMeta(potionMeta);
                return toReturn;
            case 2:
                Potion potion1 = new Potion(PotionType.INSTANT_HEAL);
                ItemStack toReturn1 = potion1.toItemStack(1);
                PotionMeta potionMeta1 = (PotionMeta) toReturn1.getItemMeta();
                potionMeta1.setDisplayName(ChatColor.GREEN + "Health Potion");
                potionMeta1.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.AQUA + "75HP"));
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    potionMeta1.addItemFlags(itemFlag);
                }
                toReturn1.setItemMeta(potionMeta1);
                return toReturn1;
            case 3:
                Potion potion2 = new Potion(PotionType.STRENGTH);
                ItemStack toReturn2 = potion2.toItemStack(1);
                PotionMeta potionMeta2 = (PotionMeta) toReturn2.getItemMeta();
                potionMeta2.setDisplayName(ChatColor.AQUA + "Major Health Potion");
                potionMeta2.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.AQUA + "300HP"));
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    potionMeta2.addItemFlags(itemFlag);
                }
                toReturn2.setItemMeta(potionMeta2);
                return toReturn2;
            case 4:
                Potion potion3 = new Potion(PotionType.INSTANT_DAMAGE);
                ItemStack toReturn3 = potion3.toItemStack(1);
                PotionMeta potionMeta3 = (PotionMeta) toReturn3.getItemMeta();
                potionMeta3.setDisplayName(ChatColor.LIGHT_PURPLE.toString() + "Superior Health Potion");
                potionMeta3.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.YELLOW + "800HP"));
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    potionMeta3.addItemFlags(itemFlag);
                }
                toReturn3.setItemMeta(potionMeta3);
                return toReturn3;
            case 5:
                Potion potion4 = new Potion(PotionType.FIRE_RESISTANCE);
                ItemStack toReturn4 = potion4.toItemStack(1);
                PotionMeta potionMeta5 = (PotionMeta) toReturn4.getItemMeta();
                potionMeta5.setDisplayName(ChatColor.YELLOW + "Legendary Health Potion");
                potionMeta5.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.YELLOW + "1600HP"));
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    potionMeta5.addItemFlags(itemFlag);
                }
                toReturn4.setItemMeta(potionMeta5);
                return toReturn4;
            case 6:
                Potion potion5 = new Potion(PotionType.SPEED);
                ItemStack toReturn5 = potion5.toItemStack(1);
                PotionMeta potionMeta6 = (PotionMeta) toReturn5.getItemMeta();
                potionMeta6.setDisplayName(ChatColor.BLUE + "Chilled Health Potion");
                potionMeta6.setLore(Arrays.asList(ChatColor.GRAY + "A potion that restores " + ChatColor.BLUE + "3200HP"));
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    potionMeta6.addItemFlags(itemFlag);
                }
                toReturn5.setItemMeta(potionMeta6);
                return toReturn5;
        }
        return null;
    }
}
