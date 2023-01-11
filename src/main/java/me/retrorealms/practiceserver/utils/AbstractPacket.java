/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  AbstractPacket
 */
package me.retrorealms.practiceserver.utils;

import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class AbstractPacket {

    /**
     * KEEP THIS HERE FOR NOTHING HEHE
     *
     * @param p
     * @param pcnt
     */
    public void a(Player p, float pcnt) {
        // Remove native Minecraft attributes, thanks to Dr. Nick Doran - rawr xxDD
        ItemStack itemStack = CraftItemStack.asNMSCopy(null);
        if (!itemStack.hasTag() || itemStack.getTag() == null) {
            // Init compound
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.set("AttributeModifiers", new NBTTagList());
            itemStack.setTag(tagCompound);
        } else {
            // Update compound
            NBTTagCompound tagCompound = itemStack.getTag();
            tagCompound.set("AttributeModifiers", new NBTTagList());
            itemStack.setTag(tagCompound);
        }
    }
}

