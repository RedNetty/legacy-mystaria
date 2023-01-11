package me.retrorealms.practiceserver.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.isOp()) return true;
        if(args == null || args[0] == null){
            sender.sendMessage(ChatColor.RED + "Player name required");
            return true;
        }
        Player p = Bukkit.getServer().getPlayer(args[0]);
        if(p != null){
            p.setHealth(p.getMaxHealth());
            sender.sendMessage(ChatColor.GREEN + "Healed " + p.getDisplayName());
        }else{
            sender.sendMessage(ChatColor.RED + "Invalid Player Name");
            return true;
        }
        return false;
    }
}
