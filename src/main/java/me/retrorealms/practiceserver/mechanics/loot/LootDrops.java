package me.retrorealms.practiceserver.mechanics.loot;

import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import me.retrorealms.practiceserver.mechanics.donations.Nametags.Nametag;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGUI;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGenerator;
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
import java.util.concurrent.ThreadLocalRandom;

public class LootDrops {
    public static ItemStack createLootDrop(int tier) {
        Random random = new Random();
        int dropChance = random.nextInt(301); // Random number to determine the type of drop.

        if (dropChance < 50) {
            return createEnchantmentOrOrb(tier, random);
        } else if (dropChance <= 100) {
            return createGems(tier, random);
        } else if (dropChance <= 160) {
            return createPotion(tier);
        } else if (dropChance <= 230) {
            return createTeleportBook(tier, random);
        } else if (dropChance < 250) {
            return createTagOrTracker();
        } else if (dropChance <= 260) {
            return createScroll(tier);
        } else if (tier == 5 && dropChance <= 270) {
            return createLegendaryOrb();
        } else if (dropChance <= 300) {
            return createCrate(tier);
        } else {
            return createPotion(tier);
        }
    }

    private static ItemStack createEnchantmentOrOrb(int tier, Random random) {
        int enchantmentType = random.nextInt(2);
        if (tier < 3 || (tier >= 3 && enchantmentType == 0)) {
            return Items.enchant(tier, random.nextInt(2), false);
        }
        return Items.orb(false);
    }

    private static ItemStack createGems(int tier, Random random) {
        int gemAmount = calculateGemAmount(tier, random);
        if (gemAmount > 64) {
            return Money.createBankNote(gemAmount);
        }
        return Money.makeGems(gemAmount);
    }

    private static int calculateGemAmount(int tier, Random random) {
        switch (tier) {
            default:
            case 1:
                return random.nextInt(20) + 100;
            case 2:
                return random.nextInt(17) + 206;
            case 3:
                return random.nextInt(33) + 302;
            case 4:
                return random.nextInt(257) + 756;
            case 5:
                return random.nextInt(513) + 1212;
            case 6:
                return random.nextInt(1029) + 1028;
        }
    }

    private static ItemStack createPotion(int tier) {
        return potion(tier);
    }

    private static ItemStack createTeleportBook(int tier, Random random) {
        int scrollType = random.nextInt(2);
        switch (tier) {
            case 1:
            case 2:
            case 3:
                return scrollType == 0 ? TeleportBooks.deadpeaksBook(false) : TeleportBooks.tripoliBook(false);
            case 4:
            case 5:
            case 6:
                return scrollType == 0 ? TeleportBooks.avalonBook(false) : TeleportBooks.tripoliBook(false);
            default:
                return TeleportBooks.tripoliBook(false);
        }
    }

    private static ItemStack createTagOrTracker() {
        boolean whatDrop = ThreadLocalRandom.current().nextBoolean();
        return whatDrop ? Nametag.item_ownership_tag.clone() : WepTrak.weapon_tracker_item.clone();
    }

    private static ItemStack createScroll(int tier) {
        return new ItemStack(new ScrollGenerator().next(tier - 1));
    }

    private static ItemStack createLegendaryOrb() {
        return new ItemStack(Items.legendaryOrb(false));
    }

    private static ItemStack createCrate(int tier) {
        return new ItemStack(CratesMain.createCrate(tier, false));
    }


    @SuppressWarnings("deprecation")
    private static ItemStack potion(int tier) {
        switch (tier) {
            case 1:
                return createPotion(PotionType.REGEN, ChatColor.WHITE + "Minor Health Potion", ChatColor.GRAY + "A potion that restores " + ChatColor.GREEN + "15HP");
            case 2:
                return createPotion(PotionType.INSTANT_HEAL, ChatColor.GREEN + "Health Potion", ChatColor.GRAY + "A potion that restores " + ChatColor.AQUA + "75HP");
            case 3:
                return createPotion(PotionType.STRENGTH, ChatColor.AQUA + "Major Health Potion", ChatColor.GRAY + "A potion that restores " + ChatColor.AQUA + "300HP");
            case 4:
                return createPotion(PotionType.INSTANT_DAMAGE, ChatColor.LIGHT_PURPLE.toString() + "Superior Health Potion", ChatColor.GRAY + "A potion that restores " + ChatColor.YELLOW + "800HP");
            case 5:
                return createPotion(PotionType.FIRE_RESISTANCE, ChatColor.YELLOW + "Legendary Health Potion", ChatColor.GRAY + "A potion that restores " + ChatColor.YELLOW + "1600HP");
            case 6:
                return createPotion(PotionType.SPEED, ChatColor.BLUE + "Chilled Health Potion", ChatColor.GRAY + "A potion that restores " + ChatColor.BLUE + "3200HP");
            default:
                return null;
        }
    }

    private static ItemStack createPotion(PotionType type, String displayName, String lore) {
        Potion potion = new Potion(type);
        ItemStack toReturn = potion.toItemStack(1);
        PotionMeta potionMeta = (PotionMeta) toReturn.getItemMeta();
        potionMeta.setDisplayName(displayName);
        potionMeta.setLore(Arrays.asList(lore));
        for (ItemFlag itemFlag : ItemFlag.values()) {
            potionMeta.addItemFlags(itemFlag);
        }
        toReturn.setItemMeta(potionMeta);
        return toReturn;
    }
}
