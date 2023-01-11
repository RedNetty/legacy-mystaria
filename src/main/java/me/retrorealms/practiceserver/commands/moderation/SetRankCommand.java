package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.player.GamePlayer.StaticConfig;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class SetRankCommand implements CommandExecutor {

    public void incorrectArgs(CommandSender sender) {
        sender.sendMessage(ChatColor.RED.toString() + "/setrank <PLAYER> <RANK>");
        sender.sendMessage(
                ChatColor.RED + "Ranks: " + ChatColor.GRAY + "Default | SUB | SUB+ | SUB++ | SUB+++ | SUPPORTER | PMOD | BUILDER | YOUTUBER");
    }
/*:thinking: F I X E D*/
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender || sender.isOp()) {
            if (args.length == 2) {
                Player player2 = Bukkit.getPlayer(args[0]);
                String rank = args[1].toLowerCase();
                switch (rank) {
                    case "pmod":
                    case "sub":
                    case "supporter":
                    case "sub+":
                    case "sub++":
                    case "sub+++":
                    case "youtuber":
                    case "quality":
                    case "builder":
                    case "gm":
                    case "manager":
                    case "dev":
                    case "default":
                        if (Bukkit.getServer().getPlayer(player2.getUniqueId()) != null) {
                            RankEnum rankEnum = RankEnum.fromString(rank);
                            ModerationMechanics.rankHashMap.put(player2.getUniqueId(), rankEnum);
                            SQLMain.updateRank(player2);
                            if (sender instanceof Player) {
                                StringUtil.sendCenteredMessage((Player) sender, ChatColor.GREEN + "You have set " + player2.getName() + "'s rank to " + rank);
                            }
                            if(!PracticeServer.DATABASE){
                                StaticConfig.get().set(player2.getUniqueId() + ".Main.Rank", rank);
                                StaticConfig.save();
                            }

                            Alignments.updatePlayerAlignment(player2);
                        }
                        break;
                    default:
                        if (sender instanceof Player) {
                            incorrectArgs(sender);
                        }
                }
            } else {
                if (sender instanceof Player) {
                    incorrectArgs(sender);
                }
            }
        }
        return false;
    }
}