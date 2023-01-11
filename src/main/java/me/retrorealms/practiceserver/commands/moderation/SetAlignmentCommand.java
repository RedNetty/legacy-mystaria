package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Subby on 08/08/2017. :Thinking: refer to setRank for the original code
 */
public class SetAlignmentCommand implements CommandExecutor {

    public void incorrectArgs(CommandSender sender) {
        sender.sendMessage(ChatColor.RED.toString() + "/setallignment <PLAYER> <Allighment>");
        sender.sendMessage(
                ChatColor.RED + "Allignment: " + ChatColor.GRAY + "LAWFUL | NEUTRAL | CHAOTIC");
    }

    public int CHAOTIC_SECONDS = 600;
    public int NEUTRAL_SECONDS = 120;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp() || sender instanceof ConsoleCommandSender) {
            if (args.length == 2) {
                Player player2 = Bukkit.getPlayer(args[0]);
                String allignment = args[1].toLowerCase();
                switch (allignment) {

                    case "lawful":
                        Alignments.setLawful(player2);
                        //        TTA_Methods.sendActionBar(player2, ChatColor.GREEN + "* YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *", 60);
                        StringUtil.sendCenteredMessage((Player) sender, ChatColor.GREEN + "You have set " + player2.getName() + "'s allignment to " + allignment);
                        break;
                    case "neutral":
                        Alignments.setNeutral(player2);
                        StringUtil.sendCenteredMessage((Player) sender, ChatColor.GREEN + "You have set " + player2.getName() + "'s allignment to " + allignment);
                        break;
                    case "chaotic":
                        Alignments.setChaotic(player2, CHAOTIC_SECONDS);
                        StringUtil.sendCenteredMessage((Player) sender, ChatColor.GREEN + "You have set " + player2.getName() + "'s allignment to " + allignment);
                        break;
                    default:
                        StringUtil.sendCenteredMessage((Player) sender, ChatColor.RED + "You entered an invalid allignment");
                        incorrectArgs(sender);
                        break;
                }

                if (Bukkit.getServer().getPlayer(player2.getName()) == null) {
                    StringUtil.sendCenteredMessage((Player) sender, ChatColor.RED + "Please enter a valid username");
                }
            }

        }
        return false;
    }
}