package me.retrorealms.practiceserver.mechanics.shard.gui;

import me.retrorealms.practiceserver.apis.nbt.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Giovanni on 10-6-2017.
 */
public class ServerGUI {

    public static Inventory serverGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Mystaria - SERVERS");

        /*TEST*/
        ItemStack testServer = new ItemStack(Material.COMMAND_CHAIN);
        ItemMeta testMeta = testServer.getItemMeta();
        testMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lDevelopment Server"));
        testMeta.setLore(Arrays.asList(
                "",
                ChatColor.GREEN + "• Access - PMOD+",
                ChatColor.WHITE + "Click here to load into",
                ChatColor.WHITE + "this server"));
        testMeta.addItemFlags(ItemFlag.values());
        testServer.setItemMeta(testMeta);
        NBTAccessor testAccessor = new NBTAccessor(testServer).check();
        testAccessor.setString("server", "dev");
        ItemStack finishedTest = testAccessor.update();
        finishedTest.addUnsafeEnchantment(Enchants.glow, 10);

        /*Us1*/
        ItemStack us1Server = new ItemStack(Material.END_CRYSTAL);
        ItemMeta kitMeta = us1Server.getItemMeta();
        kitMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lUS-1"));
        kitMeta.setLore(Arrays.asList(
                "",
                ChatColor.GREEN + "• Access - Everyone!",
                ChatColor.WHITE + "Click here to load into",
                ChatColor.WHITE + "this server"));
        kitMeta.addItemFlags(ItemFlag.values());
        us1Server.setItemMeta(kitMeta);
        NBTAccessor kitAccessor = new NBTAccessor(us1Server).check();
        kitAccessor.setString("server", "retro1");
        ItemStack finishedus1 = kitAccessor.update();
        finishedus1.addUnsafeEnchantment(Enchants.glow, 10);

        /*Us2*/
        ItemStack us2Server = new ItemStack(Material.END_CRYSTAL);
        ItemMeta P2WMeta = us2Server.getItemMeta();
        P2WMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lUS-2"));
        P2WMeta.setLore(Arrays.asList(
                "",
                ChatColor.GREEN + "• Access - Everyone!",
                ChatColor.WHITE + "Click here to load into",
                ChatColor.WHITE + "this server"));
        P2WMeta.addItemFlags(ItemFlag.values());
        us2Server.setItemMeta(P2WMeta);
        NBTAccessor P2WAccessor = new NBTAccessor(us2Server).check();
        P2WAccessor.setString("server", "retro2");
        ItemStack finishedus2 = P2WAccessor.update();
        finishedus2.addUnsafeEnchantment(Enchants.glow, 10);

        /*Event*/
        ItemStack eventServer = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta eventMeta = eventServer.getItemMeta();
        eventMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lEvent Server"));
        eventMeta.setLore(Arrays.asList(
                "",
                ChatColor.GREEN + "• Access - Everyone!",
                ChatColor.WHITE + "Click here to load into",
                ChatColor.WHITE + "this server"));
        eventMeta.addItemFlags(ItemFlag.values());
        eventServer.setItemMeta(eventMeta);
        NBTAccessor eventAccessor = new NBTAccessor(eventServer).check();
        eventAccessor.setString("server", "event");
        ItemStack finishedEvent = eventAccessor.update();
        finishedEvent.addUnsafeEnchantment(Enchants.glow, 10);

        /*Build*/
        ItemStack buildServer = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta buildMeta = buildServer.getItemMeta();
        buildMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lBuild Server"));
        buildMeta.setLore(Arrays.asList(
                "",
                ChatColor.GREEN + "• Access - Builders+",
                ChatColor.WHITE + "Click here to load into",
                ChatColor.WHITE + "this server"));
        buildMeta.addItemFlags(ItemFlag.values());
        buildServer.setItemMeta(buildMeta);
        NBTAccessor buildAccessor = new NBTAccessor(buildServer).check();
        buildAccessor.setString("server", "build");
        ItemStack finishedBuild = buildAccessor.update();
        finishedBuild.addUnsafeEnchantment(Enchants.glow, 10);

        inventory.setItem(2, finishedus1);
        inventory.setItem(4, finishedus2);
        inventory.setItem(6, finishedEvent);
        if(ModerationMechanics.isStaff(player)) {
            inventory.setItem(0, finishedTest);
            inventory.setItem(8, finishedBuild);
        }
        return inventory;

    }
}