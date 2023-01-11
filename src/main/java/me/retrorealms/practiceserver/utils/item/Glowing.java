package me.retrorealms.practiceserver.utils.item;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;

import java.lang.reflect.Field;

/**
 * Created by Giovanni on 4-7-2017.
 */
public class Glowing {

    public static Enchantment enchant;

    public static void registerEnchant() {
        Enchantment glow = new EnchantGlow(120);
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            EnchantmentWrapper.registerEnchantment(glow);
        } catch (IllegalArgumentException ignored) {

        }
        enchant = glow;
    }
}
