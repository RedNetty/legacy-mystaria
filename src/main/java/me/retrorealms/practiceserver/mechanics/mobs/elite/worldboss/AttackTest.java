package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import me.retrorealms.practiceserver.PracticeServer;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AttackTest {


    public void iceBlastAttack(Player target) {
        Location targetLoc = target.getLocation();
        World world = target.getWorld();
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    Location blastLoc = targetLoc.clone().add(x, y, z);
                    if (blastLoc.distance(targetLoc) <= 5) {
                        world.spawnParticle(Particle.BLOCK_CRACK, blastLoc, 10, 0.5, 0.5, 0.5, 0.01, Material.PACKED_ICE);
                    }
                }
            }
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1));
        target.playSound(targetLoc, Sound.BLOCK_GLASS_BREAK, 1, 0);
    }

    public void frostBreathAttack(Location target, int radius) {
        World world = target.getWorld();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(target) <= radius) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 3, 1));
            }
        }
        world.spawnParticle(Particle.SNOW_SHOVEL, target, 100, radius, 0, radius, 0.1);
        world.playSound(target, Sound.ENTITY_ENDERDRAGON_SHOOT, 1, 0);
    }

    public void iceBurstAttack(Location target, int radius) {
        World world = target.getWorld();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location burstBlock = target.clone().add(x, y, z);
                    if (burstBlock.distance(target) <= radius) {
                        Block block = burstBlock.getBlock();
                        if (block.getType() == Material.WATER) {
                            block.setType(Material.ICE);
                        }
                    }
                }
            }
        }
        world.playSound(target, Sound.BLOCK_GLASS_BREAK, 1, 0);
        new BukkitRunnable() {
            public void run() {
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Location burstBlock = target.clone().add(x, y, z);
                            if (burstBlock.distance(target) <= radius) {
                                Block block = burstBlock.getBlock();
                                if (block.getType() == Material.ICE) {
                                    block.setType(Material.WATER);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskLater(PracticeServer.getInstance(), 20 * 20); // delay in ticks
    }


    public void icyGroundAttack(Location bossLoc, int radius, int damage, int numberOfSpots, int duration, int spotSize) {
        World world = bossLoc.getWorld();
        int[] ticks = {0};
        for (int i = 0; i < numberOfSpots; i++) {
            double angle = 2 * Math.PI * i / numberOfSpots;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location spotLocation = bossLoc.clone().add(x, 0, z);
            spotLocation.setY(world.getHighestBlockYAt(spotLocation));
            for (int dx = -spotSize; dx <= spotSize; dx++) {
                for (int dz = -spotSize; dz <= spotSize; dz++) {
                    Location particleLocation = spotLocation.clone().add(dx, 0, dz);
                    world.playSound(particleLocation, Sound.BLOCK_SNOW_BREAK, 1, 1);
                    world.spawnParticle(Particle.SNOW_SHOVEL, particleLocation, 8, 0.5, 0.8, 0.5, 0.1);
                }
            }
                new BukkitRunnable() {

                public void run() {
                    if (ticks[0] >= duration) {
                        Bukkit.getScheduler().cancelTask(this.getTaskId());
                        return;
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getLocation().distance(spotLocation) <= spotSize) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 20, 1));
                            player.damage(damage);
                        }
                    }
                    ticks[0]++;
                }
            }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), new Runnable() {
            public void run() {
                for (int i = 0; i < numberOfSpots; i++) {
                    double angle = 2 * Math.PI * i / numberOfSpots;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location spotLocation = bossLoc.clone().add(x, 0, z);
                    spotLocation.setY(world.getHighestBlockYAt(spotLocation));
                    for (int dx = -spotSize; dx <= spotSize; dx++) {
                        for (int dz = -spotSize; dz <= spotSize; dz++) {
                            if(ticks[0] >= duration) return;
                            Location particleLocation = spotLocation.clone().add(dx, 0, dz);
                            world.playSound(particleLocation, Sound.BLOCK_SNOW_BREAK, 1, 1);
                            world.spawnParticle(Particle.SNOW_SHOVEL, particleLocation, 8, 0.5, 0.8, 0.5, 0.1);
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    public void iceBlockBarrageAttack(Entity entity, int radius, int damage, int numberOfIceBlocks, int duration) {
        World world = entity.getLocation().getWorld();
        for (int i = 0; i < numberOfIceBlocks; i++) {
            final int[] currentRadius = {3};
            final int[] currentDuration = {0};
            final double[] angle = {2 * Math.PI * i / numberOfIceBlocks};
            double x = Math.cos(angle[0]) * currentRadius[0];
            double z = Math.sin(angle[0]) * currentRadius[0];
            Location iceBlockLocation = entity.getLocation().clone().add(x, 0, z);
            ArmorStand iceBlock = (ArmorStand) world.spawnEntity(iceBlockLocation, EntityType.ARMOR_STAND);
            iceBlock.setHelmet(new ItemStack(Material.ICE));
            iceBlock.setVisible(false);
            iceBlock.setCustomName("iceblock");
            iceBlock.setGravity(false);
            new BukkitRunnable() {
                public void run() {
                    if (currentDuration[0] == duration) {
                        iceBlock.remove();
                    } else {
                        currentDuration[0]++;
                    }

                }
            }.runTaskTimer(PracticeServer.getInstance(), 20L, 10L);
            new BukkitRunnable() {
                public void run() {
                    if (iceBlock.isValid()) {
                        if (currentRadius[0] <= radius) {
                            currentRadius[0]++;
                        }
                        angle[0] += 0.1;
                        double x = Math.cos(angle[0]) * currentRadius[0];
                        double z = Math.sin(angle[0]) * currentRadius[0];
                        Location newIceBlockLocation = entity.getLocation().clone().add(x, 0, z);
                        iceBlock.teleport(newIceBlockLocation);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (iceBlock.getLocation().distance(player.getLocation()) <= 1) {
                                player.damage(damage);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 3));
                                player.playEffect(player.getLocation(), Effect.WITHER_SHOOT, 1);
                                iceBlock.remove();
                            }
                        }
                    }
                }
            }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
        }
    }

    public void iceSpikeAttack(Location target, int amount) {
        World world = target.getWorld();
        for (int i = 0; i < amount; i++) {
            double angle = Math.toRadians(ThreadLocalRandom.current().nextInt(0, 360));
            double x = 20 * Math.cos(angle);
            double z = 20 * Math.sin(angle);
            Location spikeLoc = new Location(world, target.getX() + x, target.getY() + 20, target.getZ() + z);
            Vector direction = spikeLoc.toVector().subtract(target.toVector()).normalize();
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK, true, (float) spikeLoc.getX(), (float) spikeLoc.getY(), (float) spikeLoc.getZ(), (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), 1, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
            new BukkitRunnable() {
                public void run() {
                    if (spikeLoc.getBlock().getType() == Material.AIR) {
                        spikeLoc.getBlock().setType(Material.PACKED_ICE);
                    }
                }
            }.runTaskLater(PracticeServer.getInstance(), 20L * 3); // delay in ticks
        }
    }
}
