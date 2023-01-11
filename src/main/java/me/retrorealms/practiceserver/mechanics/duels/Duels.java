package me.retrorealms.practiceserver.mechanics.duels;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Red
 * January 2018
 */
public class Duels implements Listener {

    static HashMap<Integer, Boolean> available = new HashMap();
    public static HashMap<Integer, Location> team1Spots = new HashMap();
    public static HashMap<Integer, Location> team2Spots = new HashMap();
    public static ConcurrentHashMap<Player, DuelPlayer> duelers = new ConcurrentHashMap<>();
    public static HashMap<Integer, DuelInstance> duels = new HashMap<>();
    public static HashMap<Player, Player> hasRequest = new HashMap();
    public static HashMap<Integer, Integer> arenaTimeout = new HashMap();
    public static HashMap<Player, Integer> stayLawful = new HashMap();
    public static BukkitTask task = null;

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        team1Spots.put(1, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -986, 180, 0));
        team2Spots.put(1, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1030, 0, 0));
        available.put(1, true);
        team1Spots.put(2, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1178, 180, 0));
        team2Spots.put(2, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1222, 0, 0));
        available.put(2, true);
        team1Spots.put(3, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1386, 180, 0));
        team2Spots.put(3, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1430, 0, 0));
        available.put(3, true);
        team1Spots.put(4, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1578, 180, 0));
        team2Spots.put(4, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1622, 0, 0));
        available.put(4, true);
        team1Spots.put(5, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1786, 180, 0));
        team2Spots.put(5, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1830, 0, 0));
        available.put(5, true);
        team1Spots.put(6, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -1978, 180, 0));
        team2Spots.put(6, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2022, 0, 0));
        available.put(6, true);
        team1Spots.put(7, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2186, 180, 0));
        team2Spots.put(7, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2230, 0, 0));
        available.put(7, true);
        team1Spots.put(8, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2378, 180, 0));
        team2Spots.put(8, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2422, 0, 0));
        available.put(8, true);
        team1Spots.put(9, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2586, 180, 0));
        team2Spots.put(9, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2630, 0, 0));
        available.put(9, true);
        team1Spots.put(10, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2778, 180, 0));
        team2Spots.put(10, new Location(Bukkit.getServer().getWorld("jew"), -273, 69, -2822, 0, 0));
        available.put(10, true);


        new AsyncTask(() -> {
            for (Integer i : arenaTimeout.keySet()) {
                if (arenaTimeout.get(i) < 1) {
                    arenaTimeout.remove(i);
                    duels.get(i).timeout();
                } else {
                    arenaTimeout.put(i, arenaTimeout.get(i) - 1);
                }
            }
        }).setUseSharedPool(true).setInterval(1).scheduleRepeatingTask();
    }

    public void onDisable(){
        for(DuelInstance duel : duels.values()){
            duel.timeout();
        }
    }

    public static void createDuel(ArrayList<Player> team1, ArrayList<Player> team2, Player team1Leader, Player team2Leader, boolean party) {
        Random r = new Random();
        for (int i = r.nextInt(2)+ 1; i < 11; i++) {
            if (available.get(i) == true) {
                final int arena = i;
                if (!checks(team1, team2, team1Leader, team2Leader)) return;
                available.put(arena, false);
                task = Bukkit.getScheduler().runTaskTimer(PracticeServer.plugin, new Runnable() {
                    int n = 10;
                    @Override
                    public void run() {
                        if(n > 0) {
                            for (Player p : team1) {
                                p.sendMessage(ChatColor.GREEN + "Teleporting to arena in " + Integer.toString(n) + " seconds.");
                            }for (Player p : team2) {
                                p.sendMessage(ChatColor.GREEN + "Teleporting to arena in " + Integer.toString(n) + " seconds.");
                            }
                            n--;
                        } else {
                            task.cancel();
                        }
                    }
                }, 0, 20);
                new AsyncTask(() -> {
                    for (Player p : team1) {
                        p.teleport(p.getLocation());
                        Horses.mounting.remove(p.getName());
                    }
                    for (Player p : team2) {
                        p.teleport(p.getLocation());
                        Horses.mounting.remove(p.getName());
                    }
                }).setDelay(9).scheduleDelayedTask();
                new AsyncTask(() -> {
                    if (!checks(team1, team2, team1Leader, team2Leader)) {
                        available.put(arena, true);
                        return;
                    }
                    DuelInstance duel = new DuelInstance(arena, team1, team2, team1Leader, team2Leader, party);
                    duel.start();
                    duels.put(arena, duel);
                    arenaTimeout.put(arena, 600);
                }).setDelay(10).scheduleDelayedTask();
                return;
            }
        }
        team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: There are no arenas available, please try again in a bit.");
        team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: There are no arenas available, please try again in a bit.");
        return;
    }


    public static boolean checks(ArrayList<Player> team1, ArrayList<Player> team2, Player team1Leader, Player team2Leader) {
        for (Player p : team1) {
            if (!Bukkit.getServer().getOnlinePlayers().contains(p)) {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are offline.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are offline.");
                return false;
            }
            if (Listeners.isInCombat(p)) {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are in combat.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are in combat.");
                return false;
            }
            if (Alignments.get(p) != "&aLAWFUL") {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are not lawful.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are not lawful.");
                return false;
            }
            if (duelers.containsKey(p)) {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are in a duel.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are in a duel.");
                return false;
            }

        }
        for (Player p : team2) {
            if (!Bukkit.getServer().getOnlinePlayers().contains(p)) {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are offline.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are offline.");
                return false;
            }
            if (Listeners.isInCombat(p)) {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are in combat.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are in combat.");
                return false;
            }
            if (Alignments.get(p) != "&aLAWFUL") {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are not lawful.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are not lawful.");
                return false;
            }
            if (duelers.containsKey(p)) {
                team1Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in the other party are in a duel.");
                team2Leader.sendMessage(ChatColor.RED + "Duel Cancelled: One or more players in your party are in a duel.");
                return false;
            }
        }
        return true;
    }

    public static Integer rankPlayers(Player p) {
        RankEnum rank = ModerationMechanics.getRank(p);
        switch (rank) {
            case DEFAULT:
                return 1;
            case SUB:
                return 2;
            case SUB1:
                return 4;
            case SUB2:
                return 6;
        }
        return 8;
    }

    @EventHandler
    void onDeath(PlayerDeathEvent e) {
        if (e.getEntity() != null && duelers.containsKey(e.getEntity())) e.getDrops().clear();
    }

    @EventHandler
    void preventInterferance(EntityDamageByEntityEvent e) {
        if(e.getDamage() <= 0) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if((duelers.containsKey(p) && !duelers.containsKey(e.getDamager())) || (!duelers.containsKey(p) && duelers.containsKey(e.getDamager()))) {
            e.setCancelled(true);
            e.setDamage(0.0);
        }
    }

    @EventHandler
    void preventDeath(EntityDamageEvent e) {
        if (e.getDamage() <= 0) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (duelers.containsKey(p) && (p.getHealth() - e.getDamage() < 1)) {
            e.setCancelled(true);
            e.setDamage(0.0);
            duelers.get(p).exitDuel(false, false);
        }
    }

    @EventHandler
    void preventFriendlyFire(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player d = (Player) e.getDamager();
        Player p = (Player) e.getEntity();
        if (Duels.duelers.containsKey(p) && Duels.duelers.containsKey(d)) {
            if (Duels.duelers.get(p).team == Duels.duelers.get(d).team) {
                e.setDamage(0.0);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onDisconnect(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (Duels.duelers.containsKey(p)) {
            duelers.get(p).exitDuel(false, true);
        }
    }

    @EventHandler
    void onKicked(PlayerKickEvent e) {
        Player p = e.getPlayer();
        if (Duels.duelers.containsKey(p)) {
            duelers.get(p).exitDuel(false, true);
        }
    }

    @EventHandler
    void onDrop(PlayerDropItemEvent e) {
        if (duelers.containsKey(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}