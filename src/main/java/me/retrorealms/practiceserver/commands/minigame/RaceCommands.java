package me.retrorealms.practiceserver.commands.minigame;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.buff.BuffHandler;

import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import me.retrorealms.practiceserver.mechanics.world.RaceMinigame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaceCommands implements CommandExecutor {

    RaceMinigame raceMinigame = PracticeServer.getRaceMinigame();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(sender instanceof Player && !sender.isOp()) return false;
        if(args.length < 1){
            sender.sendMessage(ChatColor.RED + "Usage: /racegame <end/start/grace/pvp/deathmatch>");
            return false;
        }
        if(args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("stop")){
            if(raceMinigame.getGameState() == MinigameState.NONE){
                sender.sendMessage(ChatColor.RED + "racegame not started, /racegame start to begin.");
                return false;
            }else{
                raceMinigame.cancelGame();
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("start")){
            if(raceMinigame.getGameState() != MinigameState.NONE){
                sender.sendMessage(ChatColor.RED + "racegame already started, use /racegame end before you start another");
                return false;
            }else{
                raceMinigame.startLobby();
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("psize")){
            if(args.length != 2 || raceMinigame.getGameState() != MinigameState.NONE){
                sender.sendMessage(ChatColor.RED + "to set the party max size use, /racegame partysize <1-8>");
                return false;
            }else{
                int teamSize = 1;
                try{
                    teamSize = Integer.valueOf(args[1]);
                }catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "to set the party max size use, /racegame partysize <1-8>");
                    return false;
                }
                raceMinigame.setMaxTeamSize(teamSize);
                sender.sendMessage(ChatColor.YELLOW + "You have set the max party size to " + teamSize + ".");
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("timer")){
            if(args.length != 3 || raceMinigame.getGameState() != MinigameState.NONE){
                sender.sendMessage(ChatColor.RED + "to set the timers (make sure a race isnt started), /racegame timer <LOBBY,PREP,SHRINK> <seconds>");
                return false;
            }else{
                int timer = 1;
                try{
                    timer = Integer.parseInt(args[2]);
                }catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "to set the party max size use, /racegame timer <LOBBY,PREP,SHRINK> <seconds>");
                    return false;
                }
                switch (args[1].toLowerCase()) {
                    case "lobby":
                        raceMinigame.setLobbyTime(timer);
                        break;
                    case "prep":
                        raceMinigame.setPreparationTime(timer);
                        break;
                    case "shrink":
                        raceMinigame.setShrinkTime(timer);
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "to set the party max size use, /racegame timer <LOBBY,PREP,SHRINK> <seconds>");
                        return false;
                }
                sender.sendMessage(ChatColor.YELLOW + "You have set the " + args[1] + " timer to " + timer + "seconds.");
                return true;
            }
        }
        return true;
    }

}
