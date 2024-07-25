package me.retrorealms.practiceserver.commands.chat;

import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(ChatMechanics.muted.containsKey(p)){
                p.sendMessage(ChatColor.RED + "You are currently muted");
                if(ChatMechanics.muted.get(p) > 0) {
                    Integer minutes = ChatMechanics.muted.get(p) / 60;
                    p.sendMessage(ChatColor.RED + "Your mute expires in " + minutes.toString() + " minutes.");
                }else{
                    p.sendMessage(ChatColor.RED + "Your mute WILL NOT expire.");
                }
                return true;
            }
            if (args.length == 1) {
                Player reciever = Bukkit.getServer().getPlayer(args[0]);
                if (reciever == null || reciever.isOp() && !p.isOp()) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " is OFFLINE.");
                } else {

                    if (Toggles.isToggled(reciever, "Player Messages")) {
                        String playerPrefix = ChatMechanics.getTag(p) + ChatMechanics.getDisplayNameFor(p, reciever);
                        String senderPrefix = ChatMechanics.getTag(reciever) + ChatMechanics.getDisplayNameFor(reciever, p);
                        reciever.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + playerPrefix + ": " + ChatColor.WHITE + "/" + label + " " + args[0]);
                        p.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + senderPrefix + ": " + ChatColor.WHITE + "/" + label + " " + args[0]);
                        reciever.playSound(reciever.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                        ChatMechanics.reply.put(reciever, p);

                    } else {
                        p.sendMessage(ChatColor.RED + args[0] + " has PMs disabled.");
                    }
                }
            } else if (args.length >= 2) {
                Player reciever = Bukkit.getServer().getPlayer(args[0]);
                if (reciever == null || VanishCommand.vanished.contains(reciever.getName().toLowerCase()) && !p.isOp()) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " is OFFLINE.");
                } else {
                    String message = "";
                    int i = 1;
                    while (i < args.length) {
                        message = String.valueOf(message) + args[i] + " ";
                        ++i;
                    }
                    if (message.contains("@i@") && p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR) {
                        ChatMechanics.sendShowString(reciever, p.getInventory().getItemInMainHand(), ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + p.getName(), message, reciever);
                        ChatMechanics.sendShowString(p, p.getInventory().getItemInMainHand(), ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + reciever.getName(), message, p);
                    } else {

                        if (Toggles.isToggled(reciever, "Player Messages")) {
                            reciever.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + p.getDisplayName() + ": " + ChatColor.WHITE + message);
                            p.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + reciever.getDisplayName() + ": " + ChatColor.WHITE + message);
                        } else {
                            p.sendMessage(ChatColor.RED + args[0] + " has PMs disabled.");
                            return true;
                        }
                    }
                    reciever.playSound(reciever.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                    ChatMechanics.reply.put(reciever, p);
                }
            } else {
                p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Incorrect syntax. " + "/" + label + " <PLAYER> <MESSAGE>");
            }
        }
        return true;
    }
}
