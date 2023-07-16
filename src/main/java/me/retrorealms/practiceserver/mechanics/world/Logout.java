/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package me.retrorealms.practiceserver.mechanics.world;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import me.retrorealms.practiceserver.utils.ServerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;


public class Logout implements Listener {
    public static ConcurrentHashMap<String, Integer> logging = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Location> loggingloc = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> syncing = new ConcurrentHashMap<>();

    public void onEnable() {
        PracticeServer.log.info("[Logout] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        new BukkitRunnable() {

            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    try {
                        if (!logging.containsKey(p.getName())) continue;
                        if (logging.get(p.getName()) == 0) {
                            logging.remove(p.getName());
                            loggingloc.remove(p.getName());

                            Alignments.tagged.remove(p.getName());
                            if (Listeners.combat.containsKey(p.getName())) {
                                Listeners.combat.remove(p.getName());
                                Alignments.tagged.remove(p.getName());
                            }
                            p.saveData();
                            p.kickPlayer(ChatColor.GREEN.toString() + "You have safely logged out." + "\n\n" + ChatColor.GRAY + "Your player data has been synced.");
                            ServerUtil.sendToServer(p.getName(), "lobby");
                            continue;
                        }
                        p.sendMessage(ChatColor.RED + "Logging out in ... " + ChatColor.BOLD + logging.get(p.getName()) + "s");
                        logging.put(p.getName(), logging.get(p.getName()) - 1);
                    } catch (Exception e) {

                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 10, 10);
    }

    public void onDisable(boolean patch) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SQLMain.updatePersistentStats(player);
            SQLMain.updatePlayerStats(player);
            player.kickPlayer(ChatColor.GREEN.toString() + "You have safely logged out." + "\n\n" + ChatColor.GRAY + "Your player data has been synced.");
        });
        PracticeServer.log.info("[Logout] has been disabled.");
    }


    @EventHandler
    public void onCancelDamager(EntityDamageByEntityEvent e) {
        Player p;
        if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && logging.containsKey((p = (Player) e.getDamager()).getName())) {
            logging.remove(p.getName());
            loggingloc.remove(p.getName());
            p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Logout - CANCELLED");
        }
    }

    @EventHandler
    public void onCancelDamage(EntityDamageEvent e) {
        Player p;
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getEntity() instanceof Player && logging.containsKey((p = (Player) e.getEntity()).getName())) {
            logging.remove(p.getName());
            loggingloc.remove(p.getName());
            p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Logout - CANCELLED");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        p.saveData();
        if (logging.containsKey(p.getName())) {
            logging.remove(p.getName());
            loggingloc.remove(p.getName());
        }
    }

    @EventHandler
    public void onCancelMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (logging.containsKey(p.getName()) && (loggingloc.get(p.getName())).distanceSquared(e.getTo()) >= 2.0) {
            logging.remove(p.getName());
            p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Logout - CANCELLED");
        }
    }

}

