package me.retrorealms.practiceserver.commands.moderation;


import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class AwardTokensCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.isOp() || sender instanceof ConsoleCommandSender){
            for(Player p : Bukkit.getOnlinePlayers()){
                SQLMain.updatePlayerStats(p);
            }
            if(args.length < 1){
                sender.sendMessage("/awardtokens confirm to give everyone their tokens for the wipe");
                return true;
            } else if (args[0].contains("test")){
                PersistentPlayers.assignTokens(true);
            }else if (args[0].contains("confirm")){
                PersistentPlayers.assignTokens(false);
            }else{
                sender.sendMessage("/awardtokens confirm to give everyone their tokens for the wipe");
                return true;
            }
            Bukkit.broadcastMessage(ChatColor.GREEN + "Tokens have been awarded.");
        }
        return false;
    }
}