package me.retrorealms.practiceserver.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InvSeeCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (p.isOp()) {
            if (args.length == 1) {

                if (target.isOnline()) {
                    Inventory targetInv = target.getInventory();

                    p.openInventory(targetInv);
                } else {
                    p.sendMessage(ChatColor.RED + args[0] + " is not online!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/invsee <player>");
            }
        }
        return false;
    }
}