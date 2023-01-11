package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import java.util.UUID;

public class GiveTokensCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givetokens <Player> <Amount>");
            return true;
        }
        UUID uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
        if (PersistentPlayers.get(uuid) == null) {
            sender.sendMessage(ChatColor.RED + "Player does not exist");
            return true;
        }

        PersistentPlayer pp = PersistentPlayers.get(uuid);
        int amount = 0;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Usage: /givetokens <Player> <Amount>");
            return true;
        }
        pp.tokens = pp.tokens + amount;
        sender.sendMessage(ChatColor.GREEN + "You have given " + args[0] + " " + amount + " Tokens.");

        return false;
    }
}