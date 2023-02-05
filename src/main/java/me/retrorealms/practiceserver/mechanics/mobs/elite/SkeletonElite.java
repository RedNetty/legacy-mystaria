package me.retrorealms.practiceserver.mechanics.mobs.elite;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.SkullTextures;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Created by Giovanni on 15-5-2017.
 */
public class SkeletonElite implements Listener {

    @Getter
    private static final Location[] maltaiStrike = new Location[]{

            new Location(Bukkit.getWorld("jew"), -222, 18, -467),
            new Location(Bukkit.getWorld("jew"), -222, 18, -492)
    };

    public void init() {
        /*
        // Daemon Lord Wither
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), () -> {
            Spawners.getMobs().keySet().forEach(livingEntity -> {

                if (!Mobs.isType(livingEntity, "kilatanB")) return;

                if (livingEntity.isDead()) return;

                if (!livingEntity.hasMetadata("vanished"))
                    livingEntity.setMetadata("vanished", new FixedMetadataValue(PracticeServer.getInstance(), "false"));

                double currentHP = livingEntity.getHealth();
                double maxHP = livingEntity.getMaxHealth();

                if (currentHP <= maxHP / 4) {

                    if (!livingEntity.getMetadata("vanished").get(0).asString().equals("true")) {
                        livingEntity.setMetadata("vanished", new FixedMetadataValue(PracticeServer.getInstance(), "true"));

                        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
                            EntityDaemonLord daemonLord = new EntityDaemonLord(livingEntity.getWorld());
                            daemonLord.spawn(livingEntity.getLocation());

                            IntStream.range(0, 6).forEach(action -> {
                                livingEntity.getWorld().strikeLightning(livingEntity.getLocation());
                                livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
                            });
                            livingEntity.remove();

                            List<Player> currentNear = getNearbyPlayers(livingEntity);

                            if (currentNear.isEmpty()) return; // ???

                            currentNear.forEach(player -> {
                                player.sendMessage("");
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4The Infernal Abyss: My daemon lord, bring me their souls.."));
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&oThe daemon lord's drop-rates have been improved by &l35%"));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 10F, 0.1F);
                            });
                        }, 20);
                    }
                }
            });
        }, 0L, 20);
        */

        // Skeleton Elite self
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), () -> {
            Spawners.getMobs().keySet().forEach(livingEntity -> {

                if (Mobs.isSkeletonMinion(livingEntity)) {
                    if (livingEntity.hasMetadata("skeletonWrath") && livingEntity.getMetadata("skeletonWrath").get(0).asString().equals("true")) {
                        Random r = new Random();
                        float y = r.nextFloat() - 0.2F;
                        float x = r.nextFloat() - 0.2F;
                        float z = r.nextFloat() - 0.2F;

                        Particles.SPELL_WITCH.display(0.3f, 0.3f, 0.3f, 0.02f, 60, livingEntity.getLocation().clone().add(x, y, z), 20.0);
                    }
                    return;
                }

                if (!Mobs.isSkeletonElite(livingEntity)) return;

                if (livingEntity.isDead()) return;

                if (!livingEntity.hasMetadata("wrathSkeleton"))
                    livingEntity.setMetadata("wrathSkeleton", new FixedMetadataValue(PracticeServer.getInstance(), "false"));

                if (!livingEntity.hasMetadata("invisWrathSkeleton"))
                    livingEntity.setMetadata("invisWrathSkeleton", new FixedMetadataValue(PracticeServer.getInstance(), "false"));

                if (!livingEntity.hasMetadata("minionWrathSkeleton"))
                    livingEntity.setMetadata("minionWrathSkeleton", new FixedMetadataValue(PracticeServer.getInstance(), "false"));

                if (!livingEntity.hasMetadata("powersOfMaltai"))
                    livingEntity.setMetadata("powersOfMaltai", new FixedMetadataValue(PracticeServer.getInstance(), "false"));

                double currentHP = livingEntity.getHealth();
                double maxHP = livingEntity.getMaxHealth();

                if (livingEntity.getMetadata("invisWrathSkeleton").get(0).asString().equals("true")) {
                    Random r = new Random();
                    float y = r.nextFloat() - 0.2F;
                    float x = r.nextFloat() - 0.2F;
                    float z = r.nextFloat() - 0.2F;

                    Particles.FLAME.display(0.3f, 0.3f, 0.3f, 0.02f, 60, livingEntity.getLocation().clone().add(x, y, z), 20.0);
                }

                if (currentHP <= maxHP / 2) {

                    if (!livingEntity.getMetadata("wrathSkeleton").get(0).asString().equals("true")) {

                        List<Player> currentNear = getNearbyPlayers(livingEntity);

                        if (currentNear.isEmpty()) return; // ???

                        livingEntity.setMetadata("wrathSkeleton", new FixedMetadataValue(PracticeServer.getInstance(), "true"));
                        wrath(currentNear);
                    }
                }

                if (currentHP <= maxHP / 2.5) {

                    if (!livingEntity.getMetadata("minionWrathSkeleton").get(0).asString().equals("true")) {

                        List<Player> currentNear = getNearbyPlayers(livingEntity);

                        if (currentNear.isEmpty()) return;

                        currentNear.forEach(player -> {
                            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 10F, 0.1F);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "        &c&l*** &c&nDEATHLORD SUMMONED MINIONS&c&l ***"));
                        });

                        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
                            IntStream.range(0, ThreadLocalRandom.current().nextInt(0, 3)).forEach(intConsumer -> {
                                Location location = livingEntity.getLocation();

                                double addX = ThreadLocalRandom.current().nextDouble(1, 3);
                                double addZ = ThreadLocalRandom.current().nextDouble(1, 3);

                                Location location1 = new Location(location.getWorld(), location.getX() + addX, location.getY(), location.getZ() + addZ);

                                LivingEntity livingEntity1 = Spawners.getInstance().spawnMob(location1, "weakSkeletonEntity_UV", 5, true);

                                livingEntity1.setMetadata("skeletonWrath", new FixedMetadataValue(PracticeServer.getInstance(), "true"));
                            });
                        }, 20);

