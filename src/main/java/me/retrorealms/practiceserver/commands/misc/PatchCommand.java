package me.retrorealms.practiceserver.commands.misc;

import java.net.URL;
import java.util.Scanner;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Giovanni on 7-7-2017
 * Redone by Kaveen K (https://digistart.ca) on 8/05/2018
 */
public class PatchCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

		if (!(commandSender instanceof Player))
			return false;

		if (args.length == 1 && args[0].equalsIgnoreCase("refresh")) {
			if (commandSender.isOp()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							PracticeServer.patchnotes.clear();
							URL url = new URL("https://retrorealms.net/patchnotes.txt");
							Scanner scan = new Scanner(url.openStream());
							while (scan.hasNext()) {
								PracticeServer.patchnotes.add(scan.nextLine());
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.runTaskAsynchronously(PracticeServer.plugin);
				commandSender.sendMessage(ChatColor.GREEN + "Attempted a patch notes refresh.");
				return true;

			} else {
				commandSender.sendMessage(ChatColor.RED + "No Permission");
				return true;
			}

		}

		Player player = (Player) commandSender;
		for (String patchline : PracticeServer.patchnotes) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', patchline));

		}

		return true;
	}
}
