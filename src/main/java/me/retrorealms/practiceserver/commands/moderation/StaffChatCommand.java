package me.retrorealms.practiceserver.commands.moderation;


import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {

            Player p = (Player) sender;
            if (ModerationMechanics.isStaff(p)) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (ModerationMechanics.isStaff(pl)) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < args.length; i++) {
                            builder.append(args[i] + " ");
                        }
                        String msg = builder.toString();
                        String prefix = ChatColor.GRAY + "<" + ChatColor.GOLD.toString() + ChatColor.BOLD + "SC"
                                + ChatColor.GRAY + "> ";
                        pl.sendMessage(prefix + ChatMechanics.getTag(p) + p.getDisplayName() + ": " + ChatColor.WHITE + msg);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_HORSE_GALLOP, 0, 0);
                    }
                }
            }
        }
        return false;
    }
}