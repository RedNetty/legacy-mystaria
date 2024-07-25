package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.retrorealms.practiceserver.mechanics.pvp.Alignments;


public class ToggleAlignmentGlowCommand implements CommandExecutor {
	public static boolean runningGlow = false;
	/**
	 * Created by Kaveen K (https://digistart.ca)
	 * 08/08/2018
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if (sender.isOp()) {

		}

		return true;
	}

}
