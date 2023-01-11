package me.retrorealms.practiceserver.commands.party;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PartyCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("p")) {
                if (!Parties.isInParty(p)) {
                    p.sendMessage(ChatColor.RED + "You are not in a party.");
                    return true;
                }
                if (args.length == 0) {
                    p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/p <MSG>").toString());
                    return true;
                }
                String msg = "";
                for (final String s : args) {
                    msg = String.valueOf(msg) + s + " ";
                }
                final ArrayList<Player> mem = Parties.parties.get(Parties.getParty(p));
                for (final Player pl : mem) {
                    pl.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + " " + p.getDisplayName() + ": " + ChatColor.GRAY + msg);
                }
            }
        }
        return false;
    }

}
