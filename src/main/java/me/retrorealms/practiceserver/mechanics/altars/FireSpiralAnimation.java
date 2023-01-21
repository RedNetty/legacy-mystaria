package me.retrorealms.practiceserver.mechanics.altars;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

public class FireSpiralAnimation {
    private final Location start;
    private final World world;
    private final int particlesPerIteration;
    private final double height;
    private final BukkitScheduler scheduler;
    private final double radiusDecrement;
    private int taskId;
    private double currentRadius;
    private double angle;

    public FireSpiralAnimation(Location start, int particlesPerIteration, double height, double initialRadius, double radiusDecrement) {
        this.start = start;
        this.world = start.getWorld();
        this.particlesPerIteration = particlesPerIteration;
        this.height = height;
        this.scheduler = Bukkit.getScheduler();
        this.currentRadius = initialRadius;
        this.radiusDecrement = radiusDecrement;
    }

    public void start() {
        taskId = scheduler.scheduleSyncRepeatingTask(PracticeServer.getInstance(), new SpiralTask(), 0, 1);
    }

    public void stop() {
        scheduler.cancelTask(taskId);
    }

    private class SpiralTask implements Runnable {
        private double y = start.getY();

        @Override
        public void run() {
            if (y > start.getY() + height) {
                stop();
                ExplosionTask explosionTask = new ExplosionTask();
                scheduler.scheduleSyncDelayedTask(PracticeServer.getInstance(), explosionTask, 20);
                return;
            }
            for (int i = 0; i < particlesPerIteration; i++) {
                double x = currentRadius * Math.cos(angle) + start.getX();
                double z = currentRadius * Math.sin(angle) + start.getZ();
                Location location = new Location(world, x, y, z);
                world.spawnParticle(Particle.FLAME, location, 1);
                angle += Math.PI / (particlesPerIteration / 2);
            }
            currentRadius -= radiusDecrement;
            y += 0.05;
        }
    }

    private class ExplosionTask implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                double x = (Math.random() - 0.5) * 2;
                double y = (Math.random() - 0.5) * 2;
                double z = (Math.random() - 0.5) * 2;
                Location location = start.clone();
                location.add(x, y, z);
                world.spawnParticle(Particle.SPELL_WITCH, location, 1);
            }
        }
    }
}