                        livingEntity.setMetadata("minionWrathSkeleton", new FixedMetadataValue(PracticeServer.getInstance(), "true"));

                    }
                }

                if (currentHP <= maxHP / 3) {

                    if (!livingEntity.getMetadata("invisWrathSkeleton").get(0).asString().equals("true")) {

                        List<Player> currentNear = getNearbyPlayers(livingEntity);

                        if (currentNear.isEmpty()) return;

                        livingEntity.setMetadata("invisWrathSkeleton", new FixedMetadataValue(PracticeServer.getInstance(), "true"));

                        invis(currentNear, livingEntity, 3);
                    }
                }
            });
        }, 0L, 20);
    }

    private SkeletonElite invis(List<Player> playerList, LivingEntity livingEntity, int doAfter) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            // Temp. heals, strikes wrath.

            playerList.forEach(player -> {
                player.sendMessage(ChatColor.RED + "Restless Skeleton Deathlord: Let the powers of the Abyss flow through me!");

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 10F, 0.1F);

                player.getWorld().strikeLightningEffect(player.getLocation());
            });

            double beforeHP = livingEntity.getHealth();

            livingEntity.setHealth(livingEntity.getMaxHealth());

            // Goes invis
            Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {

                // Reset HP and heal
                livingEntity.setHealth(105000);

                IntStream.range(0, 20).forEach(intConsumer -> {
                    Location location = new Location(livingEntity.getWorld(), livingEntity.getLocation().getX(), livingEntity.getLocation().getY() + 1.35, livingEntity.getLocation().getZ());
                    Entity entity = livingEntity.getWorld().spawnEntity(location, EntityType.BAT);
                    new CreatureSpawnEvent((LivingEntity)entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                });

                playerList.forEach(player -> {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.DARK_RED + "The Infernal Abyss: The deathlord..");

                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_AMBIENT, 10F, 0.1F);
                });

                // Change name
                livingEntity.setCustomName(ChatColor.DARK_RED + "The Infernal Restless Skeleton Deathlord");

                livingEntity.getEquipment().setHelmet(SkullTextures.DEMON.getSkullByURL());
                livingEntity.getEquipment().setLeggings(null);
                livingEntity.getEquipment().setBoots(null);

                livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());

                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE));

                livingEntity.setMetadata("name",
                        new FixedMetadataValue(PracticeServer.plugin, livingEntity.getCustomName()));


                Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {

                    playerList.forEach(player -> {

                        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 10F, 1.7F);
                    });
                }, 20);
            }, 20 * 2);
        }, 20 * doAfter);

        return this;
    }

    private SkeletonElite wrath(List<Player> playerList) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            playerList.forEach(player -> {
                player.sendMessage(ChatColor.RED + "Restless Skeleton Deathlord: I'm unstoppable, feel my wrath!");

                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 10F, 0.1F);

                player.getWorld().strikeLightning(player.getLocation());
                player.getWorld().strikeLightningEffect(player.getLocation());
            });
        });

        return this;
    }

    public static List<Player> getNearbyPlayers(LivingEntity livingEntity) {
        List<Player> players = Lists.newArrayList();

        livingEntity.getNearbyEntities(10, 10, 10).forEach(entity -> {
            if (entity instanceof Player) players.add((Player) entity);
        });

        return players;
    }
}
