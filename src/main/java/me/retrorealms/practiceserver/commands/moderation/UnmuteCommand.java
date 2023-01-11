package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class UnmuteCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (ModerationMechanics.isStaff(p)) {
                if(args.length == 1){
                    Player p2 = Bukkit.getPlayer(args[0]);
                    if (Bukkit.getServer().getPlayer(p2.getName()) == null) {
                        StringUtil.sendCenteredMessage((Player) sender, ChatColor.RED + "Invalid Username.");
                    }else{
                        ChatMechanics.muted.remove(p2);
                        notifyAll(p.getName(), p2);
                    }
                }else{
                    p.sendMessage(ChatColor.RED + "Usage: /psunmute <player>");
                }
            }
        }else if(sender instanceof ConsoleCommandSender){
            if(args.length == 1){
                Player p2 = Bukkit.getPlayer(args[0]);
                if (Bukkit.getServer().getPlayer(p2.getName()) == null) {
                    StringUtil.sendCenteredMessage((Player) sender, ChatColor.RED + "Invalid Username.");
                }else{
                    ChatMechanics.muted.remove(p2);
                    notifyAll("Console", p2);
                }
            }else{
                sender.sendMessage(ChatColor.RED + "Usage: /psunmute <player>");
            }
        }
        return false;
    }

    void notifyAll(String staff, Player p){
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (ModerationMechanics.isStaff(pl)) {
                pl.sendMessage(ChatColor.RED + staff + " has unmuted " + p.getName());
            }
        }
    }
}