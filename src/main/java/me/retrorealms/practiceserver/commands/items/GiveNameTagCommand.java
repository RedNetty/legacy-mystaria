package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.donations.Nametags.Nametag;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveNameTagCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(!sender.isOp()) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if (p != null && p.isOnline()) {
            ItemStack item = Nametag.item_ownership_tag;
            item.setAmount(Integer.parseInt(args[1]));
            p.getInventory().addItem(item);
        }


        return false;
    }
}
