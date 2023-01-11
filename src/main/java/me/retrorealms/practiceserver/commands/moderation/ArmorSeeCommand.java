package me.retrorealms.practiceserver.commands.moderation;

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

public class ArmorSeeCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (p.isOp()) {
            if (args.length == 1) {

                if (target.isOnline()) {
                    Inventory targetInv = Bukkit.createInventory(null, 9, target.getName() + "'s ArmorSee");
                    List<ItemStack> itemStackList = new ArrayList<>();
                    for (ItemStack itemStack : target.getInventory().getArmorContents()) {
                        if (itemStack == null || (itemStack.getType() == Material.AIR)) {
                            continue;
                        }
                        itemStackList.add(itemStack);
                    }
                    itemStackList.forEach(targetInv::addItem);
                    p.openInventory(targetInv);
                } else {
                    p.sendMessage(ChatColor.RED + args[0] + " is not online!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "/armorsee <player>");
            }
        }
        return false;
    }
}