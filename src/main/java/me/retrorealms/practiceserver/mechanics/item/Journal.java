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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Journal {
    public static ItemStack journal() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) book.getItemMeta();
        bm.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD + "Character Journal");
        bm.setAuthor("Mystaria");
        bm.setLore(Arrays.asList(ChatColor.GRAY + "A book that displays", ChatColor.GRAY + "your character's stats"));
        book.setItemMeta((ItemMeta) bm);
        return book;
    }

    public static ItemStack fullBook(Player p) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) book.getItemMeta();
        String s = ChatColor.DARK_GREEN.toString() + ChatColor.UNDERLINE + "Lawful";
        String desc = ChatColor.BLACK.toString() + ChatColor.ITALIC + "-30% Durability Arm/Wep on Death";
        if (Alignments.chaotic.containsKey(p.getName())) {
            s = ChatColor.DARK_RED.toString() + ChatColor.UNDERLINE + "Chaotic\n" + ChatColor.BLACK + ChatColor.BOLD
                    + "Neutral" + ChatColor.BLACK + " in " + Alignments.chaotic.get(p.getName()) + "s";
            desc = ChatColor.BLACK.toString() + ChatColor.ITALIC + "Inventory LOST on Death";
        }
        if (Alignments.neutral.containsKey(p.getName())) {
            s = ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "Neutral\n" + ChatColor.BLACK + ChatColor.BOLD
                    + "Lawful" + ChatColor.BLACK + " in " + Alignments.neutral.get(p.getName()) + "s";
            desc = ChatColor.BLACK.toString() + ChatColor.ITALIC + "25%/50% Arm/Wep LOST on Death";
        }
        int dps = 0;
        int arm = 0;
        int amt = 5;
        int nrg = 100;
        int block = 0;
        int dodge = 0;
        int intel = 0;
        int str = 0;
        int vit = 0;
        int dex = 0;
        int arm_pcnt = 0;
        int sword_dmg = 0;
        int pole_dmg = 0;
        int axe_dmg = 0;
        int block_pcnt = 0;
        int health_pcnt = 0;
        int hps_pcnt = 0;
        int nrg_pcnt = 0;
        int crit_pcnt = 0;
        int ele_resist = 0;
        int ele_dmg = 0;
        int staff_dmg = 0;
        int dps_pcnt = 0;
        int dodge_pcnt = 0;
        int armpen = 0;
        ItemStack[] arritemStack = p.getInventory().getArmorContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack is = arritemStack[n2];
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                int adddps = Damage.getDps(is);
                dps += adddps;
                int addarm = Damage.getArmor(is);
                arm += addarm;
                int added = Damage.getHps(is);
                amt += added;
                int addednrg = Damage.getEnergy(is);
                nrg += addednrg;
                int addeddodge = Damage.getPercent(is, "DODGE");
                dodge += addeddodge;
                int addedblock = Damage.getPercent(is, "BLOCK");
                block += addedblock;
                int addedint = Damage.getElem(is, "INT");
                intel += addedint;
                int addedstr = Damage.getElem(is, "STR");
                str += addedstr;
                int addedvit = Damage.getElem(is, "VIT");
                vit += addedvit;
                int addeddex = Damage.getElem(is, "DEX");
                dex += addeddex;
            }
            ++n2;
        }
        if (intel > 0) {
            nrg += Math.round(intel / 125);
            nrg_pcnt = (int) Math.round((double) intel * 0.009);
            crit_pcnt = (int) Math.round((double) intel * 0.015);
            staff_dmg = Math.round(intel / 50);
            ele_dmg = Math.round(intel / 30);
        }
        if (vit > 0) {
            sword_dmg = Math.round(vit / 50);
            health_pcnt = (int) Math.round((double) vit * 0.05);
            hps_pcnt = (int) Math.round((double) vit * 0.1);
            amt += hps_pcnt;

        }
        if (str > 0) {
            pole_dmg = Math.round(str / 50);
            axe_dmg = Math.round(str / 50);
            block_pcnt = (int) Math.round((double) str * 0.015);
            block = (int) ((long) block + Math.round((double) str * 0.015));
            arm_pcnt = (int) Math.round(str * 0.012);
            arm = (int) ((long) arm + Math.round((double) str * 0.012));
        }
        if (dex > 0) {
            dodge_pcnt = (int) Math.round((double) dex * 0.015);
            dodge = (int) ((long) dodge + Math.round((double) dex * 0.015));
            dps_pcnt = (int) Math.round(dex * 0.012);
            dps = (int) ((long) dps + Math.round((double) dex * 0.012));
            armpen = (int) (dex * 0.035);

        }
        bm.addPage(ChatColor.UNDERLINE.toString() + ChatColor.BOLD + "  Your Character  \n\n" + ChatColor.RESET
                + ChatColor.BOLD + "Alignment: " + s + "\n" + desc + "\n\n" + ChatColor.BLACK + "  "
                + (int) p.getHealth() + " / " + (int) p.getMaxHealth() + ChatColor.BOLD + " HP\n" + ChatColor.BLACK
                + "  " + arm + " - " + arm + "%" + ChatColor.BOLD + " Armor\n" + ChatColor.BLACK + "  " + dps
                + " - " + dps + "%" + ChatColor.BOLD + " DPS\n" + ChatColor.BLACK + "  " + amt + ChatColor.BOLD
                + " HP/s\n" + ChatColor.BLACK + "  " + nrg + "% " + ChatColor.BOLD + "Energy\n" + ChatColor.BLACK
                + "  " + dodge + "% " + ChatColor.BOLD + "Dodge\n" + ChatColor.BLACK + "  " + block + "% "
                + ChatColor.BOLD + "Block");
        bm.addPage(ChatColor.BLACK.toString() + ChatColor.BOLD + "+ " + str + " Strength\n" + "  " + ChatColor.BLACK
                + ChatColor.UNDERLINE + "'The Warrior'\n" + ChatColor.BLACK + "+" + arm_pcnt + "% Armor\n"
                + ChatColor.BLACK + "+" + block_pcnt + "% Block\n" + ChatColor.BLACK + "+" + axe_dmg + "% Axe DMG\n"
                + ChatColor.BLACK + "+" + pole_dmg + "% Polearm DMG\n\n" + ChatColor.BLACK + ChatColor.BOLD + "+ "
                + vit + " Vitality\n\n" + "  " + ChatColor.BLACK + ChatColor.UNDERLINE + "'The Defender'\n"
                + ChatColor.BLACK + "+" + health_pcnt + "% Health\n" + ChatColor.BLACK + "+" + hps_pcnt
                + "   HP/s\n" + ChatColor.BLACK + "+" + sword_dmg + "% Sword DMG");
        bm.addPage(ChatColor.BLACK + "" + ChatColor.BOLD + "+ " + intel + " Intellect\n" + "  " + ChatColor.BLACK
                + ChatColor.UNDERLINE + "'The Mage'\n" + ChatColor.BLACK + "+" + staff_dmg + "% Staff DMG\n"
                + ChatColor.BLACK + "+" + nrg_pcnt + "% Energy\n" + ChatColor.BLACK + "+" + ele_dmg
                + "% Ele Damage\n" + ChatColor.BLACK + "+" + crit_pcnt + "% Critical Hit\n\n" + ChatColor.BLACK + ""
                + ChatColor.BOLD + "+ " + dex + " Dexterity\n" + "  " + ChatColor.BLACK + ChatColor.UNDERLINE
                + "'The Archer'\n\n" + ChatColor.BLACK + "+" + dodge_pcnt + "% Dodge\n" + ChatColor.BLACK + "+"
                + dps_pcnt + "% DPS\n" + ChatColor.BLACK + "+" + armpen + "% Armor Pen.\n ");
        bm.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD + "Character Journal");
        bm.setLore(
                Arrays.asList(ChatColor.GRAY + "A book that displays", ChatColor.GRAY + "your character's stats"));
        book.setItemMeta(bm);
        return book;
    }


    public static void openJournal(Player p) {
        int slot = p.getInventory().getHeldItemSlot();
        ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, fullBook(p));
        try {
            PacketContainer pc = ProtocolLibrary.getProtocolManager().
                    createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            pc.getModifier().writeDefaults();
            ByteBuf bf = Unpooled.buffer(256); // note 1
            bf.setByte(0, (byte)0); // note 2
            bf.writerIndex(1);
            pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
            pc.getStrings().write(0, "MC|BOpen");
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        p.getInventory().setItem(slot, old);
    }
}

