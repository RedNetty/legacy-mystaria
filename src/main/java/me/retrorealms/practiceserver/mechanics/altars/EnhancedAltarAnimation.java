package me.retrorealms.practiceserver.mechanics.altars;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.List;

public class EnhancedAltarAnimation {
    private final Location center;
    private final double radius;
    private final int particleCount;
    private final double rotationSpeed;
    private final double ascendSpeed;
    private int taskId;
    private final List<Particle> particles = Arrays.asList(
            Particle.SPELL_WITCH, Particle.ENCHANTMENT_TABLE, Particle.END_ROD, Particle.DRAGON_BREATH
    );

    public EnhancedAltarAnimation(Location center, double radius, int particleCount, double rotationSpeed, double ascendSpeed) {
        this.center = center.clone();
        this.radius = radius;
        this.particleCount = particleCount;
        this.rotationSpeed = rotationSpeed;
        this.ascendSpeed = ascendSpeed;
    }

    public void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.plugin, new Runnable() {
            double angle = 0;
            double height = 0;
            @Override
            public void run() {
                if (height > 3) {
                    Bukkit.getScheduler().cancelTask(taskId);
                    return;
                }

                for (int i = 0; i < particleCount; i++) {
                    double x = center.getX() + radius * Math.cos(angle + (2 * Math.PI * i / particleCount));
                    double z = center.getZ() + radius * Math.sin(angle + (2 * Math.PI * i / particleCount));
                    Location particleLocation = new Location(center.getWorld(), x, center.getY() + height, z);

                    center.getWorld().spawnParticle(Particle.SPELL_WITCH, particleLocation, 1, 0, 0, 0, 0);
                    center.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLocation, 1, 0, 0, 0, 0);
                }

                angle += rotationSpeed;
                height += ascendSpeed;
            }
        }, 0L, 1L);
    }


    private void createSpiral(double time, double height) {
        for (int i = 0; i < particleCount; i++) {
            double angle = time + (2 * Math.PI * i / particleCount);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(center.getWorld(), x, center.getY() + height, z);
            center.getWorld().spawnParticle(particles.get(0), particleLocation, 1, 0, 0, 0, 0);
        }
    }

    private void createPillar(double time, double height) {
        for (int i = 0; i < 5; i++) {
            double y = Math.sin(time * 3 + i) * 0.2 + height;
            Location loc = center.clone().add(0, y, 0);
            center.getWorld().spawnParticle(particles.get(1), loc, 3, 0.1, 0.1, 0.1, 0);
        }
    }

    private void createOrbitingParticles(double time, double height) {
        for (int i = 0; i < 3; i++) {
            double angle = time * 2 + (2 * Math.PI * i / 3);
            double x = Math.cos(angle) * radius * 0.8;
            double z = Math.sin(angle) * radius * 0.8;
            double y = Math.sin(time * 4 + i) * 0.2 + height;
            Location loc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(particles.get(2), loc, 1, 0, 0, 0, 0);
        }
    }

    private void createGroundEffect(double time) {
        for (int i = 0; i < 8; i++) {
            double angle = time + (Math.PI * i / 4);
            double x = Math.cos(angle) * (radius * 0.5 + Math.sin(time * 2) * 0.2);
            double z = Math.sin(angle) * (radius * 0.5 + Math.sin(time * 2) * 0.2);
            Location loc = center.clone().add(x, 0.1, z);
            center.getWorld().spawnParticle(particles.get(3), loc, 1, 0, 0, 0, 0);
        }
    }
}