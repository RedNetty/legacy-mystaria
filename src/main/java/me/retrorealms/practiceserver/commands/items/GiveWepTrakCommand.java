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
 * Created by Egimsun on 11-8-2017.
 */

public class GiveWepTrakCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args){
        if(!sender.isOp()) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if(p != null && p.isOnline()){
            ItemStack tracker = new ItemStack(Material.NETHER_STAR);
            ItemMeta itemMeta = tracker.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD + "Weapon Stat Tracker");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1");
            lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Apply to any weapon to start tracking");
            lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "stats as you use it.");
            lore.add(ChatColor.GRAY + "Permanent Untradeable");
            itemMeta.setLore(lore);
            tracker.setItemMeta(itemMeta);
            if(args.length > 1){
                tracker.setAmount(Integer.parseInt(args[1]));
            }
            p.getInventory().addItem(tracker);
        }
        return false;
    }
}
