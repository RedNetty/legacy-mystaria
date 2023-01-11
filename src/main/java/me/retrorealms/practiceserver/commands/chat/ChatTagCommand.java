package me.retrorealms.practiceserver.commands.chat;

import me.retrorealms.practiceserver.enums.chat.ChatTag;
import me.retrorealms.practiceserver.mechanics.chat.ChatMechanics;
import me.retrorealms.practiceserver.mechanics.chat.ChatTagGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 3-5-2017.
 */
public class ChatTagCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (command.getName().equalsIgnoreCase("tags")) {

            if (commandSender instanceof ConsoleCommandSender) {
            /* /tags <player> <tagName> */
                String playerName = strings[0];
                String tagName = strings[1];

                if (Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).isOnline()) {

                    Player player = Bukkit.getPlayer(playerName);

                    ChatMechanics.unlockTag(player, ChatTag.valueOf(tagName));

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYour chat-tag has arrived, toggle it using &n/tags&a."));

                    return false;
                }


            } else {
                Player player = (Player) commandSender;

                new ChatTagGUI(player);
            }
        }
        return false;
    }
}
