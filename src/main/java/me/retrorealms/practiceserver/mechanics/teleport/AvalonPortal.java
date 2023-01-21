package me.retrorealms.practiceserver.mechanics.teleport;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AvalonPortal implements Listener {


    public static List<Player> glidingPlayers = new ArrayList<>();

    //Location the Portal Leads to
    public Location portalEndLoc = new Location(Bukkit.getWorld("jew"), 601.125, 44.817, 405.947);

    //Checks if a given location is the world Guard portal tp
    public static boolean isPortalZone(Location loc) {
        AtomicBoolean isZone = new AtomicBoolean(false);
        ApplicableRegionSet locset = WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
        locset.getRegions().stream().forEach(protectedRegion -> {
            if (protectedRegion.getId().equalsIgnoreCase("tp")) {
                isZone.set(true);
            }
        });
        return isZone.get();
    }

    public void onLoad() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player glidingPlayer : glidingPlayers) {
                    Location playerLoc = glidingPlayer.getLocation().clone();
                    for (int i = 0; i < 360; i++) {
                        double angle = i * Math.PI / 180;
                        Vector v = new Vector(Math.cos(angle), 0, Math.sin(angle));
                        playerLoc.add(v);
                        glidingPlayer.spawnParticle(Particle.DRAGON_BREATH, playerLoc, 1);
                        playerLoc.subtract(v);
                    }
                    if(!glidingPlayer.isGliding()) glidingPlayer.setGliding(true);
                    if (glidingPlayer.isOnGround()) {
                        stopGliding(glidingPlayer);
                    }
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);
    }

    @EventHandler
    public void onPortal(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isPortalZone(player.getLocation())) {
            teleportPlayer(player);
            event.setCancelled(true);
        }
    }

    public void stopGliding(Player player) {
        if (glidingPlayers.contains(player)) {
            player.setGliding(false);
            new BukkitRunnable() {
                public void run() {
                    glidingPlayers.remove(player);
                }
            }.runTaskLater(PracticeServer.getInstance(), 20L);
        }
    }

    @EventHandler
    public void onStopGliding(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (glidingPlayers.contains(event.getEntity())) {
            Player player = (Player) event.getEntity();
            if (event.getEntity().isOnGround()) {
                stopGliding(player);
            } else {
                if (!player.isGliding()) player.setGliding(true);
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            System.out.println(glidingPlayers.contains(p));

            if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION || e.getCause() == EntityDamageEvent.DamageCause.CONTACT || e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK || e.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL || e.getCause() == EntityDamageEvent.DamageCause.CUSTOM || e.getCause() == EntityDamageEvent.DamageCause.DROWNING || e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                if (p.isGliding() || glidingPlayers.contains(p)) {
                    e.setDamage(0.0);
                    stopGliding(p);
                    e.setCancelled(true);
                }
            }
        }
    }

    public void teleportPlayer(Player player) {
        if (!playerInPortal(player)) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 1));
        player.getWorld().strikeLightningEffect(player.getLocation());// Strikes Portal at Entrance
        player.teleport(portalEndLoc);
        player.getWorld().strikeLightningEffect(player.getLocation());// Strikes At Exit Portal
        glidingPlayers.add(player);
        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 20, 1);
        player.setGliding(true);
    }

    //If player is in the Portal "Zone" using Worldguard it will return true or false.
    public boolean playerInPortal(Player player) {
        return isPortalZone(player.getLocation());
    }
}
