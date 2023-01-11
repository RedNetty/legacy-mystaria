package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Giovanni on 13-5-2017.
 */
public class GiveEnchantCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {


        if(!commandSender.isOp()) return false;

        if (strings.length > 0) {

            String playerTarget = strings[0];
            int tier = 0;
            int type = 0;

            try {
                tier = Integer.valueOf(strings[1]);
            } catch (Exception e) {
                commandSender.sendMessage(ChatColor.RED + "Invalid TIER (must be: 0/1/2/3/4/5)");

                return false;
            }

            try {
                type = Integer.valueOf(strings[2]);
            } catch (Exception e) {
                commandSender.sendMessage(ChatColor.RED + "Invalid TYPE (must be: 0/1)");

                return false;
            }

            if (Bukkit.getPlayer(playerTarget) != null && Bukkit.getPlayer(playerTarget).isOnline()) {
                Player player = Bukkit.getPlayer(playerTarget);

                ItemStack itemStack = Items.enchant(tier + 1, type, false);

                player.getInventory().addItem(itemStack);
            }

        }

        return false;
    }
}
