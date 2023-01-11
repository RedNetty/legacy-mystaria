package me.retrorealms.practiceserver.mechanics.inventory;

import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * Created by Dr. Nick Doran on 8/16/2016.
 */
public class Icon {

    /**
     * @param playerName The name of the player.
     * @param name       The name of the future ItemStack.
     * @param lore       The lore of the future ItemStack.
     * @return The new and improved ItemStack.
     */
    public static ItemStack make(String playerName, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(name);
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    /**
     * @param itemStack The base ItemStack you're changing.
     * @param name      The name of the future ItemStack.
     * @param lore      The lore of the future ItemStack.
     * @return The new and improved ItemStack.
     */
    public static ItemStack make(ItemStack itemStack, String name, List<String> lore) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("AttributeModifiers", new NBTTagList());
        nmsStack.setTag(tag);
        itemStack = CraftItemStack.asBukkitCopy(nmsStack);

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
