/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.BookMeta
 *  org.bukkit.inventory.meta.ItemMeta
 */
package me.retrorealms.practiceserver.mechanics.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Journal {
    public static ItemStack journal() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) book.getItemMeta();
        bm.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD + "Character Journal");
        bm.setLore(Arrays.asList(ChatColor.GRAY + "A book that displays", ChatColor.GRAY + "your character's stats"));
        book.setItemMeta((ItemMeta) bm);
        return book;
    }
}

