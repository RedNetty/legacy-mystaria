package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;

public class LeaderboardCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        CommandSender p = sender;
        try{
        if (args.length == 1) {
            ResultSet rs ;
            String target;
            if(sender instanceof Player) SQLMain.updatePlayerStats((Player) sender);
            if(args[0].toLowerCase().contains("t1")){
                rs = getPlayerData("PlayerData", "Username, T1Kills", "T1Kills");
                target = "T1Kills";
            }else if(args[0].toLowerCase().contains("t2")){
                rs = getPlayerData("PlayerData", "Username, T2Kills", "T2Kills");
                target = "T2Kills";
            }else if(args[0].toLowerCase().contains("t3")){
                rs = getPlayerData("PlayerData", "Username, T3Kills", "T3Kills");
                target = "T3Kills";
            }else if(args[0].toLowerCase().contains("t4")){
                rs = getPlayerData("PlayerData", "Username, T4Kills", "T4Kills");
                target = "T4Kills";
            }else if(args[0].toLowerCase().contains("t5")){
                rs = getPlayerData("PlayerData", "Username, T5Kills", "T5Kills");
                target = "T5Kills";
            }else if(args[0].toLowerCase().contains("t6")){
                rs = getPlayerData("PlayerData", "Username, T6Kills", "T6Kills");
                target = "T6Kills";
            }else if(args[0].toLowerCase().contains("mob")){
                rs = getPlayerData("PlayerData", "Username, (T1Kills+T2Kills+T3Kills+T4Kills+T5Kills+T6Kills) as MobKills", "MobKills");
                target = "MobKills";
            }else if(args[0].toLowerCase().contains("kills")){
                rs = getPlayerData("PlayerData", "Username, PlayerKills", "PlayerKills");
                target = "PlayerKills";
            }else if(args[0].toLowerCase().contains("deaths")){
                rs = getPlayerData("PlayerData", "Username, Deaths", "Deaths");
                target = "Deaths";
            }else if(args[0].toLowerCase().contains("maxhp")){
                rs = getPlayerData("PlayerData", "Username, MaxHP", "MaxHP");
                target = "MaxHP";
            }else if(args[0].toLowerCase().contains("min")){
                rs = getPlayerData("PlayerData", "Username, OreMined", "OreMined");
                target = "OreMined";
            }else if(args[0].toLowerCase().contains("gem")){
                rs = getPlayerData("PlayerData", "Username, Gems", "Gems");
                target = "Gems";
            }else{
                p.sendMessage(ChatColor.RED + "/leaderboard <T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, MobKills, PlayerKills, Deaths, OreMined, MaxHP>");
                return true ;
            }

            p.sendMessage(ChatColor.GREEN + "------------------");
            p.sendMessage(ChatColor.GREEN + target + " Leaderboard");
            for(int i = 0; i < 10 && rs.next(); i++){
                String username = rs.getString("Username");
                String targetstring = Integer.toString(rs.getInt(target));
                p.sendMessage(ChatColor.GRAY+ username + ": " + ChatColor.GREEN + targetstring);
            }
            p.sendMessage(ChatColor.GREEN + "------------------");
        } else {
            p.sendMessage(ChatColor.RED + "/leaderboard <T1Kills, T2Kills, T3Kills, T4Kills, T5Kills, MobKills, PlayerKills, Deaths, OreMined, MaxHP>");
        }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static ResultSet getPlayerData(String table, String columns, String orderby) {
        try {
            return SQLMain.con.createStatement().executeQuery("SELECT " + columns + " FROM " + table + " ORDER BY " + orderby + " DESC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}