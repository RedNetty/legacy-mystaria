package me.retrorealms.practiceserver.commands.duels;

import me.retrorealms.practiceserver.mechanics.duels.Duels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DuelAcceptCommand implements CommandExecutor {

        public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
            if (sender instanceof Player) {
                final Player p = (Player) sender;
                if (Duels.hasRequest.containsKey(p)) {
                    ArrayList<Player> team1 = new ArrayList();
                    team1.add(Duels.hasRequest.get(p));
                    ArrayList<Player> team2 = new ArrayList();
                    team2.add(p);
                    Duels.createDuel(team1, team2, Duels.hasRequest.get(p), p, false);
                    Duels.hasRequest.remove(p);
                }else{
                    p.sendMessage(ChatColor.RED + "You do not have any duel requests at this time.");
            }
        }
        return false;
    }
}
