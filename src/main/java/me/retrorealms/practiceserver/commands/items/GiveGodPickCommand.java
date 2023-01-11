package me.retrorealms.practiceserver.commands.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Red on 11-8-2017.
 */

public class GiveGodPickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args){
        if(!sender.isOp()) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if(p != null && p.isOnline()){
            ItemStack P = new ItemStack(Material.DIAMOND_PICKAXE);
            ItemMeta pickmeta = P.getItemMeta();
            pickmeta.setDisplayName(ChatColor.BLUE + "Donator Pickaxe");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Level: " + ChatColor.BLUE + "120");
            lore.add(ChatColor.GRAY + "0 / 0");
            lore.add(ChatColor.GRAY + "EXP: " + ChatColor.BLUE + "||||||||||||||||||||||||||||||||||||||||||||||||||");
            lore.add(ChatColor.RED + "DOUBLE ORE: 10%");
            lore.add(ChatColor.RED + "GEM FIND: 10%");
            lore.add(ChatColor.RED + "TRIPLE ORE: 5%");
            lore.add(ChatColor.RED + "TREASURE FIND: 3%");
            lore.add(ChatColor.RED + "MINING SUCCESS: 20%");
            lore.add(ChatColor.RED + "DURABILITY: 15%");
            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of ice.");
            pickmeta.setLore(lore);
            P.setItemMeta(pickmeta);
            p.getInventory().addItem(P);
        }
        return false;
    }
}
