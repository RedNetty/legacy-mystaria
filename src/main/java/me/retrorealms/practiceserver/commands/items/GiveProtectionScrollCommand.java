package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 6-5-2017.
 */
public class GiveProtectionScrollCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!commandSender.isOp()) return false;

        if (strings.length > 0) {

            String playerTarget = strings[0];
            int tier = 0;

            try {
                tier = Integer.valueOf(strings[1]);
            } catch (Exception e) {
                commandSender.sendMessage(ChatColor.RED + "Invalid TIER (must be: 0/1/2/3/4)");

                return false;
            }

            if (Bukkit.getPlayer(playerTarget) != null && Bukkit.getPlayer(playerTarget).isOnline()) {
                Player player = Bukkit.getPlayer(playerTarget);

                ItemStack itemStack = ItemAPI.getScrollGenerator().next(tier);

                player.getInventory().addItem(itemStack);
            }

        }

        return false;
    }
}
