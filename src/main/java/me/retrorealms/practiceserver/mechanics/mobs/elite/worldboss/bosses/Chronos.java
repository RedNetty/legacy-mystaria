package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.SkullTextures;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.BossGearGenerator;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.WorldBossDrops;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBoss;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Chronos extends WorldBoss implements Listener {

    public BossEnum bossEnum = BossEnum.CHRONOS;
    private int timeSinceLastAttack = 0;
    private int attackFrequency = 20;
    private Location bossLocation;
    private List<Location> timeMirrors = new ArrayList<>();
    private Map<UUID, Long> playerTeleportCooldowns = new HashMap<>();
    private static final long TELEPORT_COOLDOWN = 5000; // 5 seconds cooldown

    private int phase = 1;
    private boolean isInvulnerable = false;
    private List<ArmorStand> timeAnchors = new ArrayList<>();
    private int timeAnchorDuration = 400;
    private int paradoxStormTicks = 0;
    private boolean paradoxStormActive = false;
    public Chronos() {
        super(BossEnum.CHRONOS);
    }
    @Override
    public WorldBoss spawnBoss(Location location) {
        this.livingEntity = Spawners.spawnMob(location, "chronos", 5, true);
        if (this.livingEntity == null) {
            Bukkit.getLogger().warning("Failed to spawn Chronos entity!");
            return null;
        }
        this.entityName = "chronos";
        this.tier = 5;
        this.bossLocation = location;
        this.bossEnum = BossEnum.CHRONOS;
        livingEntity.setMaxHealth(600000);
        livingEntity.setHealth(livingEntity.getMaxHealth());
        setArmor();
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
        startBossAI();
        startPhaseManager();
        announceSpawn();

        return this;
    }

    private void announceSpawn() {
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 1.0f, 0.5f);
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "The fabric of time trembles as I emerge!");
        livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
    }

    private void startBossAI() {
        new BukkitRunnable() {
            public void run() {
                try {
                    if (livingEntity == null || livingEntity.isDead()) {
                        cleanupBoss();
                        this.cancel();
                        return;
                    }

                    timeSinceLastAttack++;

                    if (timeSinceLastAttack >= attackFrequency) {
                        doRandomAttack();
                        timeSinceLastAttack = 0;
                    }

                    // Teleport if no players are nearby
                    if (ThreadLocalRandom.current().nextInt(1, 8) == 2 && livingEntity.getNearbyEntities(20, 20, 20).stream().noneMatch(e -> e instanceof Player)) {
                        teleportToNearestPlayer();
                    }

                    updateParadoxStorm();
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error in Chronos AI: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);
    }

    private void cleanupBoss() {
        timeMirrors.clear();
        timeAnchors.forEach(Entity::remove);
        timeAnchors.clear();
        paradoxStormActive = false;
    }

    private void teleportToNearestPlayer() {
        Optional<Entity> nearestPlayer = livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 50, 50, 50).stream()
                .filter(e -> e instanceof Player)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(livingEntity.getLocation())));

        nearestPlayer.ifPresent(player -> {
            Location teleportLocation = player.getLocation().add(ThreadLocalRandom.current().nextDouble(-5, 5), 0, ThreadLocalRandom.current().nextDouble(-5, 5));
            livingEntity.teleport(teleportLocation);
            sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Time bends to my will as I close in on my prey!");
            livingEntity.getWorld().playSound(teleportLocation, Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 0.5f);
            livingEntity.getWorld().spawnParticle(Particle.PORTAL, teleportLocation, 100, 1, 1, 1, 0.5);
        });
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
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Time marches on, and so does my power! Entering phase " + phase + "!");
        livingEntity.getWorld().strikeLightningEffect(livingEntity.getLocation());
        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        livingEntity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, livingEntity.getLocation(), 10, 1, 1, 1, 0.1);
    }

    private void doRandomAttack() {
        if (isInvulnerable) return;

        int attack = ThreadLocalRandom.current().nextInt(1, 7);
        switch (attack) {
            case 1:
                temporalEcho();
                break;
            case 2:
                chronoruptionField();
                break;
            case 3:
                timeMirrorMaze();
                break;
            case 4:
                paradoxStorm();
                break;
            case 5:
                temporalRift();
                break;
            case 6:
                timeStop();
                break;
        }
    }

    private void temporalEcho() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Brace yourselves, mortals! The echoes of time approach!");

        // Indicator before the attack
        new BukkitRunnable() {
            int duration = 0;

            public void run() {
                if (duration >= 50) {
                    this.cancel();
                    startTemporalEcho();
                    return;
                }

                livingEntity.getWorld().spawnParticle(Particle.SMOKE_LARGE, livingEntity.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.1);
                livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_NOTE_BASEDRUM, 1.0f, 1.5f);

                duration++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 5L);
    }

    private void startTemporalEcho() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Witness the echoes of time!");
        List<ArmorStand> echoes = new ArrayList<>();
        for (int i = 0; i < 5 + phase; i++) {
            ArmorStand echo = (ArmorStand) livingEntity.getWorld().spawnEntity(livingEntity.getLocation(), EntityType.ARMOR_STAND);
            echo.setVisible(false);
            echo.setGravity(false);
            echo.setHelmet(SkullTextures.VOID.getSkullByURL());
            echoes.add(echo);
        }

        new BukkitRunnable() {
            int duration = 0;
            double angle = 0;

            public void run() {
                if (duration >= 60 || livingEntity.isDead()) {
                    echoes.forEach(Entity::remove);
                    this.cancel();
                    return;
                }
                angle += Math.PI / 25;
                echoes.forEach(echo -> {
                    double radius = 8 * Math.sin(angle);
                    double height = 3 * Math.cos(angle);
                    Location newLoc = livingEntity.getLocation().add(
                            radius * Math.cos(angle + echoes.indexOf(echo) * (2 * Math.PI / echoes.size())),
                            height,
                            radius * Math.sin(angle + echoes.indexOf(echo) * (2 * Math.PI / echoes.size()))
                    );
                    if (!echo.hasMetadata("NPC")) echo.teleport(newLoc);
                    echo.getWorld().spawnParticle(Particle.PORTAL, echo.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
                    echo.getWorld().spawnParticle(Particle.DRAGON_BREATH, echo.getLocation(), 5, 0.2, 0.2, 0.2, 0.05);
                    echo.getWorld().getNearbyEntities(echo.getLocation(), 2, 2, 2).stream()
                            .filter(e -> e instanceof Player)
                            .forEach(e -> {
                                Player player = (Player) e;
                                if (canDamagePlayer(player)) { // Added cooldown check
                                    player.damage(1500 * phase);
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
                                }
                            });
                });
                duration++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }

    private void chronoruptionField() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Your timeline is mine to manipulate!");
        new BukkitRunnable() {
            int duration = 0;
            double radius = 5;

            public void run() {
                if (duration >= 100 || livingEntity.isDead()) {
                    this.cancel();
                    return;
                }
                radius += 0.5;
                for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 16) {
                    Location particleLoc = livingEntity.getLocation().add(
                            radius * Math.cos(angle),
                            1,
                            radius * Math.sin(angle)
                    );
                    livingEntity.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, particleLoc, 5, 0.1, 0.1, 0.1, 0.1);
                }
                livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), radius, radius, radius).stream()
                        .filter(e -> e instanceof Player)
                        .forEach(e -> {
                            Player player = (Player) e;
                            if (canTeleport(player)) {
                                Location newLoc = findSafeLocation(player.getLocation(), 15);
                                if (newLoc != null) {
                                    Location oldLoc = player.getLocation().clone();
                                    if (!player.hasMetadata("NPC")) player.teleport(newLoc);
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                                    player.sendMessage(ChatColor.LIGHT_PURPLE + "You've been displaced in time!");
                                    oldLoc.getWorld().spawnParticle(Particle.PORTAL, oldLoc, 50, 0.5, 1, 0.5, 0.1);
                                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 0.5f);
                                    setTeleportCooldown(player);
                                }
                            }
                        });
                duration++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 2L);
    }

    private void timeMirrorMaze() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Navigate the mirrors of time... if you can!");
        timeMirrors.clear();
        for (int i = 0; i < 15 + (phase * 2); i++) {
            Location mirrorLoc = findSafeLocation(livingEntity.getLocation(), 20);
            if (mirrorLoc != null) {
                timeMirrors.add(mirrorLoc);
            }
        }
        new BukkitRunnable() {
            int duration = 0;

            public void run() {
                if (duration >= 120 || livingEntity.isDead()) {
                    this.cancel();
                    return;
                }
                timeMirrors.forEach(loc -> {
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.5, 0.5, 0.5, 0.05);
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc, 10, 0.3, 0.3, 0.3, 0.05);
                    loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5).stream()
                            .filter(e -> e instanceof Player || e instanceof Projectile)
                            .forEach(e -> {
                                if (e instanceof Player) {
                                    Player player = (Player) e;
                                    if (canTeleport(player)) {
                                        Location newLoc = findSafeLocation(player.getLocation(), 20);
                                        if (newLoc != null) {
                                            player.teleport(newLoc);
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.0f);
                                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
                                            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
                                            setTeleportCooldown(player);
                                        }
                                    }

                                } else if (e instanceof Projectile) {
                                    Vector velocity = e.getVelocity();
                                    // Reverse the direction and add a random sideways component
                                    velocity.multiply(-1.5);
                                    velocity.add(new Vector(
                                            ThreadLocalRandom.current().nextDouble(-0.5, 0.5),
                                            0,
                                            ThreadLocalRandom.current().nextDouble(-0.5, 0.5)
                                    ));
                                    e.setVelocity(velocity);
                                }
                            });
                });
                duration++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 2L);
    }

    private void paradoxStorm() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Face the fury of temporal paradoxes!");
        paradoxStormActive = true;
        paradoxStormTicks = 0;

        new BukkitRunnable() {
            @Override
            public void run() {
                paradoxStormActive = false;
                paradoxStormTicks = 0;
                sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "The storm subsides... for now.");
            }
        }.runTaskLater(PracticeServer.getInstance(), 600L); // 30 seconds (20 ticks per second)
    }

    private void updateParadoxStorm() {
        if (paradoxStormActive) {
            paradoxStormTicks++;
            if (paradoxStormTicks >= 600) { // 30 seconds
                paradoxStormActive = false;
                paradoxStormTicks = 0;
                sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "The storm of paradoxes has ended!");
            } else {
                for (int i = 0; i < 7 + phase; i++) {
                    Location paradoxLoc = livingEntity.getLocation().add(
                            ThreadLocalRandom.current().nextDouble(-25, 25),
                            ThreadLocalRandom.current().nextDouble(0, 12),
                            ThreadLocalRandom.current().nextDouble(-25, 25)
                    );
                    paradoxLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, paradoxLoc, 75, 1.5, 1.5, 1.5, 0.1);
                    paradoxLoc.getWorld().spawnParticle(Particle.SPELL_WITCH, paradoxLoc, 30, 1, 1, 1, 0.05);
                    paradoxLoc.getWorld().playSound(paradoxLoc, Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 2.0f);
                    paradoxLoc.getWorld().playSound(paradoxLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
                    paradoxLoc.getWorld().getNearbyEntities(paradoxLoc, 4, 4, 4).stream()
                            .filter(e -> e instanceof Player)
                            .forEach(e -> {
                                Player player = (Player) e;
                                if (canDamagePlayer(player)) { // Added cooldown check
                                    player.damage(1500 * phase);
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 120, 1));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
                                    Vector knockback = player.getLocation().toVector().subtract(paradoxLoc.toVector()).normalize().multiply(2.5);
                                    player.setVelocity(knockback);
                                    PracticeServer.antiCheat.getPlayerData(player).setLastAttackTime(System.currentTimeMillis());
                                }
                            });
                }
            }
        }
    }

    private void temporalRift() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Behold the power of the Temporal Rift!");
        isInvulnerable = true;
        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));

        Location bossLoc = livingEntity.getLocation();
        int maxAnchors = 2 + phase;
        int radius = 8;
        int heightDifference = 3;

        List<Location> potentialLocations = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location testLoc = bossLoc.clone().add(x, 0, z);
                testLoc.setY(testLoc.getWorld().getHighestBlockYAt(testLoc));

                if (isValidAnchorLocation(testLoc, bossLoc, heightDifference)) {
                    potentialLocations.add(testLoc.add(0, 1, 0));
                }
            }
        }

        Collections.shuffle(potentialLocations);

        for (int i = 0; i < Math.min(maxAnchors, potentialLocations.size()); i++) {
            Location anchorLoc = potentialLocations.get(i);
            ArmorStand anchor = (ArmorStand) livingEntity.getWorld().spawnEntity(anchorLoc, EntityType.ARMOR_STAND);
            setupAnchor(anchor);
            timeAnchors.add(anchor);

            // Visual effect for anchor spawn
            anchorLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, anchorLoc, 1);
            anchorLoc.getWorld().playSound(anchorLoc, Sound.ENTITY_LIGHTNING_IMPACT, 1.0f, 2.0f);
        }

        if (timeAnchors.isEmpty()) {
            sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "The Temporal Rift struggles to open in this area!");
            isInvulnerable = false;
        } else {
            sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.RED + "Destroy the Time Anchors to damage me!");
            sendLocalMessage(ChatColor.YELLOW + "Time Anchors have appeared! Destroy them to weaken Chronos!");
            startTemporalRiftEffect();
        }
    }

    private boolean isValidAnchorLocation(Location loc, Location bossLoc, int heightDifference) {
        return loc.getBlock().getType().isSolid() &&
                loc.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                loc.clone().add(0, 2, 0).getBlock().getType() == Material.AIR &&
                Math.abs(loc.getY() - bossLoc.getY()) <= heightDifference &&
                loc.distanceSquared(bossLoc) >= 25; // At least 5 blocks away
    }

    private void setupAnchor(ArmorStand anchor) {
        anchor.setGravity(false);
        anchor.setVisible(true);
        anchor.setCustomName(ChatColor.GOLD + "Time Anchor (Right-Click)");
        anchor.setCustomNameVisible(true);
        anchor.setSmall(false); // Set the anchor to normal size
        anchor.setArms(true); // Give the anchor arms
        anchor.setBasePlate(false); // Remove the base plate

        // Set the anchor's equipment
        anchor.getEquipment().setHelmet(new ItemStack(Material.END_CRYSTAL));
        anchor.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        anchor.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        anchor.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        anchor.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
    }

    private void startTemporalRiftEffect() {
        new BukkitRunnable() {
            int duration = 0;

            @Override
            public void run() {
                if (duration >= timeAnchorDuration || livingEntity.isDead() || timeAnchors.isEmpty()) {
                    endTemporalRift();
                    this.cancel();
                    return;
                }

                timeAnchors.removeIf(Entity::isDead);
                timeAnchors.forEach(this::anchorEffect);
                playerEffect();
                duration++;
            }

            private void anchorEffect(ArmorStand anchor) {
                anchor.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, anchor.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                anchor.getWorld().spawnParticle(Particle.PORTAL, anchor.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.05);
                anchor.getWorld().getNearbyEntities(anchor.getLocation(), 2, 2, 2).stream()
                        .filter(e -> e instanceof Player && canDamagePlayer((Player) e))
                        .forEach(e -> ((Player) e).damage(200 * phase));
            }

            private void playerEffect() {
                livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 30, 30, 30).stream()
                        .filter(e -> e instanceof Player)
                        .forEach(e -> {
                            Player player = (Player) e;
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
                        });
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }

    private void endTemporalRift() {
        isInvulnerable = false;
        timeAnchors.forEach(Entity::remove);
        timeAnchors.clear();
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "The Temporal Rift closes!");
    }

    private void timeStop() {
        sendLocalMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.YELLOW + "Time shall stand still!");
        new BukkitRunnable() {
            int duration = 0;
            double radius = 25;

            public void run() {
                if (duration >= 40 || livingEntity.isDead()) {
                    this.cancel();
                    return;
                }
                for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 32) {
                    Location particleLoc = livingEntity.getLocation().add(
                            radius * Math.cos(angle),
                            0,
                            radius * Math.sin(angle)
                    );
                    livingEntity.getWorld().spawnParticle(Particle.SNOW_SHOVEL, particleLoc, 5, 0.1, 1, 0.1, 0);
                    livingEntity.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }
                livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), radius, radius, radius).stream()
                        .filter(e -> e instanceof Player)
                        .forEach(e -> {
                            Player player = (Player) e;
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 5));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 128));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 2));
                            player.getWorld().spawnParticle(Particle.SNOW_SHOVEL, player.getLocation(), 20, 0.5, 1, 0.5, 0);
                            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.3, 0.3, 0);
                            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
                        });
                duration++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 2L);
    }

    private Location findSafeLocation(Location center, int radius) {
        for (int i = 0; i < 20; i++) {  // Try up to 20 times
            Location randomLoc = center.clone().add(
                    ThreadLocalRandom.current().nextDouble(-radius, radius),
                    0,
                    ThreadLocalRandom.current().nextDouble(-radius, radius)
            );
            randomLoc.setY(randomLoc.getWorld().getHighestBlockYAt(randomLoc));

            if (isSafeLocation(randomLoc)) {
                return randomLoc;
            }
        }
        return null;  // If no safe location found
    }

    private boolean isSafeLocation(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block ground = loc.clone().add(0, -1, 0).getBlock();

        return !feet.getType().isSolid() &&
                !head.getType().isSolid() &&
                ground.getType().isSolid() &&
                !ground.isLiquid();
    }

    private void sendLocalMessage(String message) {
        livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), 30, 30, 30).stream()
                .filter(e -> e instanceof Player)
                .forEach(e -> ((Player) e).sendMessage(message));
    }

    private boolean canTeleport(Player player) {
        return !playerTeleportCooldowns.containsKey(player.getUniqueId()) ||
                System.currentTimeMillis() - playerTeleportCooldowns.get(player.getUniqueId()) > TELEPORT_COOLDOWN;
    }

    private void setTeleportCooldown(Player player) {
        playerTeleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity().equals(livingEntity)) {
            if (isInvulnerable) {
                ((Player) e.getDamager()).sendMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Chronos, Lord of Time: " + ChatColor.RED + "You must destroy the Time Anchors to damage me!");
                e.setDamage(0);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnchorDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ArmorStand) {
            ArmorStand anchor = (ArmorStand) e.getEntity();
            if (timeAnchors.contains(anchor)) {
                if (e.getDamager() instanceof Player) {
                    Player player = (Player) e.getDamager();
                    destroyAnchor(anchor, player);
                    e.setCancelled(true);
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onAnchorInteract(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            ArmorStand anchor = (ArmorStand) e.getRightClicked();
            if (timeAnchors.contains(anchor)) {
                Player player = e.getPlayer();
                destroyAnchor(anchor, player);
                e.setCancelled(true);
            }
        }
    }

    private void destroyAnchor(ArmorStand anchor, Player player) {
        // Visual effect for anchor destruction
        anchor.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, anchor.getLocation(), 1);
        anchor.getWorld().playSound(anchor.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        anchor.getEquipment().clear();
        anchor.remove();
        timeAnchors.remove(anchor);

        player.sendMessage(ChatColor.GREEN + "You've destroyed a Time Anchor!");

        if (timeAnchors.isEmpty()) {
            endTemporalRift();
            livingEntity.damage(livingEntity.getMaxHealth() * 0.1 * phase);
            livingEntity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, livingEntity.getLocation().add(0, 2, 0), 50, 0.5, 0.5, 0.5, 0.1);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1.0f, 0.5f);
        }
    }

    public void setArmor() {
        try {
            livingEntity.getEquipment().setBoots(WorldBossDrops.createDrop(8, entityName));
            livingEntity.getEquipment().setLeggings(WorldBossDrops.createDrop(7, entityName));
            livingEntity.getEquipment().setChestplate(WorldBossDrops.createDrop(6, entityName));
            livingEntity.getEquipment().setHelmet(SkullTextures.VOID.getSkullByURL());
            int weaponType = BossGearGenerator.getWeaponType(entityName);
            livingEntity.getEquipment().setItemInMainHand(WorldBossDrops.createDrop(weaponType, entityName));
            Bukkit.getLogger().info("Armor set successfully for " + entityName);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error setting armor for " + entityName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}