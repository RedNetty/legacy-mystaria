package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 8-7-2017.
 */
public class GiveBuffCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(!sender.isOp()) return false;

        if (strings.length > 0) {

            String playerTarget = strings[0];
            int rate = 0;

            try {
                rate = Integer.valueOf(strings[1]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Improvement rate must be a number.");

                return false;
            }

            if (Bukkit.getPlayer(playerTarget) != null && Bukkit.getPlayer(playerTarget).isOnline()) {
                Player player = Bukkit.getPlayer(playerTarget);

                player.getInventory().addItem(PracticeServer.buffHandler().newBuffItem(playerTarget, player.getUniqueId(), rate));
            }
        }
        return false;
    }
}
