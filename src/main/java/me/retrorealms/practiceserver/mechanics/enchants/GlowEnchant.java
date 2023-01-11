/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.enchantments.EnchantmentTarget
 *  org.bukkit.enchantments.EnchantmentWrapper
 *  org.bukkit.inventory.ItemStack
 */
package me.retrorealms.practiceserver.mechanics.enchants;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class GlowEnchant
        extends EnchantmentWrapper {
    public GlowEnchant(int id) {
        super(id);
    }

    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    public EnchantmentTarget getItemTarget() {
        return null;
    }

    public int getMaxLevel() {
        return 10;
    }

    public String getName() {
        return "Glow";
    }

    public int getStartLevel() {
        return 1;
    }
}

