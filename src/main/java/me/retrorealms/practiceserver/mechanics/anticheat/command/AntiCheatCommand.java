package me.retrorealms.practiceserver.mechanics.anticheat.command;

import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiCheatCommand implements CommandExecutor {
    private final AdvancedAntiCheat antiCheat;

    public AntiCheatCommand(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("anticheat.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                antiCheat.getConfigManager().loadConfig();
                sender.sendMessage(ChatColor.GREEN + "AntiCheat configuration reloaded.");
                break;
            case "stats":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /anticheat stats <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                sendPlayerStats(sender, target);
                break;
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /anticheat reset <player> [checkType]");
                    return true;
                }
                resetPlayerViolations(sender, args[1], args.length > 2 ? args[2] : null);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "AntiCheat Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat reload " + ChatColor.WHITE + "- Reload the configuration");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat stats <player> " + ChatColor.WHITE + "- View player's AntiCheat stats");
        sender.sendMessage(ChatColor.YELLOW + "/anticheat reset <player> [checkType] " + ChatColor.WHITE + "- Reset player's violation levels");
    }

    private void sendPlayerStats(CommandSender sender, Player target) {
        ACPlayerData data = antiCheat.getPlayerData(target);
        sender.sendMessage(ChatColor.GOLD + "AntiCheat Stats for " + target.getName() + ":");
        sender.sendMessage(ChatColor.YELLOW + "Last Speed: " + ChatColor.WHITE + String.format("%.2f", data.getLastSpeed()));
        sender.sendMessage(ChatColor.YELLOW + "Air Ticks: " + ChatColor.WHITE + data.getAirTicks());
        // Add more stats as needed
    }

    private void resetPlayerViolations(CommandSender sender, String playerName, String checkType) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (checkType == null) {
            antiCheat.getViolationManager().resetViolations(target.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Reset all violation levels for " + playerName);
        } else {
            antiCheat.getViolationManager().resetViolations(target.getUniqueId(), checkType);
            sender.sendMessage(ChatColor.GREEN + "Reset violation levels for " + playerName + " for check type: " + checkType);
        }
    }
}