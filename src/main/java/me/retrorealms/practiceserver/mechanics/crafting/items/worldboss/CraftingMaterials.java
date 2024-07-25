package me.retrorealms.practiceserver.mechanics.crafting.items.worldboss;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CraftingMaterials {


    public static ItemStack createEssenceOfFrost() {
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Essence of Frost");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A crystallized essence of pure cold.",
                ChatColor.DARK_PURPLE + "Rare Crafting Material"
        ));
        meta.addEnchant(Enchantment.FROST_WALKER, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createStarlightEssence() {
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Starlight Essence");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Condensed light from distant stars.",
                ChatColor.DARK_PURPLE + "Rare Crafting Material"
        ));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createAngelicFeather() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Angelic Feather");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A feather imbued with celestial energy.",
                ChatColor.DARK_PURPLE + "Rare Crafting Material"
        ));
        meta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createDivineClay() {
        ItemStack item = new ItemStack(Material.CLAY_BALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Divine Clay");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Clay infused with divine essence.",
                ChatColor.DARK_PURPLE + "Rare Crafting Material"
        ));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createInfernalCore() {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Infernal Core");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "The blazing heart of a powerful fire elemental.",
                ChatColor.DARK_PURPLE + "Rare Crafting Material"
        ));
        meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createWindsongFeather() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Windsong Feather");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A feather that hums with the power of the sky.",
                ChatColor.DARK_PURPLE + "Rare Crafting Material"
        ));
        meta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createElementalHarmony() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Elemental Harmony");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A perfect balance of frost, fire, and wind.",
                ChatColor.GOLD + "Legendary Modifier",
                ChatColor.GREEN + "Grants the wielder increased power over the elements."
        ));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}