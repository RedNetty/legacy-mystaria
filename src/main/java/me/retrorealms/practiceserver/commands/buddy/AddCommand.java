package me.retrorealms.practiceserver.commands.buddy;

import me.retrorealms.practiceserver.mechanics.player.Buddies;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AddCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            ArrayList<String> buddy;
            Player p = (Player) sender;
            if (args.length > 0) {
                buddy = Buddies.getBuddies(p.getName());
                if (!buddy.contains(args[0].toLowerCase())) {
                    if (args[0].equalsIgnoreCase(p.getName())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't add yourself to your buddy list!");
                    } else {
                        buddy.add(args[0].toLowerCase());
                        p.sendMessage(ChatColor.GREEN + "You've added " + ChatColor.BOLD + args[0] + ChatColor.GREEN + " to your BUDDY list.");
                        Buddies.buddies.put(p.getName(), buddy);
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + args[0] + ChatColor.YELLOW + " is already on your BUDDY LIST.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Incorrect Syntax - " + ChatColor.BOLD + "/add <PLAYER>");
            }
        }
        return false;
    }
}
