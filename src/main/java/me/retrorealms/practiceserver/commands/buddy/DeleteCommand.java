package me.retrorealms.practiceserver.commands.buddy;

import me.retrorealms.practiceserver.mechanics.player.Buddies;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DeleteCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            ArrayList<String> buddy;
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("del") || cmd.getName().equalsIgnoreCase("delete")) {
                if (args.length > 0) {
                    buddy = Buddies.getBuddies(p.getName());
                    if (buddy.contains(args[0].toLowerCase())) {
                        buddy.remove(args[0].toLowerCase());
                        p.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.YELLOW + " has been removed from your BUDDY list.");
                        Buddies.buddies.put(p.getName(), buddy);
                    } else {
                        p.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.YELLOW + " is not on any of your social lists.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Incorrect Syntax - " + ChatColor.BOLD + "/delete <PLAYER>");
                }
            }
        }
        return true;
    }
}
