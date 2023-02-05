package me.retrorealms.practiceserver.mechanics.pvp;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.ToggleMobsCommand;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static me.retrorealms.practiceserver.mechanics.pvp.Alignments.isSafeZone;

public class Deadman implements Listener{
    public static boolean deadman;
    public static int stage;
    public static BukkitTask task;
    public static BukkitTask task2;
    public static BukkitTask task3;

    //Stage 1, stuck in safezone
    //Stage 2, no pvp
    //Stage 3, deathban
    //Stage 4, border
    public void onEnable(){
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        stage = 0;
        deadman = false;
        WorldBorder border = Bukkit.getWorld("jew").getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000000);
        border.setDamageBuffer(10000);
    }
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){
        if (e.getDamage() <= 0.0) {
            return;
        }
        if(!deadman) return;
        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player && stage < 3){
            e.setCancelled(true);
            e.setDamage(0.0);
        }
    }

    @EventHandler
    public void onDeathban(PlayerDeathEvent e){
        if(!deadman) return;
        if(stage > 2){
            e.getEntity().kickPlayer(ChatColor.RED + "You Have Died. Thank you for participating in this Deadman Tournament");
        }
    }

    @EventHandler
    public void onLateJoin(PlayerJoinEvent e){
        if(!deadman) return;
        if(stage > 2){
            if(!e.getPlayer().isOp())e.getPlayer().kickPlayer(ChatColor.RED + "Deadman Deathmatch has already begun.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if(!deadman || stage > 1) return;
        Player p = e.getPlayer();
        p.sendMessage("222");
        if (isSafeZone(e.getFrom()) && !isSafeZone(e.getTo())) {
            p.sendMessage(ChatColor.RED + "You cannot exit deadpeaks until the event begins.");
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);
            p.teleport(TeleportBooks.stonePeaks);
        }
    }

    public static void countdown(int stage){
        String message = "";
        switch(stage){
            case 0:
                setStage(0);
                return;
            case 1:
                setStage(1);
                return;
            case 2:
                message = ChatColor.GREEN + "Deadman Grace Period will begin in ";
                break;
            case 3:
                message = ChatColor.GREEN + "PVP will be enabled in ";
                break;
            case 4:
                message = ChatColor.GREEN + "Deadman Deathmatch will begin in ";
                break;
        }
        final String message1 = message;
        task3 = Bukkit.getScheduler().runTaskTimer(PracticeServer.plugin, new Runnable() {
            int time = 60;
            @Override
            public void run() {
                if(time == 60 || time == 30 || time == 15 || time == 10 || time == 5 || time == 4 || time == 3 || time == 2 || time == 1 || time == -10){
                    Bukkit.broadcastMessage(message1 + Integer.toString(time) + " seconds!");
                }
                if(time == 0) {
                    setStage(stage);
                    task3.cancel();
                }
                time--;
            }
        }, 0, 20);
    }

    public static void setStage(int stage){
        WorldBorder border = Bukkit.getWorld("jew").getWorldBorder();
        switch(stage) {
            case 0:
                Bukkit.broadcastMessage(ChatColor.RED + "Deadman mode has been disabled");
                deadman = false;
                Deadman.stage = 0;
                border.setCenter(0, 0);
                border.setSize(1000000);
                if(task != null) task.cancel();
                if(task2 != null) task2.cancel();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Alignments.setLawful(p);
                }
                break;
            case 1:
                deadman = true;
                Deadman.stage = 1;
                Bukkit.broadcastMessage(ChatColor.GREEN + "Deadman mode has been enabled");
                break;
            case 2:
                Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Deadman Grace Period has begun, collect as much gear as you can before the deathmatch!");
                Deadman.stage = 2;
                deadman = true;
                break;
            case 3:
                ToggleMobsCommand.togglespawners = false;
                for (LivingEntity l : Bukkit.getServer().getWorld("jew").getLivingEntities()) {
                    if (!(l instanceof Player)) {
                        l.remove();
                    }
                }
                Deadman.stage = 3;
                deadman = true;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Alignments.setChaotic(p, Integer.MAX_VALUE);
                    if(p.getLocation().getZ() > 220 || p.getLocation().getX() < 350) p.teleport(TeleportBooks.generateRandomSpawnPoint(p.getName()));
                }
                Bukkit.getServer().broadcastMessage(ChatColor.RED + "PVP has been enabled, last man standing wins, do not log out!");
                break;
            case 4:
                Deadman.stage = 4;
                Bukkit.getServer().broadcastMessage(ChatColor.RED + "Deadman Deathmatch has begun, stay inside the border!");
                border.setCenter(645, -82);
                border.setSize(800);
                border.setSize(300, 100);
                task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        border.setSize(50, 100);
                    }
                }.runTaskLater(PracticeServer.plugin, 3000);
                task2 = new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (isOutsideBorder(p)) {
                                p.damage(p.getMaxHealth() / 30);
                                Alignments.tagged.put(p.getName(), System.currentTimeMillis());
                                p.sendMessage(ChatColor.RED + "Get back inside the border to stop taking damage");
                            }
                        }
                    }
                }.runTaskTimer(PracticeServer.plugin, 20, 20);
                break;
        }
    }

    public static boolean isOutsideBorder(Player p) {
        Location loc = p.getLocation();
        WorldBorder border = p.getWorld().getWorldBorder();
        double size = border.getSize()/2;
        Location center = border.getCenter();
        double x = loc.getX() - center.getX(), z = loc.getZ() - center.getZ();
        return ((x > size || (-x) > size) || (z > size || (-z) > size));
    }




}
