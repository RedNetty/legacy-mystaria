package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
/**
 * Created by Kaveen K (https://digistart.ca)
 * 08/08/2018
 */

public class RRVersionCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		sender.sendMessage(ChatColor.RED + "RetroRealms Version: " + ChatColor.GRAY + PracticeServer.VERSION_STRING);
		return true;
	}

}
