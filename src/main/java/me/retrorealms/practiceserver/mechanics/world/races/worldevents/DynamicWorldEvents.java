package me.retrorealms.practiceserver.mechanics.world.races.worldevents;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.loot.LootChests;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.world.races.ElementalArtifacts;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicWorldEvents implements Listener {
    private static final int EVENT_INTERVAL = 15 * 60 * 20; // 15 minutes
    private static final int EVENT_DURATION = 5 * 60 * 20; // 5 minutes
    private static boolean eventActive = false;

    private static Map<UUID, Integer> playerKills = new HashMap<>();
    public static void startEventScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!eventActive) {
                    triggerRandomEvent();
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), EVENT_INTERVAL, EVENT_INTERVAL);
    }

    private static void triggerRandomEvent() {
        World world = Bukkit.getWorld("jew");
        List<Player> players = world.getPlayers();

        if (players.isEmpty()) return;

        List<Runnable> events = Arrays.asList(
                () -> eliteMobInvasion(world, players),
                () -> treasureHunt(world, players),
                () -> elementalStorm(world, players),
                () -> bossBattle(world, players)
        );

        events.get(new Random().nextInt(events.size())).run();
        eventActive = true;
    }

    private static void eliteMobInvasion(World world, List<Player> players) {
        Bukkit.broadcastMessage(ChatColor.RED + "Elite mobs are invading! Defeat them for extra rewards!");

        Map<UUID, Integer> playerKills = new HashMap<>();

        new BukkitRunnable() {
            int remainingTime = EVENT_DURATION;
            int mobsSpawned = 0;
            int maxMobs = players.size() * 5;

            @Override
            public void run() {
                if (remainingTime <= 0 || mobsSpawned >= maxMobs) {
                    endEliteMobInvasion(playerKills);
                    this.cancel();
                    return;
                }

                for (Player player : players) {
                    if (new Random().nextInt(100) < 20) {
                        Location spawnLoc = findSafeSpawnLocation(player.getLocation(), 10, 20);
                        if (spawnLoc != null) {
                            LivingEntity eliteMob = Spawners.spawnMob(spawnLoc, "zombie", 5, true);
                            eliteMob.setCustomName(ChatColor.RED + "Invading Elite");
                            eliteMob.setCustomNameVisible(true);

                            eliteMob.setMetadata("invasionMob", new FixedMetadataValue(PracticeServer.getInstance(), true));

                            spawnEliteMobEffects(eliteMob);
                            mobsSpawned++;
                        }
                    }
                }

                remainingTime -= 100;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 100L);
    }

    private static void endEliteMobInvasion(Map<UUID, Integer> playerKills) {
        Bukkit.broadcastMessage(ChatColor.GREEN + "The elite mob invasion has ended!");

        List<Map.Entry<UUID, Integer>> sortedKills = playerKills.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        for (int i = 0; i < sortedKills.size(); i++) {
            Player player = Bukkit.getPlayer(sortedKills.get(i).getKey());
            if (player != null) {
                int kills = sortedKills.get(i).getValue();
                ItemStack reward = CreateDrop.createDrop(5, new Random().nextInt(4) + 1, 4);
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.GOLD + "You placed #" + (i + 1) + " in the invasion with " + kills + " kills! Enjoy your reward!");
            }
        }

        eventActive = false;
    }

    private static void treasureHunt(World world, List<Player> players) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "A treasure hunt has begun! Find and loot special chests!");

        int numChests = Math.max(5, players.size() / 2);
        List<Location> chestLocations = new ArrayList<>();
        Map<UUID, Integer> playerFinds = new HashMap<>();

        for (int i = 0; i < numChests; i++) {
            Location chestLoc = findSafeSpawnLocation(world.getSpawnLocation(), 100, 1000);
            if (chestLoc != null) {
                LootChests.createSpecialLootChest(chestLoc, 5);
                chestLocations.add(chestLoc);

                spawnTreasureChestEffects(chestLoc);
            }
        }

        new BukkitRunnable() {
            int remainingTime = EVENT_DURATION;

            @Override
            public void run() {
                if (remainingTime <= 0 || chestLocations.isEmpty()) {
                    endTreasureHunt(playerFinds);
                    this.cancel();
                    return;
                }

                remainingTime -= 1200;
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Treasure hunt ends in " + (remainingTime / 1200) + " minutes!");
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1200L);
    }

    private static void endTreasureHunt(Map<UUID, Integer> playerFinds) {
        Bukkit.broadcastMessage(ChatColor.GREEN + "The treasure hunt has ended!");

        List<Map.Entry<UUID, Integer>> sortedFinds = playerFinds.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        for (int i = 0; i < sortedFinds.size(); i++) {
            Player player = Bukkit.getPlayer(sortedFinds.get(i).getKey());
            if (player != null) {
                int finds = sortedFinds.get(i).getValue();
                ItemStack reward = CreateDrop.createDrop(5, new Random().nextInt(4) + 1, 4);
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.GOLD + "You placed #" + (i + 1) + " in the treasure hunt with " + finds + " chests found! Enjoy your reward!");
            }
        }

        eventActive = false;
    }

    private static void elementalStorm(World world, List<Player> players) {
        ElementalArtifacts.ElementType element = ElementalArtifacts.ElementType.values()[new Random().nextInt(ElementalArtifacts.ElementType.values().length)];
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "An elemental storm of " + element + " is raging! Elemental artifacts have spawned!");

        int numArtifacts = Math.max(3, players.size() / 3);
        List<Location> artifactLocations = new ArrayList<>();

        for (int i = 0; i < numArtifacts; i++) {
            Location artifactLoc = findSafeSpawnLocation(world.getSpawnLocation(), 100, 1000);
            if (artifactLoc != null) {
                ItemStack artifact = ElementalArtifacts.createElementalArtifact(5, new Random().nextInt(4) + 1, element);
                world.dropItemNaturally(artifactLoc, artifact);
                artifactLocations.add(artifactLoc);

                spawnElementalArtifactEffects(artifactLoc, element);
            }
        }

        new BukkitRunnable() {
            int remainingTime = EVENT_DURATION;

            @Override
            public void run() {
                if (remainingTime <= 0) {
                    endElementalStorm();
                    this.cancel();
                    return;
                }

                for (Player player : players) {
                    applyStormEffect(player, element);
                }

                remainingTime -= 100;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 100L);
    }

    private static void endElementalStorm() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "The elemental storm has subsided!");
        eventActive = false;
    }

    private static void bossBattle(World world, List<Player> players) {
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "A powerful boss has appeared! Work together to defeat it!");

        Location bossLoc = findSafeSpawnLocation(world.getSpawnLocation(), 50, 200);
        if (bossLoc == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "Failed to spawn the boss. Event cancelled.");
            eventActive = false;
            return;
        }

        LivingEntity boss = Spawners.spawnMob(bossLoc, "giant", 6, true);
        boss.setCustomName(ChatColor.DARK_RED + "Elemental Titan");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(10000);
        boss.setHealth(10000);

        spawnBossEffects(boss);

        new BukkitRunnable() {
            int remainingTime = EVENT_DURATION;

            @Override
            public void run() {
                if (remainingTime <= 0 || boss.isDead()) {
                    endBossBattle(boss, players);
                    this.cancel();
                    return;
                }

                if (remainingTime % 600 == 0) { // Every 30 seconds
                    performBossAbility(boss, players);
                }

                remainingTime -= 20;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
    }

    private static void endBossBattle(LivingEntity boss, List<Player> players) {
        if (boss.isDead()) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "The Elemental Titan has been defeated!");

            // Reward all participating players
            for (Player player : players) {
                if (player.getLocation().distance(boss.getLocation()) <= 50) {
                    ItemStack reward = CreateDrop.createDrop(5, new Random().nextInt(4) + 1, 4);
                    player.getInventory().addItem(reward);
                    player.sendMessage(ChatColor.GOLD + "You've received a reward for defeating the Elemental Titan!");
                }
            }
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "The Elemental Titan has escaped! The battle is over.");
            boss.remove();
        }

        eventActive = false;
    }

    private static void performBossAbility(LivingEntity boss, List<Player> players) {
        int ability = new Random().nextInt(4);
        switch (ability) {
            case 0: // Fire Nova
                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                for (Player player : players) {
                    if (player.getLocation().distance(boss.getLocation()) <= 10) {
                        player.damage(50, boss);
                        player.setFireTicks(100);
                    }
                }
                break;
            case 1: // Ice Prison
                for (Player player : players) {
                    if (player.getLocation().distance(boss.getLocation()) <= 15) {
                        Location playerLoc = player.getLocation();
                        for (int x = -1; x <= 1; x++) {
                            for (int y = 0; y <= 2; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x != 0 || y != 1 || z != 0) {
                                        playerLoc.clone().add(x, y, z).getBlock().setType(Material.ICE);
                                    }
                                }
                            }
                        }
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 4));
                    }
                }
                break;
            case 2: // Lightning Strike
                for (Player player : players) {
                    if (player.getLocation().distance(boss.getLocation()) <= 20) {
                        player.getWorld().strikeLightning(player.getLocation());
                    }
                }
                break;
            case 3: // Earthquake
                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                for (Player player : players) {
                    if (player.getLocation().distance(boss.getLocation()) <= 15) {
                        player.damage(30, boss);
                        player.setVelocity(player.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize().multiply(2).setY(1));
                    }
                }
                break;
        }
    }

    private static Location findSafeSpawnLocation(Location center, int minRadius, int maxRadius) {
        World world = center.getWorld();
        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
            int x = (int) (center.getX() + radius * Math.cos(angle));
            int z = (int) (center.getZ() + radius * Math.sin(angle));
            int y = world.getHighestBlockYAt(x, z);

            Location loc = new Location(world, x, y, z);
            if (isSafeLocation(loc) && !Alignments.isSafeZone(loc) && isWithinWorldBorder(loc)) {
                return loc;
            }
        }

        return null; // Return null if no safe location found
    }

    private static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block ground = feet.getRelative(BlockFace.DOWN);

        return !feet.getType().isSolid() && !head.getType().isSolid() && ground.getType().isSolid();
    }

    private static boolean isWithinWorldBorder(Location location) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        double size = worldBorder.getSize() / 2;
        Location center = worldBorder.getCenter();
        double x = location.getX() - center.getX(), z = location.getZ() - center.getZ();
        return Math.abs(x) <= size && Math.abs(z) <= size;
    }

    private static void spawnEliteMobEffects(LivingEntity eliteMob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteMob.isDead() || !eliteMob.isValid()) {
                    this.cancel();
                    return;
                }
                eliteMob.getWorld().spawnParticle(Particle.DRAGON_BREATH, eliteMob.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
    }

    private static void spawnTreasureChestEffects(Location chestLoc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (chestLoc.getBlock().getType() != Material.GLOWSTONE) {
                    this.cancel();
                    return;
                }
                chestLoc.getWorld().spawnParticle(Particle.TOTEM, chestLoc.clone().add(0.5, 1.5, 0.5), 20, 0.3, 0.3, 0.3, 0.05);
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 10L);
    }

    private static void spawnElementalArtifactEffects(Location artifactLoc, ElementalArtifacts.ElementType element) {
        Particle particleType = getElementParticle(element);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (artifactLoc.getBlock().getType() == Material.AIR) {
                    this.cancel();
                    return;
                }
                artifactLoc.getWorld().spawnParticle(particleType, artifactLoc.clone().add(0.5, 0.5, 0.5), 30, 0.3, 0.3, 0.3, 0.05);
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 10L);
    }

    private static Particle getElementParticle(ElementalArtifacts.ElementType element) {
        switch (element) {
            case FIRE: return Particle.FLAME;
            case ICE: return Particle.SNOW_SHOVEL;
            case LIGHTNING: return Particle.END_ROD;
            case EARTH: return Particle.VILLAGER_HAPPY;
            default: return Particle.SPELL_MOB;
        }
    }

    private static void applyStormEffect(Player player, ElementalArtifacts.ElementType element) {
        switch (element) {
            case FIRE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1));
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                break;
            case ICE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1));
                player.getWorld().spawnParticle(Particle.SNOW_SHOVEL, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                break;
            case LIGHTNING:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                break;
            case EARTH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                break;
        }
    }

    private static void spawnBossEffects(LivingEntity boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    this.cancel();
                    return;
                }
                boss.getWorld().spawnParticle(Particle.SPELL_WITCH, boss.getLocation().add(0, 2, 0), 50, 1, 2, 1, 0.1);
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 5L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (eventActive) {
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.YELLOW + "A world event is currently active! Check your surroundings!");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof LivingEntity) {
            Player player = (Player) event.getEntity();
            LivingEntity damager = (LivingEntity) event.getDamager();

            if (damager.hasMetadata("invasionMob")) {
                // Increase damage from invasion mobs
                event.setDamage(event.getDamage() * 1.5);
            }

            if (damager.getCustomName() != null && damager.getCustomName().contains("Elemental Titan")) {
                // Boss damage handling
                handleBossDamage(player, event);
            }
        }
    }

    private void handleBossDamage(Player player, EntityDamageByEntityEvent event) {
        double damage = event.getDamage();

        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            damage *= 0.7;
        }

        if (ElementalArtifacts.playerHasArtifact(player)) {
            ItemStack artifact = ElementalArtifacts.getPlayerArtifact(player);
            ElementalArtifacts.ElementType artifactType = ElementalArtifacts.getElementFromItem(artifact);

            switch (artifactType) {
                case FIRE:
                    damage *= 0.8;
                    player.setFireTicks(0); // Immunity to fire
                    break;
                case ICE:
                    damage *= 0.9;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0)); // Slow the boss
                    break;
                case LIGHTNING:
                    damage *= 0.85;
                    event.getDamager().getWorld().strikeLightning(event.getDamager().getLocation()); // Strike the boss with lightning
                    break;
                case EARTH:
                    damage *= 0.75;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1)); // Regeneration
                    break;
            }
        }

        event.setDamage(damage);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (eventActive) {
            Player player = event.getPlayer();
            Location to = event.getTo();

            // Apply effects when players enter certain areas during events
            if (to.getBlock().getBiome() == Biome.DESERT && me.retrorealms.practiceserver.mechanics.world.races.ElementalArtifacts.getCurrentElement() == me.retrorealms.practiceserver.mechanics.world.races.ElementalArtifacts.ElementType.FIRE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.GLOWSTONE) {
            Location clickedLoc = event.getClickedBlock().getLocation();
            if (LootChests.opened.containsKey(clickedLoc)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                Inventory chestInventory = LootChests.opened.get(clickedLoc);
                player.openInventory(chestInventory);

                // Remove the chest after it's opened
                clickedLoc.getBlock().setType(Material.AIR);
                LootChests.opened.remove(clickedLoc);
                LootChests.loot.remove(clickedLoc);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("invasionMob") && event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            UUID killerId = killer.getUniqueId();
            // Update kill count for the player
            // This assumes you have a Map<UUID, Integer> playerKills defined somewhere in your class
            playerKills.put(killerId, playerKills.getOrDefault(killerId, 0) + 1);
        }
    }

    public static void initializeEventSystem() {
        startEventScheduler();
        Bukkit.getPluginManager().registerEvents(new DynamicWorldEvents(), PracticeServer.getInstance());
        Bukkit.getLogger().info("Dynamic World Events system initialized.");
    }
}