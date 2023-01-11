package me.retrorealms.practiceserver.commands.duels;

import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyDuelCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (args.length == 1) {
                final String player = args[0];
                if (Bukkit.getPlayer(player) != null) {
                    Player recipient = Bukkit.getPlayer(player);
                    if(!Parties.isInParty(p)){
                        p.sendMessage(ChatColor.RED + "Cannot create party duel while not in party, use /duel <name> instead.");
                        return false;
                    }if(!Parties.isInParty(recipient)){
                        p.sendMessage(ChatColor.RED + "This user is not in a party, use /duel <name> instead.");
                        return false;
                    }
                    recipient = Parties.getPartyLeader(recipient);
                    if(!Parties.isPartyLeader(p)){
                        p.sendMessage(ChatColor.RED + "Only the party leader can start duels.");
                        return false;
                    }if(Duels.hasRequest.containsValue(p)){
                        p.sendMessage(ChatColor.RED + "You already an active duel request");
                        return false;
                    }if(recipient == p){
                        p.sendMessage(ChatColor.RED + "You cannot duel yourself.");
                        return false;
                    }if(Duels.duelers.containsKey(recipient)){
                        p.sendMessage(ChatColor.RED + "This player is already in a duel.");
                        return false;
                    }
                    if(!Duels.hasRequest.containsKey(player)){
                        Duels.hasRequest.put(recipient, p);
                        new AsyncTask(() -> {
                            p.sendMessage(ChatColor.RED + "Your duel request has timed out.");
                            Duels.hasRequest.get(p).sendMessage(ChatColor.RED + "Your duel request has timed out.");
                            Duels.hasRequest.remove(p);
                        }).setDelay(15).scheduleDelayedTask();
                        p.sendMessage(ChatColor.GOLD + "Duel request sent to " + recipient.getName());
                        recipient.sendMessage(ChatColor.GOLD + p.getName() + " has requested to duel your party, type /pdaccept to accept this request.");
                        for(Player member : Parties.getEntirePartyOf(p)){
                            member.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "<P>" + ChatColor.GRAY + " Your party leader has requested to duel " + recipient.getName());
                        }for(Player member : Parties.getEntirePartyOf(recipient)){
                            member.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "<P>" + ChatColor.GRAY + " Your party has recieved a duel request from " + p.getName());
                        }

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