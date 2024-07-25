package me.retrorealms.practiceserver.commands.minigame;

import me.retrorealms.practiceserver.PracticeServer;

import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import me.retrorealms.practiceserver.mechanics.world.races.RaceMinigame;
import me.retrorealms.practiceserver.mechanics.world.races.RaceSettingsMenu;
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
            assert sender instanceof Player;
            Player player = (Player)sender;
            RaceSettingsMenu.openMenu(player);
            return true;
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
        if (args[0].equalsIgnoreCase("incursion")) {
            if (args.length < 2) {

                sender.sendMessage(ChatColor.RED + "Usage: /racegame incursion <on/off> [Multiplier]");
                return false;
            }
            if(args[1].equalsIgnoreCase("rate")) {
                double spawnRate = 0.15;
                try {
                    spawnRate = Double.parseDouble(args[2]);
                    raceMinigame.setIncursionSpawnRate(spawnRate);
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Rate. Using default value of 15.");
                }
                return false;
            }

            boolean enabled = args[1].equalsIgnoreCase("on");
            double healthMultiplier = 15; // Default value

            if (args.length > 2) {
                try {
                    healthMultiplier = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid multiplier. Using default value of 15.");
                }
            }

            raceMinigame.toggleIncursionMode(enabled, healthMultiplier);
            return true;
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
