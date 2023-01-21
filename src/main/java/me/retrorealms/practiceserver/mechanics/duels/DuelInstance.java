package me.retrorealms.practiceserver.mechanics.duels;

import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.teleport.Hearthstone;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Red
 * January 2018
 */
public class DuelInstance {


    public List<Player> team1;
    public List<Player> team2;
    private ArrayList<Player> team1Remaining;
    private ArrayList<Player> team2Remaining;
    private Integer arena;
    private Player team1Leader;
    private Player team2Leader;
    private boolean party;

    public DuelInstance(Integer arena, List<Player> team1, List<Player> team2, Player team1Leader, Player team2Leader, boolean party){
        this.arena = arena;
        this.team1 = team1;
        this.team1Remaining = new ArrayList();
        team1Remaining.addAll(team1);
        this.team2 = team2;
        this.team2Remaining = new ArrayList();
        team2Remaining.addAll(team2);
        this.team1Leader = team1Leader;
        this.team2Leader = team2Leader;
        this.party = party;
    }

    public void start(){
        for(Player p : team1){
            Duels.duelers.put(p, new DuelPlayer(p, arena, 1));
            p.sendMessage(ChatColor.GREEN + "The Duel has begun, FIGHT!");
            p.teleport(Duels.team1Spots.get(arena));
            p.teleport(Duels.team1Spots.get(arena));
            p.teleport(Duels.team1Spots.get(arena));
            p.setHealth(p.getMaxHealth());
            TeleportBooks.casting_loc.remove(p.getName());
            TeleportBooks.casting_time.remove(p.getName());
            TeleportBooks.teleporting_loc.remove(p.getName());
            Hearthstone.casting.remove(p.getName());
            Hearthstone.castingloc.remove(p.getName());
            Horses.mounting.remove(p.getName());
        }for(Player p : team2){
            Duels.duelers.put(p, new DuelPlayer(p, arena, 2));
            p.sendMessage(ChatColor.GREEN + "The Duel has begun, FIGHT!");
            p.teleport(Duels.team2Spots.get(arena));
            p.teleport(Duels.team2Spots.get(arena));
            p.teleport(Duels.team2Spots.get(arena));
            p.setHealth(p.getMaxHealth());
            TeleportBooks.casting_loc.remove(p.getName());
            TeleportBooks.casting_time.remove(p.getName());
            TeleportBooks.teleporting_loc.remove(p.getName());
            Hearthstone.casting.remove(p.getName());
            Hearthstone.castingloc.remove(p.getName());
            Horses.mounting.remove(p.getName());
        }
    }

    public void checkRemaining(){
        if(team1Remaining.isEmpty() || team2Remaining.isEmpty()) end();
    }

    public void removePlayerFromDuel(Player p){
        if(team1Remaining.contains(p)) team1Remaining.remove(p);
        if(team2Remaining.contains(p)) team2Remaining.remove(p);
    }

    public void end() {
        List<Player> winningTeam;
        List<Player> losingTeam;
        Player winningLeader;
        Player losingLeader;
        if (team1Remaining.isEmpty()) {
            winningTeam = team2;
            winningLeader = team2Leader;
            losingTeam = team1;
            losingLeader = team1Leader;
        } else {
            winningTeam = team1;
            winningLeader = team1Leader;
            losingTeam = team2;
            losingLeader = team2Leader;
        }
        Collection<Player> recipients = new ArrayList<>();
        if (party) {
            recipients.addAll(Bukkit.getServer().getOnlinePlayers());
        }else{
            recipients.addAll(team1);
            recipients.addAll(team2);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GREEN + winningLeader.getName());
        for (Player p : winningTeam) {
            if (p != winningLeader) {
                sb.append(", " + p.getName());
            }
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(ChatColor.RED + losingLeader.getName());
        for (Player p2 : losingTeam) {
            if (p2 != losingLeader) {
                sb2.append(", " + p2.getName());
            }
        }
        for (Player all : Bukkit.getServer().getOnlinePlayers()) {
            StringUtil.sendCenteredMessage(all, sb.toString());
            StringUtil.sendCenteredMessage(all, ChatColor.GOLD + "DEFEATED");
            StringUtil.sendCenteredMessage(all, sb2.toString());
        }
        for(Player p : team2Remaining){
            if(!team2Remaining.isEmpty()) Duels.duelers.get(p).exitDuel(true, false);
        }
        for(Player p : team1Remaining){
            if(!team1Remaining.isEmpty()) Duels.duelers.get(p).exitDuel(true, false);
        }
        Duels.available.put(arena, true);
        Duels.duels.remove(arena);
    }

    public void timeout(){
        for(Player p : team2Remaining){
            Duels.duelers.get(p).exitDuel(true, false);
            p.sendMessage(ChatColor.RED + "This duel has run out of time, and ended.");
        }
        for(Player p : team1Remaining){
            Duels.duelers.get(p).exitDuel(true, false);
            p.sendMessage(ChatColor.RED + "This duel has run out of time, and ended.");
        }
        Duels.available.put(arena, true);
        Duels.duels.remove(arena);
    }
}
