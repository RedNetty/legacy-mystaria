package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.HashMapSorter;
import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGenerator;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.SkullTextures;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.BossGearGenerator;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.WorldBossDrops;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBoss;
import me.retrorealms.practiceserver.mechanics.money.GemPouches;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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


        new BukkitRunnable() {
            public void run() {
                try {
                    if (livingEntity.isDead()) {
                        minionList.forEach(livingEntity -> {
                            if (!livingEntity.isDead()) {
                                if (Spawners.mobs.containsKey(livingEntity)) Spawners.mobs.remove(livingEntity);
                                livingEntity.remove();
                            }
                        });
                        this.cancel();
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


                    bossMechanic((int) (livingEntity.getHealth() / livingEntity.getMaxHealth() * 100));
                    if ((livingEntity.getHealth() / livingEntity.getMaxHealth() * 100) < 23) {
                        if (!bezerk) activateBezerk();
                    }
                    if (bezerk) {
                        livingEntity.getWorld().spawnParticle(Particle.DRAGON_BREATH, livingEntity.getEyeLocation(), 3, 0.5, 0.8, 0.5, 0.1);

                    }
                } catch (Exception e) {

                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 20L, 20L);
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

                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 2L);
    }

    @Override
    public void rewardLoot() {
        ArrayList<Map.Entry<Player, Integer>> players = HashMapSorter.sortTopPlayers(getDamageDone());
        Player winner;
        Player second;
        Player third;
        int toSkip = 0;

        if (players.size() > 0 && players.get(0).getValue() > 40000) {
            winner = players.get(0).getKey();
            toSkip = 1;
        } else {
            winner = null;
        }
        if (players.size() > 1 && players.get(1).getValue() > 40000) {
            second = players.get(1).getKey();
            toSkip = 2;
        } else {
            second = null;
        }
        if (players.size() > 2 && players.get(2).getValue() > 40000) {
            third = players.get(2).getKey();
            toSkip = 3;
        } else {
            third = null;
        }

        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            StringUtil.sendCenteredMessage(player, "&e★☆✫ &3&l World-Boss &e✫☆★");
            StringUtil.sendCenteredMessage(player, "&bFrost-Wing &7has been defeated!");
            if (winner != null) {
                StringUtil.sendCenteredMessage(player, "&e* 1. " + winner.getName() + " &7- &c" + players.get(0).getValue() + "DMG");
            }
            if (second != null) {
                StringUtil.sendCenteredMessage(player, "&e* 2. " + second.getName() + " &7- &c" + players.get(1).getValue() + "DMG");
            }
            if (third != null) {
                StringUtil.sendCenteredMessage(player, "&e* 3. " + third.getName() + " &7- &c" + players.get(2).getValue() + "DMG");
            }
            player.sendMessage("");
        });


        getLoot(winner, 1);
        getLoot(second, 2);
        getLoot(third, 3);
        players.stream().skip(toSkip).forEach(p -> {
            if (p.getValue() > 10000) getLoot(p.getKey(), 4);
        });
    }

    public void getLoot(Player player, int place) {
        if (player != null) {
            List<ItemStack> lootDrops = new ArrayList<>();
            int armChance = 65;
            int wepChance = 65;
            int legOrbAmount = 2;
            int t5ProtAmount = 2;
            int bankNoteAmount = 5000;

            switch (place) {
                case 1:
                    armChance = 6;
                    wepChance = 9;
                    legOrbAmount = 15;
                    t5ProtAmount = 10;
                    bankNoteAmount = 35000;
                    break;
                case 2:
                    armChance = 8;
                    wepChance = 13;
                    legOrbAmount = 10;
                    t5ProtAmount = 7;
                    bankNoteAmount = 25000;
                    break;
                case 3:
                    armChance = 10;
                    wepChance = 15;
                    legOrbAmount = 8;
                    t5ProtAmount = 4;
                    bankNoteAmount = 25000;
                    break;
            }


            if (ThreadLocalRandom.current().nextInt(wepChance) == 2)
                lootDrops.add(livingEntity.getEquipment().getItemInMainHand());
            if (ThreadLocalRandom.current().nextInt(armChance) == 2)
                lootDrops.add(WorldBossDrops.createDrop(5, entityName));
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
                lootDrops.add(GemPouches.gemPouch(6).clone());
            }

            if (!lootDrops.isEmpty()) lootDrops.forEach(item -> {
                Mobdrops.dropShowString(player, item, null);
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), item);
                } else {
                    player.getInventory().addItem(item);
                }
            });
        }

    }

    public void doRandomAttack() {
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
                    case 4:
                        player.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Frost-Wing The Frozen Titan: " + ChatColor.YELLOW + "You will be buried under an avalanche of ice!");
                        break;

                }
            }
        });
        int ran = ThreadLocalRandom.current().nextInt(1, 4);
        switch (ran) {
            case 1:
                iceBlockBarrageAttack(livingEntity, 10, ThreadLocalRandom.current().nextInt(2500, 3000), 30, 25);
                break;
            case 2:
                icyGroundAttack(livingEntity.getLocation(), 9, ThreadLocalRandom.current().nextInt(300, 370), ThreadLocalRandom.current().nextInt(4, 5), 35, 5);
                break;
            case 3:
                summonMinionsAttack(livingEntity.getLocation(), ThreadLocalRandom.current().nextInt(6, 7));
                break;

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
            int elite = ThreadLocalRandom.current().nextInt(1, 4);

            boolean isElite = elite == 1 ? true : false;
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

                                                }
                                            }
                                        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }.runTaskLater(PracticeServer.getInstance(), 5L);
                        this.cancel();
                    }
                    tick++;
                    entity.getWorld().playEffect(entity.getLocation().clone().add(0, 2, 0), Effect.STEP_SOUND, Material.FROSTED_ICE);
                } catch (Exception e) {

                }
            }

        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 0L, 10L);
    }

    public void setArmor() {
        livingEntity.getEquipment().setBoots(WorldBossDrops.createDrop(8, "frostwing"));
        livingEntity.getEquipment().setLeggings(WorldBossDrops.createDrop(7, "frostwing"));
        livingEntity.getEquipment().setChestplate(WorldBossDrops.createDrop(6, "frostwing"));
        livingEntity.getEquipment().setHelmet(SkullTextures.FROST.getSkullByURL());
        livingEntity.getEquipment().setItemInMainHand(WorldBossDrops.createDrop(BossGearGenerator.getWeaponType("frostwing"), "frostwing"));
    }

}
