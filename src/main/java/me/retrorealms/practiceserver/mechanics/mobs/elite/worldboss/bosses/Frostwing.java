package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.SkullTextures;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.BossGearGenerator;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.WorldBossDrops;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBoss;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Frostwing extends WorldBoss implements Listener {

    public BossEnum bossEnum = BossEnum.FROSTWING;
    int timeSinceLastATK = 20;
    List<LivingEntity> minionList = new ArrayList<>();
    int attackFreq = 30;
    boolean bezerk = false;
    boolean healing = false;
    Location bossLocation;

    int stage = 0;
    private int phase = 1;
    private boolean isInvulnerable = false;
    private Map<UUID, Long> playerFrostbiteMap = new HashMap<>();
    private List<Location> iceWalls = new ArrayList<>();
    private int iceWallDuration = 200;
    private boolean glacialStormActive = false;
    private int glacialStormTicks = 0;
    private Location lastTeleportLocation;
    public Frostwing() {
        super(BossEnum.FROSTWING);
    }
    @Override
    public WorldBoss spawnBoss(Location location) {
        this.livingEntity = Spawners.spawnMob(location, "frostwing", 5, true);
        this.entityName = "frostwing";
        this.tier = 5;
        this.bossLocation = location;
        this.bossEnum = BossEnum.FROSTWING;
        livingEntity.setMaxHealth(ThreadLocalRandom.current().nextInt(400000, 450000));
        livingEntity.setHealth(livingEntity.getMaxHealth());
        setArmor();

        startBossAI();
        startPhaseManager();

        return this;
    }

    public boolean isBezerk() {
        return bezerk;
    }

    public void activateBezerk() {
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            StringUtil.sendCenteredMessage(player, ChatColor.YELLOW + "*******************************************************");
            StringUtil.sendCenteredMessage(player, ChatColor.AQUA + "Frost-Wing is now en-raged (>25% Health)");
            StringUtil.sendCenteredMessage(player, ChatColor.GRAY + "50% damage increase, 50% attack frequency");
            StringUtil.sendCenteredMessage(player, ChatColor.GRAY + "50% speed");
            StringUtil.sendCenteredMessage(player, ChatColor.YELLOW + "*******************************************************");
        });
        attackFreq = 20;
        bezerk = true;
        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2500, 1), true);
    }

    public void bossMechanic(int percent) {
        if (healing) return;
        if ((stage == 0 && percent < 75) || (stage == 1 && percent < 50) || (stage == 2 && percent < 35) || (stage == 3 && percent < 15)) {
            stage++;
            healing = true;
        }
        if (healing) {
            bossLocation = bossLocation.getBlock().getLocation();
            circleHealAnimation(livingEntity);
        }
        if (healing) {
            new BukkitRunnable() {
                int i = 0;

                public void run() {
                    try {
                        if (i < 5) {
                            i++;
                            summonMinionsAttack(livingEntity.getLocation(), 1);
                            int healAmount = ThreadLocalRandom.current().nextInt(10000, 13600);
                            livingEntity.setHealth(livingEntity.getHealth() + healAmount);
                            livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).forEach(entity -> {
                                if (entity instanceof Player) {
                                    StringUtil.sendCenteredMessage((Player) entity, ChatColor.translateAlternateColorCodes('&', livingEntity.getName() + " &7 - &aHealed: +" + healAmount + "hp"));
                                }
                            });
                        }
                        if (i >= 5) {
                            healing = false;
                            this.cancel();
                        }
                    } catch (Exception e) {
                        // Handle exception
                    }
                }
            }.runTaskTimer(PracticeServer.getInstance(), 0L, 5L);
        }
    }

    public void circleHealAnimation(Entity boss) {
        Location bossLocation = boss.getLocation();
        World world = boss.getWorld();

        Random random = new Random();
        List<Location> particleLocations = new ArrayList<>();
        new BukkitRunnable() {
            public void run() {
                try {
                    if (healing) {
                        livingEntity.teleport(bossLocation);
                    } else {
                        this.cancel();
                    }
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
        new BukkitRunnable() {
            int ticks = 0;

            public void run() {
                try {
                    if (ticks >= 10 || !healing) {
                        this.cancel();
                        return;
                    }
                    for (int i = 0; i < 5; i++) {
                        double x = bossLocation.getX() + (random.nextDouble() * 2 - 1);
                        double y = 8;
                        double z = bossLocation.getZ() + (random.nextDouble() * 2 - 1);
                        Location particleLocation = new Location(world, x, y, z);
                        particleLocations.add(particleLocation);
                        world.spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 1);
                    }
                    for (Iterator<Location> iterator = particleLocations.iterator(); iterator.hasNext(); ) {
                        Location location = iterator.next();
                        location.add(0, -0.5, 0);
                        world.spawnParticle(Particle.VILLAGER_HAPPY, location, 1);
                        if (location.getBlockY() <= bossLocation.getBlockY()) {
                            iterator.remove();
                        }
                    }
                    ticks++;
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 2L);
    }

    private void startBossAI() {
        new BukkitRunnable() {
            public void run() {
                try {
                    if (livingEntity.isDead()) {
                        cleanupBoss();
                        this.cancel();
                        return;
                    }
                    List<Player> nearbyPlayers = livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).stream().filter(player -> player instanceof Player).map(player -> (Player) player).collect(Collectors.toList());
                    if (nearbyPlayers.size() > 0 && timeSinceLastATK < attackFreq) timeSinceLastATK++;
                    if (timeSinceLastATK >= attackFreq) {
                        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).forEach(entity -> {
                            if (entity instanceof Player && timeSinceLastATK >= attackFreq) {
                                if (!healing && livingEntity.isOnGround()) doRandomAttack();
                            }
                        });
                    }

                    updateGlacialStorm();
                    updateIceWalls();
                    bossMechanic((int) (livingEntity.getHealth() / livingEntity.getMaxHealth() * 100));
                    if ((livingEntity.getHealth() / livingEntity.getMaxHealth() * 100) < 23) {
                        if (!bezerk) activateBezerk();
                    }
                    if (bezerk) {
                        livingEntity.getWorld().spawnParticle(Particle.DRAGON_BREATH, livingEntity.getEyeLocation(), 3, 0.5, 0.8, 0.5, 0.1);
                    }
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);
    }

    private void cleanupBoss() {
        minionList.forEach(minion -> {
            if (!minion.isDead()) {
                if (Spawners.mobs.containsKey(minion)) Spawners.mobs.remove(minion);
                minion.remove();
            }
        });
        iceWalls.forEach(loc -> loc.getBlock().setType(Material.AIR));
        glacialStormActive = false;
    }

    private void updateGlacialStorm() {
        if (glacialStormActive) {
            glacialStormTicks++;
            if (glacialStormTicks >= 200) { // 30 seconds
                glacialStormActive = false;
                glacialStormTicks = 0;
                announceToNearbyPlayers(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "The Glacial Storm subsides...");
            } else {
                livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 30, 30, 30).stream()
                        .filter(e -> e instanceof Player)
                        .forEach(e -> {
                            Player p = (Player) e;
                            p.damage(100);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
                        });
                livingEntity.getWorld().spawnParticle(Particle.SNOW_SHOVEL, livingEntity.getLocation(), 500, 15, 5, 15, 0.1);
            }
        }
    }

    private void updateIceWalls() {
        iceWalls.removeIf(loc -> {
            if (loc.getBlock().getType() == Material.ICE) {
                if (ThreadLocalRandom.current().nextInt(100) < 5) {
                    loc.getBlock().setType(Material.AIR);
                    return true;
                }
            } else {
                return true;
            }
            return false;
        });
    }

    private void doRandomAttack() {
        timeSinceLastATK = 0;
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2, 2);
        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).forEach(entity -> {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                int msg = ThreadLocalRandom.current().nextInt(1, 4);
                switch (msg) {
                    case 1:
                        player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "You will freeze in your tracks!");
                        break;
                    case 2:
                        player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "My power is absolute, and your fate is sealed in ice!");
                        break;
                    case 3:
                        player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "Your warmth will be extinguished, just like your life!");
                        break;
                }
            }
        });
        int attackChoice = ThreadLocalRandom.current().nextInt(100);
        if (attackChoice < 20) {
            iceBlockBarrageAttack(livingEntity, 10, ThreadLocalRandom.current().nextInt(1500, 2000), 25, 25);
        } else if (attackChoice < 40) {
            icyGroundAttack(livingEntity.getLocation(), 9, ThreadLocalRandom.current().nextInt(300, 370), ThreadLocalRandom.current().nextInt(4, 5), 35, 5);
        } else if (attackChoice < 55) {
            summonMinionsAttack(livingEntity.getLocation(), ThreadLocalRandom.current().nextInt(2, 5));
        } else if (attackChoice < 70) {
            frostNova();
        } else if (attackChoice < 85) {
            blizzardStorm();
        } else if (attackChoice < 95) {
            frostbiteAura();
        } else {
            glacialCataclysm();
        }
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
                    world.spawnParticle(Particle.SNOW_SHOVEL, particleLocation, 3, 0.5, 0.8, 0.5, 0.1);
                }
            }
            new BukkitRunnable() {
                public void run() {
                    try {
                        if (ticks[0] >= duration) {
                            this.cancel();
                            return;
                        }
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getLocation().distance(spotLocation) <= spotSize) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 20, 1));
                                player.damage(damage);
                            }
                        }
                        ticks[0]++;
                    } catch (Exception e) {
                        // Handle exception
                    }
                }
            }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
        }
        new BukkitRunnable() {
            public void run() {
                try {
                    if (ticks[0] >= duration) {
                        this.cancel();
                        return;
                    }
                    for (int i = 0; i < numberOfSpots; i++) {
                        double angle = 2 * Math.PI * i / numberOfSpots;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location spotLocation = bossLoc.clone().add(x, 0, z);
                        spotLocation.setY(world.getHighestBlockYAt(spotLocation));
                        for (int dx = -spotSize; dx <= spotSize; dx++) {
                            for (int dz = -spotSize; dz <= spotSize; dz++) {
                                if (ticks[0] >= duration) return;
                                Location particleLocation = spotLocation.clone().add(dx, 0, dz);
                                world.playSound(particleLocation, Sound.BLOCK_SNOW_BREAK, 1, 1);
                                world.spawnParticle(Particle.SNOW_SHOVEL, particleLocation, 3, 0.5, 0.8, 0.5, 0.1);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
    }

    public void summonMinionsAttack(Location bossLoc, int numberOfMinions) {
        World world = bossLoc.getWorld();
        for (int i = 0; i < numberOfMinions; i++) {
            double angle = 2 * Math.PI * i / numberOfMinions;
            double x = Math.cos(angle) * 3;
            double z = Math.sin(angle) * 3;
            int elite = ThreadLocalRandom.current().nextInt(1, 7);

            boolean isElite = elite == 1;
            Location minionLocation = bossLoc.clone().add(x, 1, z);
            LivingEntity livingEntity = Spawners.spawnMob(minionLocation, "skeleton", 5, isElite);
            minionList.add(livingEntity);
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.PACKED_ICE));
            livingEntity.setCustomName(ChatColor.YELLOW + "Frost-Wing's Loyal Frozen Minion");
        }
    }

    public void iceBlockBarrageAttack(Entity entity, int radius, int damage, int numberOfIceBlocks, int duration) {
        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                try {
                    if (tick >= 2) {
                        Location location = entity.getLocation();
                        World world = entity.getLocation().getWorld();
                        new BukkitRunnable() {
                            public void run() {
                                try {
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
                                                if (currentRadius[0] <= radius) {
                                                    currentRadius[0]++;
                                                }
                                                if (currentDuration[0] == duration) {
                                                    this.cancel();
                                                    iceBlock.remove();
                                                } else {
                                                    currentDuration[0]++;
                                                }
                                            }
                                        }.runTaskTimer(PracticeServer.getInstance(), 20L, 5L);
                                        new BukkitRunnable() {
                                            public void run() {
                                                try {
                                                    if (currentDuration[0] == duration) this.cancel();
                                                    entity.teleport(location);
                                                    if (iceBlock.isValid()) {
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
                                                } catch (Exception e) {
                                                    // Handle exception
                                                }
                                            }
                                        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
                                    }
                                } catch (Exception e) {
                                    // Handle exception
                                }
                            }
                        }.runTaskLater(PracticeServer.getInstance(), 5L);
                        this.cancel();
                    }
                    tick++;
                    entity.getWorld().playEffect(entity.getLocation().clone().add(0, 2, 0), Effect.STEP_SOUND, Material.FROSTED_ICE);
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 0L, 10L);
    }


    private long lastAnnouncementTime = 0;
    private static final long ANNOUNCEMENT_COOLDOWN = 10000; // 10 seconds in milliseconds

    private void frostNova() {
        if (System.currentTimeMillis() - lastAnnouncementTime > ANNOUNCEMENT_COOLDOWN) {
            announceAbility("Frost Nova", "The air crystallizes around you!");
            lastAnnouncementTime = System.currentTimeMillis();
        }

        Location center = livingEntity.getLocation();
        new BukkitRunnable() {
            double radius = 1;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40) {
                    this.cancel();
                    return;
                }

                // Create expanding ice circle
                for (double angle = 0; angle < 360; angle += 5) {
                    double x = radius * Math.cos(Math.toRadians(angle));
                    double z = radius * Math.sin(Math.toRadians(angle));
                    Location particleLoc = center.clone().add(x, 0, z);
                    center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.1, 0.1, 0.1, 0);
                    center.getWorld().spawnParticle(Particle.SNOW_SHOVEL, particleLoc, 3, 0.1, 0.1, 0.1, 0);
                }

                // Create ice spikes effect
                if (ticks % 5 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double angle = Math.random() * 360;
                        double distance = Math.random() * radius;
                        double x = distance * Math.cos(Math.toRadians(angle));
                        double z = distance * Math.sin(Math.toRadians(angle));
                        Location spikeLoc = center.clone().add(x, 0, z);
                        createIceSpikeEffect(spikeLoc);
                    }
                }

                radius += 0.5;
                ticks++;

                // Damage players, but give them a chance to avoid it
                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 5, radius)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        double distanceFromCenter = player.getLocation().distance(center);
                        if (distanceFromCenter < radius - 1.5 && canDamagePlayer(player)) { // Added cooldown check
                            double damage = 30 * phase * (1 - (distanceFromCenter / radius));
                            player.damage(damage);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                        }
                    }
                }

                center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }

    private void createIceSpikeEffect(Location location) {
        for (int y = 0; y < 3; y++) {
            Location spikePart = location.clone().add(0, y, 0);
            spikePart.getWorld().spawnParticle(Particle.END_ROD, spikePart, 10, 0.2, 0.2, 0.2, 0);
            spikePart.getWorld().spawnParticle(Particle.SNOW_SHOVEL, spikePart, 5, 0.1, 0.1, 0.1, 0);
        }
    }

    private void blizzardStorm() {
        if (System.currentTimeMillis() - lastAnnouncementTime > ANNOUNCEMENT_COOLDOWN) {
            announceAbility("Blizzard Storm", "Nature's fury unleashed!");
            lastAnnouncementTime = System.currentTimeMillis();
        }

        glacialStormActive = true;
        glacialStormTicks = 0;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!glacialStormActive || glacialStormTicks >= 200) {
                    glacialStormActive = false;
                    glacialStormTicks = 0;
                    if (System.currentTimeMillis() - lastAnnouncementTime > ANNOUNCEMENT_COOLDOWN) {
                        announceAbility("Blizzard Storm", "The storm calms...");
                        lastAnnouncementTime = System.currentTimeMillis();
                    }
                    this.cancel();
                    return;
                }

                Location center = livingEntity.getLocation();

                // Create swirling snow particles
                for (int i = 0; i < 5; i++) {
                    double angle = (glacialStormTicks * 5 + i * 72) % 360;
                    double x = 10 * Math.cos(Math.toRadians(angle));
                    double z = 10 * Math.sin(Math.toRadians(angle));
                    Location particleLoc = center.clone().add(x, 5 * Math.sin(Math.toRadians(glacialStormTicks * 10)), z);
                    center.getWorld().spawnParticle(Particle.SNOW_SHOVEL, particleLoc, 10, 1, 1, 1, 0);
                    center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0.5, 0.5, 0.5, 0);
                }

                // Periodically summon ice shards effect
                if (glacialStormTicks % 20 == 0) {
                    summonIceShardsEffect(center);
                }

                // Affect nearby players
                for (Entity entity : center.getWorld().getNearbyEntities(center, 25, 25, 25)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (Math.random() < 0.1 && canDamagePlayer(player)) { // 10% chance to apply effects each tick, with cooldown check
                            player.damage(5 * phase);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                        }

                        // Gentle push towards the center
                        Vector push = center.toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.05);
                        player.setVelocity(player.getVelocity().add(push));
                    }
                }

                glacialStormTicks++;
                if (glacialStormTicks % 20 == 0) { // Play sound every second
                    center.getWorld().playSound(center, Sound.WEATHER_RAIN, 1.0f, 0.5f);
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }

    private void summonIceShardsEffect(Location center) {
        for (int i = 0; i < 3; i++) { // Reduced number of shards
            Location shardLoc = center.clone().add(Math.random() * 20 - 10, 10, Math.random() * 20 - 10);
            Vector direction = center.toVector().subtract(shardLoc.toVector()).normalize();

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= 20) {
                        this.cancel();
                        return;
                    }
                    shardLoc.add(direction.clone().multiply(0.5));
                    shardLoc.getWorld().spawnParticle(Particle.END_ROD, shardLoc, 10, 0.1, 0.1, 0.1, 0);
                    shardLoc.getWorld().spawnParticle(Particle.SNOW_SHOVEL, shardLoc, 5, 0.1, 0.1, 0.1, 0);

                    for (Entity entity : shardLoc.getWorld().getNearbyEntities(shardLoc, 1, 1, 1)) {
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            player.damage(10 * phase);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                        }
                    }

                    ticks++;
                }
            }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
        }
    }

    private void announceAbility(String abilityName, String message) {
        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).forEach(entity -> {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "[" + abilityName + "] " + message);
            }
        });
    }

    private void frostbiteAura() {
        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).forEach(entity -> {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "Your flesh will freeze and shatter!");
            }
        });
        new BukkitRunnable() {
            int duration = 0;

            @Override
            public void run() {
                if (duration >= 120) {
                    this.cancel();
                    return;
                }
                for (Entity entity : livingEntity.getNearbyEntities(20, 20, 20)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (!playerFrostbiteMap.containsKey(player.getUniqueId())) {
                            playerFrostbiteMap.put(player.getUniqueId(), System.currentTimeMillis());
                        } else if (System.currentTimeMillis() - playerFrostbiteMap.get(player.getUniqueId()) > 1000) {
                            player.damage(200 * phase);
                            playerFrostbiteMap.put(player.getUniqueId(), System.currentTimeMillis());
                        }
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                    }
                }
                livingEntity.getWorld().spawnParticle(Particle.SNOW_SHOVEL, livingEntity.getLocation(), 100, 7, 1, 7, 0.1);
                duration++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }

    private void glacialCataclysm() {
        announceToNearbyPlayers(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "Behold the power of a Glacial Cataclysm!");
        isInvulnerable = true;
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 300) { // 15 seconds
                    isInvulnerable = false;
                    this.cancel();
                    return;
                }
                if (ticks % 40 == 0) { // Every 2 seconds
                    frostNova();
                }
                if (ticks % 20 == 0) { // Every 1 second
                    livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 50, 50, 50).stream()
                            .filter(e -> e instanceof Player)
                            .forEach(e -> ((Player) e).damage(250 * phase));
                }
                livingEntity.getWorld().spawnParticle(Particle.SNOWBALL, livingEntity.getLocation(), 100, 10, 10, 10, 0.1);
                ticks++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }


    private void teleportToNearestPlayer() {
        if (lastTeleportLocation != null && livingEntity.getLocation().distance(lastTeleportLocation) < 10) {
            return; // Prevent frequent teleportation to the same area
        }

        Optional<Entity> nearestPlayer = livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 100, 100, 100).stream()
                .filter(e -> e instanceof Player)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(livingEntity.getLocation())));

        nearestPlayer.ifPresent(player -> {
            Location teleportLocation = player.getLocation().add(ThreadLocalRandom.current().nextDouble(-5, 5), 0, ThreadLocalRandom.current().nextDouble(-5, 5));
            livingEntity.teleport(teleportLocation);
            lastTeleportLocation = teleportLocation;
            announceToNearbyPlayers("Frost-Wing suddenly appears nearby!");
            livingEntity.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 0.5f);
            livingEntity.getWorld().spawnParticle(Particle.DRAGON_BREATH, teleportLocation, 100, 1, 1, 1, 0.1);
        });
    }
    private void announceToNearbyPlayers(String message) {
        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 50, 50, 50).stream()
                .filter(e -> e instanceof Player)
                .forEach(e -> ((Player) e).sendMessage(ChatColor.AQUA + message));
    }
    private void startPhaseManager() {
        new BukkitRunnable() {
            public void run() {
                if (livingEntity.isDead()) {
                    this.cancel();
                    return;
                }

                double healthPercentage = livingEntity.getHealth() / livingEntity.getMaxHealth();
                if (healthPercentage <= 0.25 && phase < 4) {
                    phase = 4;
                    announcePhaseChange();
                } else if (healthPercentage <= 0.5 && phase < 3) {
                    phase = 3;
                    announcePhaseChange();
                } else if (healthPercentage <= 0.75 && phase < 2) {
                    phase = 2;
                    announcePhaseChange();
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);
    }

    private void announcePhaseChange() {
        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 25, 25, 25).forEach(entity -> {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                StringUtil.sendCenteredMessage(player, ChatColor.AQUA + "Frost-Wing enters phase " + phase + "!");
                StringUtil.sendCenteredMessage(player, ChatColor.GRAY + "The air becomes even colder!");
            }
        });
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
        livingEntity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, livingEntity.getLocation(), 10, 1, 1, 1, 0.1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().equals(livingEntity)) {
            if (isInvulnerable) {
                e.setCancelled(true);
                if (e.getDamager() instanceof Player) {
                    ((Player) e.getDamager()).sendMessage(ChatColor.AQUA + "Your attacks cannot penetrate Frost-Wing's icy shield!");
                }
            } else {
                // Chance to counterattack
                if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                    if (e.getDamager() instanceof Player) {
                        Player attacker = (Player) e.getDamager();
                        attacker.damage(e.getDamage() * 0.5, livingEntity);
                        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                        attacker.sendMessage(ChatColor.AQUA + "Frost-Wing's icy aura damages you!");
                        livingEntity.getWorld().spawnParticle(Particle.SNOW_SHOVEL, attacker.getLocation(), 50, 0.5, 1, 0.5, 0.1);
                    }
                }
            }
        }
    }
    public void setArmor() {
        try {
            livingEntity.getEquipment().setBoots(WorldBossDrops.createDrop(8, entityName));
            livingEntity.getEquipment().setLeggings(WorldBossDrops.createDrop(7, entityName));
            livingEntity.getEquipment().setChestplate(WorldBossDrops.createDrop(6, entityName));
            livingEntity.getEquipment().setHelmet(SkullTextures.FROST.getSkullByURL());
            int weaponType = BossGearGenerator.getWeaponType(entityName);
            livingEntity.getEquipment().setItemInMainHand(WorldBossDrops.createDrop(weaponType, entityName));
            Bukkit.getLogger().info("Armor set successfully for " + entityName);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error setting armor for " + entityName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
