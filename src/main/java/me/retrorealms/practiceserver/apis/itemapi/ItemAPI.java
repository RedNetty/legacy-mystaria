package me.retrorealms.practiceserver.apis.itemapi;

import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGenerator;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 6-5-2017.
 */
public class ItemAPI {

    private static ScrollGenerator scrollGenerator;

    public static void init() {
        if (scrollGenerator == null) scrollGenerator = new ScrollGenerator();
    }

    public static ScrollGenerator getScrollGenerator() {
        return scrollGenerator;
    }

    public static boolean isProtected(ItemStack itemStack) {
        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);

        return nbtAccessor.hasTag()
                && nbtAccessor.hasKey("itemProtection")
                && !nbtAccessor.getString("itemProtection").equals("false")
                && nbtAccessor.getString("itemProtection").equals("true");
    }

    public static ItemStack removeProtection(ItemStack itemStack) {
        if (!isProtected(itemStack)) return itemStack;

        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);

        nbtAccessor.remove("itemProtection");

        ItemStack itemStack1 = nbtAccessor.update();

        ItemMeta itemMeta = itemStack1.getItemMeta();

        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();

            lore.remove("");

            lore.remove(ChatColor.GREEN.toString() + ChatColor.BOLD + "PROTECTED");
            lore.remove("PROTECTED");

            itemMeta.setLore(lore);

        } else itemMeta.setLore(Arrays.asList(""));

        itemStack1.setItemMeta(itemMeta);

        return itemStack1;
    }

    public static ItemStack makeProtected(ItemStack itemStack) {
        if (isProtected(itemStack)) return itemStack;

        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);
        nbtAccessor.setString("itemProtection", "true");

        ItemStack itemStack1 = nbtAccessor.update();

        ItemMeta itemMeta = itemStack1.getItemMeta();

        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            lore.addAll(Arrays.asList("", ChatColor.GREEN.toString() + ChatColor.BOLD + "PROTECTED"));

            itemMeta.setLore(lore);

        } else itemMeta.setLore(Arrays.asList("", ChatColor.GREEN.toString() + ChatColor.BOLD + "PROTECTED"));

        itemStack1.setItemMeta(itemMeta);

        return itemStack1;
    }

    public static boolean isProtectionScroll(ItemStack itemStack) {
        NBTAccessor nbtAccessor = new NBTAccessor(itemStack);

        return nbtAccessor.hasTag() && nbtAccessor.hasKey("itemProtectionScroll");

    }

    public static int getScrollTier(ItemStack protScroll) {
        if (!isProtectionScroll(protScroll)) return -1;

        NBTAccessor nbtAccessor = new NBTAccessor(protScroll);

        return nbtAccessor.getInteger("itemProtectionScrollTier");
    }

    public static boolean canEnchant(ItemStack itemStack, ItemStack protScroll) {
        if (!isProtectionScroll(protScroll)) return false;
        if (isProtected(itemStack)) return false;

        NBTAccessor nbtAccessor = new NBTAccessor(protScroll);

        int tier = nbtAccessor.getInteger("itemProtectionScrollTier");

        return itemStack.getType().name().contains("WOOD") && tier == 0
                || itemStack.getType().name().contains("STONE") && tier == 1
                || itemStack.getType().name().contains("IRON") && tier == 2
                || (itemStack.getType().name().contains("DIAMOND_") && !itemStack.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString())) && tier == 3
                || itemStack.getType().name().contains("GOLD") && tier == 4
                || (itemStack.getType().name().contains("LEATHER_") && !Items.isBlueLeather(itemStack)) && tier == 0
                || itemStack.getType().name().contains("CHAINMAIL_") && tier == 1
                || (itemStack.getType().name().contains("LEATHER_") && Items.isBlueLeather(itemStack))&& tier == 5
                || (itemStack.getType().name().contains("DIAMOND_") && itemStack.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString())) && tier == 5;


    }
}
