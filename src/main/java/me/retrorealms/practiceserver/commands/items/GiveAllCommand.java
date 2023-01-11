package me.retrorealms.practiceserver.commands.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Red on 11-10-2017.
 */

public class GiveAllCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args){
        Player p = (Player) sender;
        if(!sender.isOp() || sender instanceof ConsoleCommandSender) return false;
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        int num = 1;
        if(args[0] != null) {
            num = Integer.valueOf(args[0]);
        }
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            if(player != p){
                ItemStack thisItem = itemStack;
                thisItem.setAmount(num);
                player.getInventory().addItem(thisItem);
            }
        }
        String itemName = p.getInventory().getItemInMainHand().getType().name();
        if(itemStack.getItemMeta().getDisplayName() != null) itemName = itemStack.getItemMeta().getDisplayName();
        if(args[0] != null && Integer.valueOf(args[0]) > 0) Bukkit.broadcastMessage(ChatColor.GREEN + p.getName() + " has given " + args[0] + " " + itemName + ChatColor.GREEN + " to everyone!");
        return false;
    }
}
