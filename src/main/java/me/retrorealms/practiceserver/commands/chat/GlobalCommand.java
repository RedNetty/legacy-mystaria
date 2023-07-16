package me.retrorealms.practiceserver.commands.chat;

import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


/**
 * Created by Jaxon on 7/17/2017.
 */
public class GlobalCommand implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        try {
            if (args.length == 0) {
                return false;
            }
            if (!(commandSender instanceof Player)) {
                return false;
            }
            Player player = (Player) commandSender;
            if(ChatMechanics.muted.containsKey(player)){
                player.sendMessage(ChatColor.RED + "You are currently muted");
                if(ChatMechanics.muted.get(player) > 0) {
                    Integer minutes = ChatMechanics.muted.get(player) / 60;
                    player.sendMessage(ChatColor.RED + "Your mute expires in " + minutes.toString() + " minutes.");
                }else{
                    player.sendMessage(ChatColor.RED + "Your mute WILL NOT expire.");
                }
                return false;
            }
            String fullMessage = StringUtil.getFullMessage(args, 0);
            String globalPrefix = ChatColor.translateAlternateColorCodes('&', "&b<&b&lG&b> ");
            if (fullMessage.toLowerCase().contains("wts") || fullMessage.contains("trading") || fullMessage.contains("selling") || fullMessage.contains("casino") || fullMessage.contains("wtb")) {
                globalPrefix = ChatColor.translateAlternateColorCodes('&', "&a<&a&lT&a> ");
            }
            ItemStack iteminHand = player.getInventory().getItemInMainHand();
            for (Player randomPlayer : Bukkit.getOnlinePlayers()) {

                if (fullMessage.toLowerCase().contains("@i@") && player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    ChatMechanics.sendShowString(player, iteminHand, globalPrefix + ChatMechanics.fullDisplayName(player), fullMessage, randomPlayer);
                } else {
                    randomPlayer.sendMessage(globalPrefix + ChatMechanics.fullDisplayName(player) + ": " + ChatColor.WHITE + fullMessage);
                }
            }
        } catch (Exception e) {

        }

        return false;
    }
}
