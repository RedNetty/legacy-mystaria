package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.HashMapSorter;
import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGenerator;
import me.retrorealms.practiceserver.mechanics.mobs.boss.BossConfigHandler;
import me.retrorealms.practiceserver.mechanics.mobs.boss.BossSpawnLocation;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.WorldBossDrops;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.BossEnum;
import me.retrorealms.practiceserver.mechanics.money.GemPouches;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WorldBossHandler implements Listener {

    private static final int MAX_SPAWN_ATTEMPTS = 5;
    private static final long BOSS_DESPAWN_TIME = 6600L; // 1 hour in seconds
    private static final long BOSS_CHECK_INTERVAL = 1200L; // 1 minute in ticks
    private static final double MINIMUM_DAMAGE_FOR_LOOT = 5000.0;
    private static final long BOSS_SPAWN_COOLDOWN = 3600000; // 1 hour in milliseconds

    @Getter private static int totalT5Kills = 0;
    @Getter private static WorldBoss activeBoss = null;
    @Getter private static BossSpawnLocation bossSpawnLocation = null;
    private static long lastBossSpawnTime = 0;

    public static void addKill() {
        if (activeBoss == null) {
            totalT5Kills++;
        }
    }

    public static void resetKills() {
        totalT5Kills = 0;
    }

    public static void spawnBoss() {

        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            try {
                Bukkit.getLogger().info("Starting boss spawn process (Attempt " + (attempt + 1) + ")...");

                bossSpawnLocation = selectSpawnLocation();
                if (bossSpawnLocation == null) {
                    Bukkit.getLogger().warning("Failed to select spawn location. Retrying...");
                    continue;
                }

                Location spawnLocation = validateSpawnLocation(bossSpawnLocation);
                if (spawnLocation == null) {
                    Bukkit.getLogger().warning("Failed to validate spawn location. Retrying...");
                    continue;
                }

                BossEnum selectedBoss = ThreadLocalRandom.current().nextBoolean() ? BossEnum.FROSTWING : BossEnum.CHRONOS;
                Bukkit.getLogger().info("Selected boss: " + selectedBoss);

                WorldBoss bossInstance = selectedBoss.getWorldBoss();
                if (bossInstance == null) {
                    Bukkit.getLogger().warning("Failed to create WorldBoss instance for " + selectedBoss.name() + "!");
                    continue;
                }

                bossInstance = bossInstance.spawnBoss(spawnLocation);
                if (bossInstance == null) {
                    Bukkit.getLogger().warning("spawnBoss method returned null for " + selectedBoss.name() + "!");
                    continue;
                }

                if (bossInstance.getLivingEntity() == null) {
                    Bukkit.getLogger().warning("Boss entity is null after spawning " + selectedBoss.name() + "!");
                    continue;
                }

                if (activeBoss == null) {
                    activeBoss = bossInstance;
                    announceAndVisualizeBossSpawn(bossInstance, bossSpawnLocation);
                    lastBossSpawnTime = System.currentTimeMillis();
                    resetKills();
                    Bukkit.getLogger().info("Boss spawn process completed successfully.");
                    return;
                } else {
                    Bukkit.getLogger().warning("Another boss was spawned concurrently. Aborting this spawn.");
                    return;
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error during boss spawn attempt " + (attempt + 1) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        Bukkit.getLogger().severe("Failed to spawn boss after " + MAX_SPAWN_ATTEMPTS + " attempts.");
    }

    private static BossSpawnLocation selectSpawnLocation() {
        if (BossSpawnLocation.values().length == 0) {
            Bukkit.getLogger().warning("No boss spawn locations defined!");
            return null;
        }
        return BossSpawnLocation.values()[ThreadLocalRandom.current().nextInt(BossSpawnLocation.values().length)];
    }

    private static Location validateSpawnLocation(BossSpawnLocation bossSpawnLocation) {
        Location spawnLocation = bossSpawnLocation.getLocation();
        if (spawnLocation == null) {
            Bukkit.getLogger().warning("Spawn location is null for " + bossSpawnLocation.name() + "!");
            return null;
        }
        if (!spawnLocation.getChunk().isLoaded()) {
            spawnLocation.getChunk().load();
        }
        return spawnLocation;
    }

    private static void announceAndVisualizeBossSpawn(WorldBoss boss, BossSpawnLocation location) {
        if (boss == null) {
            Bukkit.getLogger().severe("Cannot announce boss spawn: boss is null");
            return;
        }

        if (boss.getLivingEntity() == null) {
            Bukkit.getLogger().severe("Cannot announce boss spawn: boss entity is null");
            return;
        }

        if (location == null) {
            Bukkit.getLogger().severe("Cannot announce boss spawn: location is null");
            return;
        }

        World world = boss.getLivingEntity().getWorld();
        if (world == null) {
            Bukkit.getLogger().severe("Cannot announce boss spawn: world is null");
            return;
        }

        world.spawnParticle(Particle.FLAME, boss.getLivingEntity().getLocation(), 50, 0.5, 0.5, 0.5, 0);

        String bossName = (boss.bossEnum != null) ? boss.bossEnum.getDisplayName() : "Unknown Boss";
        String bossSpawnMessage = ChatColor.YELLOW + "* " + bossName + " spawned at '" + location.name() + "' *";

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                StringUtil.sendCenteredMessage(player, "&e★☆✫ &3&l World-Boss &e✫☆★");
                StringUtil.sendCenteredMessage(player, bossSpawnMessage);
                ActionBar.sendActionBar(player, bossSpawnMessage, 3);
                player.playSound(player.getLocation(), Sound.BLOCK_END_GATEWAY_SPAWN, 2, 2);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Error sending boss spawn message to player " + player.getName() + ": " + e.getMessage());
            }
        }
    }


    private static void despawnBoss(String message) {
        if (activeBoss != null) {
            activeBoss.getLivingEntity().remove();
            Bukkit.broadcastMessage(ChatColor.RED + message);
            activeBoss = null;
        }
        resetKills();
    }

    public static WorldBoss bossFromString(String boss) {
        return boss.equalsIgnoreCase("chronos") ? BossEnum.CHRONOS.getWorldBoss() : BossEnum.FROSTWING.getWorldBoss();
    }

    public void onLoad() {
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
        BossConfigHandler.onLoad();
        new BukkitRunnable() {
            public void run() {
                if (totalT5Kills > 450 && activeBoss == null) {
                    spawnBoss();
                }
                if (activeBoss != null && activeBoss.getLivingEntity() != null) {
                    if (activeBoss.getLivingEntity().isDead()) {
                        activeBoss = null;
                    } else {
                        Location location = activeBoss.getLivingEntity().getLocation();
                        activeBoss.getLivingEntity().getWorld().getNearbyEntities(location, 15, 15, 15).stream()
                                .filter(entity -> entity instanceof Player)
                                .map(entity -> (Player) entity)
                                .filter(Player::isInsideVehicle)
                                .forEach(player -> {
                                    player.getVehicle().remove();
                                    StringUtil.sendCenteredMessage(player, "&7&l*** &7You've dismounted due to a powerful Aura &7&l***");
                                });
                    }
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);
    }

    public void rewardLoot() {
        if (activeBoss == null) {
            Bukkit.getLogger().warning("Active boss is null in rewardLoot method");
            return;
        }

        ArrayList<Map.Entry<Player, Integer>> players = HashMapSorter.sortTopPlayers(activeBoss.getDamageDone());

        String bossName = activeBoss.bossEnum != null ? activeBoss.bossEnum.getDisplayName() : "World Boss";
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            StringUtil.sendCenteredMessage(player, "&e★☆✫ &3&l World-Boss &e✫☆★");
            StringUtil.sendCenteredMessage(player, "&b" + bossName + " &7has been defeated!");
            for (int i = 0; i < Math.min(3, players.size()); i++) {
                Map.Entry<Player, Integer> entry = players.get(i);
                StringUtil.sendCenteredMessage(player, "&e* " + (i + 1) + ". " + entry.getKey().getName() + " &7- &c" + entry.getValue() + "DMG");
            }
            player.sendMessage("");
        });

        players.stream()
                .filter(entry -> entry.getValue() >= MINIMUM_DAMAGE_FOR_LOOT)
                .forEach(entry -> getLoot(entry.getKey(), players.indexOf(entry) + 1));

        activeBoss = null;
    }

    public void getLoot(Player player, int place) {
        if (activeBoss == null || activeBoss.livingEntity == null) {
            Bukkit.getLogger().warning("Active boss or its entity is null in getLoot method");
            return;
        }
        LivingEntity livingEntity = activeBoss.livingEntity;
        if (player != null) {
            List<ItemStack> lootDrops = generateLoot(place, livingEntity, activeBoss.entityName);
            lootDrops.forEach(item -> {
                Mobdrops.dropShowString(player, item, null);
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), item);
                } else {
                    player.getInventory().addItem(item);
                }
            });
        }
    }

    private List<ItemStack> generateLoot(int place, LivingEntity livingEntity, String bossEntityName) {
        List<ItemStack> lootDrops = new ArrayList<>();
        int armChance = 65, wepChance = 65, legOrbAmount = 2, t5ProtAmount = 2, bankNoteAmount = 5000;

        switch (place) {
            case 1:
                armChance = 6; wepChance = 9; legOrbAmount = 15; t5ProtAmount = 10; bankNoteAmount = 35000;
                break;
            case 2:
                armChance = 8; wepChance = 13; legOrbAmount = 10; t5ProtAmount = 7; bankNoteAmount = 25000;
                break;
            case 3:
                armChance = 10; wepChance = 15; legOrbAmount = 8; t5ProtAmount = 4; bankNoteAmount = 25000;
                break;
        }

        if (ThreadLocalRandom.current().nextInt(wepChance) == 2)
            lootDrops.add(livingEntity.getEquipment().getItemInMainHand());
        if (ThreadLocalRandom.current().nextInt(armChance) == 2)
            lootDrops.add(WorldBossDrops.createDrop(5, bossEntityName));
        if (ThreadLocalRandom.current().nextInt(armChance) == 2)
            lootDrops.add(livingEntity.getEquipment().getChestplate());
        if (ThreadLocalRandom.current().nextInt(armChance) == 2)
            lootDrops.add(livingEntity.getEquipment().getLeggings());
        if (ThreadLocalRandom.current().nextInt(armChance) == 2)
            lootDrops.add(livingEntity.getEquipment().getBoots());

        ItemStack legOrb = Items.legendaryOrb(false).clone();
        legOrb.setAmount(ThreadLocalRandom.current().nextInt((legOrbAmount / 2) + 1, legOrbAmount + 1));
        lootDrops.add(legOrb);

        ItemStack t5Prot = new ScrollGenerator().next(4).clone();
        t5Prot.setAmount(ThreadLocalRandom.current().nextInt((t5ProtAmount / 2) + 1, t5ProtAmount + 1));
        lootDrops.add(t5Prot);

        lootDrops.add(Money.createBankNote(ThreadLocalRandom.current().nextInt((bankNoteAmount / 2) + 1, bankNoteAmount + 1)).clone());

        for (int i = 0; i < ThreadLocalRandom.current().nextInt(place + 1); i++) {
            lootDrops.add(GemPouches.gemPouch(6, false).clone());
        }

        return lootDrops;
    }

    @EventHandler
    public void onBossKill(EntityDeathEvent event) {
        if (activeBoss != null && event.getEntity().equals(activeBoss.getLivingEntity())) {
            rewardLoot();
            resetKills();
        }
    }

    @EventHandler
    public void onDamageBoss(EntityDamageByEntityEvent e) {
        if (activeBoss != null && e.getEntity().equals(activeBoss.getLivingEntity())) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                activeBoss.addDamage(p, (int) e.getFinalDamage());
            } else if (e.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) e.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    Player p = (Player) projectile.getShooter();
                    activeBoss.addDamage(p, (int) e.getFinalDamage());
                }
            }
        }
    }

    // Test method to spawn a simple entity
    public static void testSpawn(Location location) {
        try {
            Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            Bukkit.getLogger().info("Test zombie spawned at: " + location + " with ID: " + zombie.getEntityId());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error spawning test zombie: " + e.getMessage());
            e.printStackTrace();
        }
    }
}