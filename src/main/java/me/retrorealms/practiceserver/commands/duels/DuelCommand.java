package me.retrorealms.practiceserver.commands.duels;

import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class DuelCommand implements CommandExecutor {



    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (args.length == 1) {
                final String player = args[0];
                if (Bukkit.getPlayer(player) != null) {
                    Player recipient = Bukkit.getPlayer(player);
                    if(Duels.hasRequest.containsValue(p)){
                        p.sendMessage(ChatColor.RED + "You already an active duel request");
                        return false;
                    }if(recipient == p){
                        p.sendMessage(ChatColor.RED + "You cannot duel yourself.");
                        return false;
                    }if(Duels.duelers.containsKey(recipient)){
                        p.sendMessage(ChatColor.RED + "This player is already in a duel.");
                        return false;
                    }
                    if(!Duels.hasRequest.containsKey(recipient)){
                        Duels.hasRequest.put(recipient, p);
                        new AsyncTask(() -> {
                            p.sendMessage(ChatColor.RED + "Your duel request has timed out.");
                            Duels.hasRequest.get(p).sendMessage(ChatColor.RED + "Your duel request has timed out.");
                            Duels.hasRequest.remove(p);
                        }).setDelay(15).scheduleDelayedTask();
                        p.sendMessage(ChatColor.GOLD + "Duel request sent to " + recipient.getName());
                        recipient.sendMessage(ChatColor.GOLD + p.getName() + " has requested to duel you, type /daccept to accept this request.");
                    }else{
                        p.sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + player + ChatColor.RED + " has an active duel request");
                    }
                } else {
                    p.sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + player + ChatColor.RED + " is OFFLINE");
                }
            } else {
                p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/duel <player>").toString());
            }
        }
        return false;
    }
}

