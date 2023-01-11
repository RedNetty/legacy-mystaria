package me.retrorealms.practiceserver.mechanics.item.scroll;

import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.useless.api.Generator;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Giovanni on 6-5-2017.
 */
public class ScrollGenerator implements Generator<ItemStack> {
    @Override
    public ItemStack next() {
        return null;
    }

    public ItemStack nextEnchantPickaxe(String type, int amount) {
        ItemStack itemStack = new ItemStack(Material.EMPTY_MAP);
        ItemMeta itemMeta = itemStack.getItemMeta();

        String name = ChatColor.GREEN.toString() + ChatColor.BOLD + type;

        itemMeta.setDisplayName(name);

        itemMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Effect: +" + amount + "%"));

        itemStack.setItemMeta(itemMeta);
        itemStack.addEnchantment(Enchants.glow, 1);

        NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();
        nbtAccessor.setString("pickaxeEnchant", type);
        nbtAccessor.setInt("pickaxeEnchantEffect", amount);

        return nbtAccessor.update();
    }

    public ItemStack next(int tier) {
        ItemStack itemStack = new ItemStack(Material.EMPTY_MAP);
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = ChatColor.WHITE.toString() + ChatColor.BOLD + "WHITE SCROLL: ";

        switch (tier) {
            case 0:
                displayName = displayName + ChatColor.WHITE + "Protect Leather Equipment";
                break;
            case 1:
                displayName = displayName + ChatColor.GREEN + "Protect Chainmail Equipment";
                break;
            case 2:
                displayName = displayName + ChatColor.AQUA + "Protect Iron Equipment";
                break;
            case 3:
                displayName = displayName + ChatColor.LIGHT_PURPLE + "Protect Diamond Equipment";
                break;
            case 4:
                displayName = displayName + ChatColor.YELLOW + "Protect Gold Equipment";
                break;
            case 5:
                displayName = displayName + ChatColor.BLUE + "Protect Frozen Equipment";
                break;
        }

        itemMeta.setDisplayName(displayName);
        int realTier = tier + 1;
        String realTierString = Integer.toString(realTier);
        itemMeta.setLore(Arrays.asList("",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any T" + realTierString + " item to ",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " it from being destroyed",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "if the next enchantment scroll (up to +12) fails"));

        itemStack.setItemMeta(itemMeta);

        NBTAccessor nbtAccessor = new NBTAccessor(itemStack).check();
        nbtAccessor.setString("itemProtectionScroll", "true");
        nbtAccessor.setInt("itemProtectionScrollTier", tier);

        return nbtAccessor.update();
    }

    public int getPrice(int tier) {
        switch (tier) {
            case 0:
                return 250;
            case 1:
                return 500;
            case 2:
                return 1000;
            case 3:
                return 2500;
            case 4:
                return 10000;
            case 5:
                return -1;
        }

        return 0;
    }
}
