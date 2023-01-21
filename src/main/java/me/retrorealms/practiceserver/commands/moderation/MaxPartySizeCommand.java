package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MaxPartySizeCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command cmd, final String label, final String[] args) {
        if (sender instanceof Player && !sender.isOp()) return false;
        if(args.length < 1 || args.length > 1){
            sender.sendMessage(ChatColor.RED + "Usage: /maxpartysize <number>");
            return false;
        }
        try {
            Parties.maxSize = (Integer.parseInt(args[0]));
            sender.sendMessage(ChatColor.GREEN + "Max party size has been set to " + args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Usage: /maxpartysize <number>");
            return true;
        }
        return true;
    }
}
