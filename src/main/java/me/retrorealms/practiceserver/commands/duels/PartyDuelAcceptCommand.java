package me.retrorealms.practiceserver.commands.duels;

import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyDuelAcceptCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (Duels.hasRequest.containsKey(p)) {
                int maxPlayers = 1;
                for(Player team1 : Parties.getParties().get(Duels.hasRequest.get(p))){
                    maxPlayers = Math.max(maxPlayers, Duels.rankPlayers(team1));
                }for(Player team2 : Parties.getParties().get(p)){
                    maxPlayers = Math.max(maxPlayers, Duels.rankPlayers(team2));
                }
                if(Parties.getParties().get(p).size() > maxPlayers || Parties.getParties().get(Duels.hasRequest.get(p)).size() > maxPlayers){
                    for(Player member : Parties.getEntirePartyOf(p)){
                        member.sendMessage(ChatColor.RED + "Duel Cancelled: One member in either party must be higher rank to support this many players.");
                    }for(Player member : Parties.getEntirePartyOf(Duels.hasRequest.get(p))){
                        member.sendMessage(ChatColor.RED + "Duel Cancelled: One member in either party must be higher rank to support this many players.");
                    }
                    return false;
                }
                Duels.createDuel(Parties.getEntirePartyOf(Duels.hasRequest.get(p)), Parties.getEntirePartyOf(p), Duels.hasRequest.get(p), p, true);
                Duels.hasRequest.remove(p);
            }else{
                p.sendMessage(ChatColor.RED + "You do not have any duel requests at this time.");
            }
        }
        return false;
    }

}