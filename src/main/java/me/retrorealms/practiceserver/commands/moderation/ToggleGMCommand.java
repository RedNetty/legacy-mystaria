package me.retrorealms.practiceserver.commands.moderation;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.retrorealms.practiceserver.mechanics.player.Listeners;

public class ToggleGMCommand implements CommandExecutor {
	public static ArrayList<String> togglegm = new ArrayList<String>();
	/**
	 * Created by Kaveen K (https://digistart.ca)
	 * 08/07/2018
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender instanceof Player) {
			if (sender.isOp()) {
				Player player = (Player) sender;
				if (togglegm.contains(player.getName())) {
					togglegm.remove(player.getName());
					Listeners.hpCheck(player);
					player.setMaxHealth(10000);
					player.setHealth(10000);
					sender.sendMessage(ChatColor.AQUA + "You have enabled GM mode again.");
					return true;
				} else {
					togglegm.add(player.getName());
					Listeners.hpCheck(player);
					if (VanishCommand.vanished.contains(player.getName().toLowerCase())) {
						VanishCommand.vanished.remove(player.getName().toLowerCase());
						for (Player pl : Bukkit.getOnlinePlayers()) {
							if (pl == player)
								continue;
							pl.showPlayer(player);
						}
						player.sendMessage(ChatColor.RED + "You are now " + ChatColor.BOLD + "visible.");
					}
					sender.sendMessage(ChatColor.RED + "You have disabled GM mode.");
				}

			}

		}

		return true;
	}

}
