package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.pvp.Deadman;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeadmanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(sender instanceof Player && !sender.isOp()) return false;
        if(args.length < 1 || args.length > 1){
            sender.sendMessage(ChatColor.RED + "Usage: /deadman <end/start/grace/pvp/deathmatch>");
            return false;
        }
        if(args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("stop")){
            if(Deadman.stage < 1){
                sender.sendMessage(ChatColor.RED + "Deadman not started, /deadman start to begin.");
                return false;
            }else{
                Deadman.setStage(0);
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("start")){
            if(Deadman.stage > 1){
                sender.sendMessage(ChatColor.RED + "Deadman already started, use grace/pvp/deathmatch to advance it to the next stage");
                return false;
            }else{
                Deadman.setStage(1);
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("grace")){
            if(Deadman.stage > 2){
                sender.sendMessage(ChatColor.RED + "Deadman already in pvp stage, use /deadman end before you start another");
                return false;
            }else{
                Deadman.countdown(2);
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("pvp")){
            if(Deadman.stage > 3){
                sender.sendMessage(ChatColor.RED + "Deadman already in deathmatch stage, use /deadman end before you start another");
                return false;
            }else{
                Deadman.countdown(3);
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("deathmatch")){
            Deadman.countdown(4);
        }else{
            sender.sendMessage(ChatColor.RED + "Usage: /deadman <end/start/grace/pvp/deathmatch>");
            return false;
        }
        return true;
    }
}