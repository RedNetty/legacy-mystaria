package me.retrorealms.practiceserver.mechanics.useless.skeleton;

import com.google.common.collect.Lists;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.useless.DungeonPlayer;
import me.retrorealms.practiceserver.mechanics.useless.DungeonPool;
import me.retrorealms.practiceserver.mechanics.useless.api.Actionbar;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

/**
 * Created by Giovanni on 2-5-2017.
 */
public class SkeletonDungeon implements Listener {

    /* HARDCODED, WHO CARES?! WANTED TO MAKE THIS QUICK AND DIRTY */
    private final List<DungeonPlayer> dungeonPlayers = Lists.newArrayList();

    private final ConcurrentHashMap<Integer, Location> spawnerLocationsIntro = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Integer> maxWaves = new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<Entity> livingEntities = Lists.newCopyOnWriteArrayList();

    private World world;

    private int introTask;
    private int introTask2;

    private int ambientSoundTask;

    private int wave = 0;
    private int waveTask;
    private int entityTracker;

    private int specialWaveThunder;

    private boolean wave3Special;
    private int bossChecker3;
    private boolean wrath3;

    private SkeletonBoss skeletonBoss;

    public SkeletonDungeon(List<Player> playerList) {
        playerList.forEach(player -> {
            dungeonPlayers.add(new DungeonPlayer(player));
        });

        System.out.println("Skeleton Dungeon loaded..");

        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            dungeonPlayers.forEach(dungeonPlayer -> {

                dungeonPlayer.sendMessage("&cAn overwhelming force goes through your body as you enter the dungeon..", false);
                dungeonPlayer.freeze(100);
                dungeonPlayer.blind(100);

                dungeonPlayer.playSound(Sound.ENTITY_ELDER_GUARDIAN_DEATH, 10F, 1.1F);
            });

            DungeonPool.getPool().load(loadedWorld -> {

                dungeonPlayers.forEach(player -> {
                    player.getPlayer().teleport(new Location(loadedWorld, -1774, 21, -537, -91F, -6.3F));
                    player.getPlayer().setFallDistance(0F);

                    for (PotionEffect potionEffect : player.getPlayer().getActivePotionEffects())
                        player.getPlayer().removePotionEffect(potionEffect.getType());
                });

                world = loadedWorld;


                spawnerLocationsIntro.put(1, new Location(world, -1754, 23, -538));
                spawnerLocationsIntro.put(2, new Location(world, -1764, 21, -555));
                spawnerLocationsIntro.put(3, new Location(world, -1731, 21, -523));
                spawnerLocationsIntro.put(4, new Location(world, -1732, 21, -556));

                maxWaves.put(1, 25);
                maxWaves.put(2, 45);
                maxWaves.put(3, 55);
                maxWaves.put(4, 65);
            });
        }, 10);

        introTask = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), () -> {


            if (world != null) {

                if (world.getPlayers().size() >= dungeonPlayers.size()) {

                    start();

                    Bukkit.getScheduler().cancelTask(introTask);
                }
            }

        }, 0, 10);

    }

    private void waveTaskStart() {
        entityTracker = Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), () -> {

            livingEntities.forEach(entity -> {
                if (entity.isDead()) livingEntities.remove(entity);
            });

            dungeonPlayers.forEach(dungeonPlayer -> {
                Actionbar actionbar = new Actionbar()
                        .setPlayer(dungeonPlayer.getPlayer())
                        .setMessage("&eSKELETON DUNGEON &6- &7[ &cWAVE&c: &c" + wave + " &6- &aMONSTERS&a: &c" + livingEntities.size() + " &7]");

                actionbar.send();
            });

        }, 0L, 10);

        waveTask = Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), () -> {
            if (wave <= 0 || wave >= 4) return;

            if (!livingEntities.isEmpty() || skeletonBoss != null && skeletonBoss.isAlive()) return;

            wave++;

            startWave(wave);
        }, 0L, 100);
    }

    private void handleSpecialWave() {
        wave3Special = true;
        wave--;

        List<Location> thunderLocations = Arrays.asList(
                new Location(world, -1737, 40, -548),
                new Location(world, -1749, 45, -518)
        );

        int[] currentTime = {0};

        int maxEntities = maxWaves.get(wave);
        int perSpawner = (maxEntities / 4) / 2;

        bossChecker3 = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {

            if (skeletonBoss == null) return;

            if (skeletonBoss.isAlive()) {
                float maxHP = skeletonBoss.getMaxHealth();
                float currentHP = skeletonBoss.getHealth();

                if (currentHP <= maxHP / 2) {

                    if (!wrath3) {
                        dungeonPlayers.forEach(dungeonPlayer -> {

                            dungeonPlayer.sendMessage("&cRestless Skeleton Overlord: Feel my powers rising, slowly, through your body..", true);
                        });

                        IntStream.range(1, 4).forEach(spawnerCount -> {

                            IntStream.range(0, perSpawner / 2).forEach(toSpawn -> {

                                Location location = spawnerLocationsIntro.get(spawnerCount);

                                double newX = location.getX() + new Random().nextInt(2);
                                double newZ = location.getZ() + new Random().nextInt(2);

                                Location location1 = new Location(world, newX, location.getY(), newZ);

                                livingEntities.add(Spawners.getInstance().spawnMob(location1, "skellyDSkeletonGuardian", 5, false));
                            });


                        });

                        wrath3 = true;
                    }

                    dungeonPlayers.forEach(dungeonPlayer -> {

                        dungeonPlayer.getPlayer().getWorld().strikeLightning(dungeonPlayer.getPlayer().getLocation());
                        dungeonPlayer.getPlayer().getWorld().strikeLightningEffect(dungeonPlayer.getPlayer().getLocation());
                    });
                } else if (currentHP <= 1000) {
                    dungeonPlayers.forEach(dungeonPlayer -> {

                        dungeonPlayer.playSound(Sound.BLOCK_PORTAL_TRAVEL, 10F, 1.7F);
                    });


                    Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
                        dungeonPlayers.forEach(dungeonPlayer -> {

                            dungeonPlayer.sendMessage("&cRestless Skeleton Overlord: Did you really think you could kill me that easily?", false);
                            dungeonPlayer.playSound(Sound.ENTITY_WITHER_AMBIENT, 10F, 0.1F);
                        });

                        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {

                            livingEntities.forEach(Entity::remove);
                            livingEntities.clear();

                            dungeonPlayers.forEach(dungeonPlayer -> {

                                dungeonPlayer.playSound(Sound.ENTITY_ENDERMEN_TELEPORT, 10, 0.35F);
                            });

                        }, 20 * 3);
                    }, 20 * 3);

                    Bukkit.getScheduler().cancelTask(bossChecker3);
                }

                return;
            }

        }, 0L, 10);

        specialWaveThunder = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {
            if (currentTime[0] >= 10) {
                Bukkit.getScheduler().cancelTask(specialWaveThunder);

                return;
            }

            currentTime[0]++;

            thunderLocations.forEach(thunderLocation -> {
                world.strikeLightning(thunderLocation);
                world.strikeLightningEffect(thunderLocation);
            });
        }, 0L, 20);

        dungeonPlayers.forEach(dungeonPlayer -> {
            dungeonPlayer.sendMessage("", false);
            dungeonPlayer.sendMessage("&c&lBOSS WAVE", true);
            dungeonPlayer.sendMessage("&7 [ Defeat the Skeleton Overlord and it's elite guardians ]", true);
            dungeonPlayer.playSound(Sound.ENTITY_WITHER_AMBIENT, 10F, 0.1F);
        });

        Location monsterLocation = new Location(world, -1752, 30, -538, 90.7F, 36.9F);

        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            IntStream.range(1, 4).forEach(spawnerCount -> {

                IntStream.range(0, perSpawner).forEach(toSpawn -> {

                    Location location = spawnerLocationsIntro.get(spawnerCount);

                    double newX = location.getX() + new Random().nextInt(2);
                    double newZ = location.getZ() + new Random().nextInt(2);

                    Location location1 = new Location(world, newX, location.getY(), newZ);

                    livingEntities.add(Spawners.getInstance().spawnMob(location1, "skellyDSkeletonGuardianElite", 3, true));
                });


            });

            skeletonBoss = new SkeletonBoss(world, this);
            skeletonBoss.spawn(monsterLocation);
        }, 20);
    }

    private void startWave(int wave) {

        if (wave == 4)
            Bukkit.getScheduler().cancelTask(waveTask);

        if (wave == 3) {
            if (!wave3Special) {

                this.handleSpecialWave();

                return;
            }
        }

        dungeonPlayers.forEach(dungeonPlayer -> {
            dungeonPlayer.sendMessage("", false);
            dungeonPlayer.sendMessage("&cWAVE: &l" + wave, true);
            dungeonPlayer.sendMessage("&7[ Defeat the monsters and conquer the Great Portal ]", true);
            dungeonPlayer.playSound(Sound.ENTITY_ENDERDRAGON_GROWL, 10F, 0.3F);
        });

        int maxEntities = maxWaves.get(wave);
        int perSpawner = maxEntities / 4;

        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            IntStream.range(1, 4).forEach(spawnerCount -> {

                IntStream.range(0, perSpawner).forEach(toSpawn -> {

                    Location location = spawnerLocationsIntro.get(spawnerCount);

                    double newX = location.getX() + new Random().nextInt(2);
                    double newZ = location.getZ() + new Random().nextInt(2);

                    Location location1 = new Location(world, newX, location.getY(), newZ);

                    livingEntities.add(Spawners.getInstance().spawnMob(location1, "skellyDSkeletonGuardian", 3, false));
                });

            });
        }, 20);

        List<Location> thunderLocations = Arrays.asList(
                new Location(world, -1737, 40, -548),
                new Location(world, -1749, 45, -518)
        );

        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            thunderLocations.forEach(thunderLocation -> {
                world.strikeLightning(thunderLocation);
                world.strikeLightningEffect(thunderLocation);
            });
        }, 40);
    }

    private void start() {
        waveTaskStart();

        ambientSoundTask = Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), () -> {

            boolean doSound = new Random().nextInt(150) <= 60;

            if (!doSound) return;

            dungeonPlayers.forEach(dungeonPlayer -> {
                dungeonPlayer.playSound(Sound.ENTITY_ENDERDRAGON_AMBIENT, 10F, 0.1F);
            });

        }, 0L, 20 * 35);


        List<Location> thunderLocations = Arrays.asList(
                new Location(world, -1737, 40, -548),
                new Location(world, -1749, 45, -518)
        );

        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            thunderLocations.forEach(thunderLocation -> {
                world.strikeLightning(thunderLocation);
                world.strikeLightningEffect(thunderLocation);
            });
        }, 80);

        int[] currentTime = {0};

        SkeletonBoss[] skeletonBossTemp = {null};

        Location spawnLocation = new Location(world, -1752, 28, -538);

        introTask2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {

            currentTime[0]++;

            if (currentTime[0] == 8) {
                DungeonPlayer dungeonPlayerTarget = dungeonPlayers.get(new Random().nextInt(dungeonPlayers.size()));

                dungeonPlayers.forEach(dungeonPlayer -> {
                    dungeonPlayer.playSound(Sound.ENTITY_WITHER_DEATH, 10, 1.7F);

                    IntStream.range(0, 40).forEach(message -> {
                        dungeonPlayer.sendMessage("", false);
                    });

                    dungeonPlayer.sendMessage("&7" + dungeonPlayerTarget.getPlayer().getName() + ": What's that? Who's there? ", false);
                });

                spawnLocation.getBlock().setType(Material.BARRIER);
            }

            if (currentTime[0] == 13) {
                Location monsterLocation = new Location(world, -1752, 30, -538, 90.7F, 36.9F);

                skeletonBossTemp[0] = new SkeletonBoss(world, this);
                skeletonBossTemp[0].invulnerable(true).clearAI().spawn(monsterLocation);

                dungeonPlayers.forEach(dungeonPlayer -> {
                    dungeonPlayer.playSound(Sound.ENTITY_ELDER_GUARDIAN_HURT, 10, 0.15F);

                    dungeonPlayer.sendMessage("&cRestless Skeleton Overlord: I welcome you to my domain.. And my army.", false);
                });
            }

            if (currentTime[0] == 19) {
                dungeonPlayers.forEach(dungeonPlayer -> {
                    dungeonPlayer.playSound(Sound.ENTITY_ENDERMEN_TELEPORT, 10, 0.35F);

                    dungeonPlayer.sendMessage("&cRestless Skeleton Overlord: Defend the portal, guardians!", false);
                });

                skeletonBossTemp[0].getBukkitEntity().remove();
                spawnLocation.getBlock().setType(Material.AIR);


                int maxEntities = maxWaves.get(1);
                int perSpawner = maxEntities / 4;

                IntStream.range(1, 4).forEach(spawnerCount -> {

                    IntStream.range(0, perSpawner).forEach(toSpawn -> {

                        Location location = spawnerLocationsIntro.get(spawnerCount);

                        double newX = location.getX() + new Random().nextInt(2);
                        double newZ = location.getZ() + new Random().nextInt(2);

                        Location location1 = new Location(world, newX, location.getY(), newZ);

                        livingEntities.add(Spawners.getInstance().spawnMob(location1, "skellyDSkeletonGuardian", 3, false));
                    });

                });
            }

            if (currentTime[0] >= 20 && currentTime[0] <= 25) {
                thunderLocations.forEach(thunderLocation -> {
                    world.strikeLightning(thunderLocation);
                    world.strikeLightningEffect(thunderLocation);
                });

                if (wave > 0) return;

                dungeonPlayers.forEach(dungeonPlayer -> {
                    dungeonPlayer.sendMessage("", false);
                    dungeonPlayer.sendMessage("&cWAVE: &l1", true);
                    dungeonPlayer.sendMessage("&7[ Defeat the monsters and conquer the Great Portal ]", true);
                    dungeonPlayer.playSound(Sound.ENTITY_ENDERDRAGON_GROWL, 10F, 0.3F);
                });

                wave++;

            }

            if (currentTime[0] == 25) {
                Bukkit.getScheduler().cancelTask(introTask2);
            }

        }, 0, 20);

    }
}
