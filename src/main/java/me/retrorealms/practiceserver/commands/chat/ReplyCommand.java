package me.retrorealms.practiceserver.commands.chat;

import me.retrorealms.practiceserver.commands.moderation.VanishCommand;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            int n;
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
            if (ChatMechanics.reply.containsKey(p)) {
                Player reciever = ChatMechanics.reply.get(p);
                if (reciever == null || VanishCommand.vanished.contains(reciever.getName().toLowerCase()) && !p.isOp()) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + ChatMechanics.reply.get(p).getName() + ChatColor.RED + " is OFFLINE.");
                } else if (args.length == 0) {
                    reciever.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + p.getDisplayName() + ": " + ChatColor.WHITE + "/" + label + " " + reciever.getName());
                    p.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + reciever.getDisplayName() + ": " + ChatColor.WHITE + "/" + label + " " + reciever.getName());
                    reciever.playSound(reciever.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                    ChatMechanics.reply.put(reciever, p);
                } else if (args.length >= 1) {
                    String message = "";
                    int n2 = args.length;
                    n = 0;
                    while (n < n2) {
                        String s = args[n];
                        message = String.valueOf(message) + s + " ";
                        ++n;
                    }
                    if (message.contains("@i@") && p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR) {
                        ChatMechanics.sendShowString(reciever, p.getInventory().getItemInMainHand(), ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + p.getName(), message, reciever);
                        ChatMechanics.sendShowString(p, p.getInventory().getItemInMainHand(), ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + reciever.getName(), message, p);
                    } else {
                        if (Toggles.hasPMEnabled(reciever)) {
                            String playerPrefix = ChatMechanics.getDisplayNameFor(p, reciever);
                            String senderPrefix = ChatMechanics.getDisplayNameFor(reciever, p);
                            reciever.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "FROM " + playerPrefix + ": " + ChatColor.WHITE + message);
                            p.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "TO " + senderPrefix + ": " + ChatColor.WHITE + message);
                            reciever.playSound(reciever.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                            ChatMechanics.reply.put(reciever, p);
                        } else {
                            p.sendMessage(ChatColor.RED + args[0] + " has PMs disabled.");
                        }//This is a test for git fml
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You have no conversation to respond to!");
            }
        }
        return true;
    }
}
