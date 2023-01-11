package me.retrorealms.practiceserver.mechanics.market.commands;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * Created by Khalid on 8/4/2017.
 */
public class MarketCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(true) return false;
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;

        if (ModerationMechanics.isDonator(p) || ModerationMechanics.isStaff(p)) {
            if (Alignments.chaotic.containsKey(p.getName())) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " do this while chaotic!");
            } else {
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Opening Market...");
                Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
                    PracticeServer.getManagerHandler().getGlobalMarket().openChoiceGUI(p);
                }, 40);

            }
        } else {
            p.sendMessage(ChatColor.RED + "You must be Sub++ or above to use the /market command.");

        }

        return true;
    }
}
