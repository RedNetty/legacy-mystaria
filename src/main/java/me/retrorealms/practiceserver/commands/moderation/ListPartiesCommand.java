package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ListPartiesCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player && !ModerationMechanics.isStaff(((Player) sender))) return false;
        ArrayList<Player> notInParty = new ArrayList();
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(Parties.isInParty(p) && Parties.isPartyLeader(p)){
                String message = ChatColor.YELLOW + p.getName() + ": " + ChatColor.GRAY;
                for(Player member: Parties.getParties().get(p)){
                    if(member != p) message += member.getName() + " ";
                }
                sender.sendMessage(message);
            }else{
                if(!Parties.isInParty(p)) notInParty.add(p);
            }
        }
        String noparty = ChatColor.RED + "Not in Party: " + ChatColor.GRAY;
        for(Player p : notInParty){
            noparty += p.getName() + " ";
        }
        sender.sendMessage(noparty);
        return true;
    }
}
