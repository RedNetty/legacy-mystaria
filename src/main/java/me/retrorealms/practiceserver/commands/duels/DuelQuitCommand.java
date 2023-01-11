package me.retrorealms.practiceserver.commands.duels;

import me.retrorealms.practiceserver.mechanics.duels.Duels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelQuitCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if(Duels.duelers.containsKey(p)){
                Duels.duelers.get(p).exitDuel(false, false);
            }else{
                p.sendMessage(ChatColor.RED + "You are not in a duel.");
            }
        }
        return false;
    }

}