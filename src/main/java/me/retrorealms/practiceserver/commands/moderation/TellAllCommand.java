package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TellAllCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player && ModerationMechanics.getRank((Player) sender) == RankEnum.BUILDER) || (sender.isOp())) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String allArgs = sb.toString().trim();
                Bukkit.broadcastMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + ">>> " + ChatColor.AQUA + allArgs);
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    ActionBar.sendActionBar(pl, ChatColor.AQUA.toString() + ChatColor.BOLD + ">>> " + ChatColor.AQUA + allArgs + ChatColor.AQUA + ChatColor.BOLD + " <<<", 5);
                    pl.playSound(pl.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                    pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        return false;
    }
}
