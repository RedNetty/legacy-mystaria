package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CloneCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (p.isOp()) {
            if (args.length == 1) {
                if (Bukkit.getServer().getOfflinePlayer(args[0]).isOnline()) {
                    SQLMain.updatePlayerStats(Bukkit.getServer().getPlayer(args[0]));
                }
                SQLMain.clonePlayer(p, args[0]);
            } else {
                p.sendMessage(ChatColor.RED + "/playerclone <player>");
            }
        }
        return false;
    }
}