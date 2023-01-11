package me.retrorealms.practiceserver.commands.party;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PQuitCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
                if (args.length == 0) {
                    if (!Parties.isInParty(p)) {
                        p.sendMessage(ChatColor.RED + "You are not in a party.");
                        return true;
                    }
                    p.sendMessage(String.valueOf(ChatColor.RED.toString()) + "You have left the party.");
                    Parties.removePlayer(p);
                } else {
                    p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/pquit").toString());
                }
            }

        return false;
    }
}
