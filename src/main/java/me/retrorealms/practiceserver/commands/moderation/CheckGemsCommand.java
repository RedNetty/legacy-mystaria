package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Jaxon on 9/26/2017.
 */
public class CheckGemsCommand implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player && sender.isOp()) {
            Player player = (Player)sender;
            String playerName = args[0];
            if(Bukkit.getPlayer(playerName) != null) {
                player.sendMessage(ChatColor.GREEN.toString() + Economy.getBalance(Bukkit.getPlayer(playerName).getUniqueId()) + "G");
            }
        }
        return false;
    }
}
