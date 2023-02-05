package me.retrorealms.practiceserver.mechanics.mobs;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.commands.moderation.ToggleMobsCommand;
import me.retrorealms.practiceserver.mechanics.drops.Drops;
import me.retrorealms.practiceserver.mechanics.mobs.elite.GolemElite;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.Util;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Spawners implements Listener {

    public static ConcurrentHashMap<Location, String> spawners;
    public static ConcurrentHashMap<LivingEntity, Location> mobs;
    public static ConcurrentHashMap<Location, Long> respawntimer;
    private static Spawners instance;

    static {
        Spawners.spawners = new ConcurrentHashMap<Location, String>();
        Spawners.mobs = new ConcurrentHashMap<LivingEntity, Location>();
        Spawners.respawntimer = new ConcurrentHashMap<Location, Long>();
    }

    HashMap<String, Location> creatingspawner;

    public Spawners() {
        instance = this;

        CreatureSpawnEvent.SpawnReason spawnReason;
        this.creatingspawner = new HashMap<String, Location>();
    }

    public static ConcurrentHashMap<LivingEntity, Location> getMobs() {
        return mobs;
    }

    public static Spawners getInstance() {
        return instance;
    }

    static boolean isPlayerNearby(final Location loc) {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() == loc.getWorld()) {
                if (p.getLocation().distanceSquared(loc) < 640.0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getHp(final ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            final List<String> lore = is.getItemMeta().getLore();
            if (lore.size() > 1 && lore.get(1).contains("HP")) {
                try {
                    return Integer.parseInt(lore.get(1).split(": +")[1]);
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static boolean isCorrectFormat(final String data) {
        if (!data.contains(":") || !data.contains("@") || !data.contains("#")) {
            return false;
        }
        if (data.contains(",")) {
            String[] split;
            for (int length = (split = data.split(",")).length, i = 0; i < length; ++i) {
                final String s = split[i];
                if (!s.contains(":") || !s.contains("@") || !s.contains("#")) {
                    return false;
                }
                final String type = s.split(":")[0];
                if (!type.equalsIgnoreCase("skeleton") && !type.equalsIgnoreCase("zombie")
                        && !type.equalsIgnoreCase("silverfish") && !type.equalsIgnoreCase("magmacube")
                        && !type.equalsIgnoreCase("spider") && !type.equalsIgnoreCase("cavespider")
                        && !type.equalsIgnoreCase("imp") && !type.equalsIgnoreCase("witherskeleton")
                        && !type.equalsIgnoreCase("daemon") && !type.equalsIgnoreCase("golem")
                        && !type.equalsIgnoreCase("wither") && !type.equalsIgnoreCase("giant")
                        && !type.equalsIgnoreCase("mitsuki") && !type.equalsIgnoreCase("copjak")
                        && !type.equalsIgnoreCase("kingofgreed") && !type.equalsIgnoreCase("skeletonking")
                        && !type.equalsIgnoreCase("impa") && !type.equalsIgnoreCase("bloodbutcher")
                        && !type.equalsIgnoreCase("spectralGuard") && !type.equalsIgnoreCase("blayshan")
                        && !type.equalsIgnoreCase("watchMaster") && !type.equalsIgnoreCase("jayden")
                        && !type.equalsIgnoreCase("kilatan") && !type.equalsIgnoreCase("bossSkeletonDungeon")
                        && !type.equalsIgnoreCase("weakSkeletonEntity") && !type.equalsIgnoreCase("krampus")
                        && !type.equalsIgnoreCase("spectralKnight") && !type.equalsIgnoreCase("spiderQueen")
                        && !type.equalsIgnoreCase("frostKing") && !type.equalsIgnoreCase("duranor")
                        && !type.equalsIgnoreCase("risk_Elite") && !type.equalsIgnoreCase("warden")
                        && !type.equalsIgnoreCase("frozenElite") && !type.equalsIgnoreCase("frozenBoss")
                        && !type.equalsIgnoreCase("frozenGolem") && !type.equalsIgnoreCase("orcKing")) {
                    return false;
                }
                try {
                    final int tier = Integer.parseInt(s.split(":")[1].split("@")[0]);
                    if (tier < 1 || tier > 6) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
                final String elite = s.split("@")[1].split("#")[0];
                if (!elite.equalsIgnoreCase("true") && !elite.equalsIgnoreCase("false")) {
                    return false;
                }
                try {
                    final int amt = Integer.parseInt(s.split("#")[1]);
                    if (amt < 1 || amt > 10) {
                        return false;
                    }
                } catch (Exception e2) {
                    return false;
                }
                final int tier2 = Integer.parseInt(data.split(":")[1].split("@")[0]);
                final boolean iselite = Boolean.parseBoolean(elite);
                if ((type.equalsIgnoreCase("mitsuki") && (!iselite || tier2 != 1))
                        || (type.equalsIgnoreCase("copjak") && (!iselite || tier2 != 2)
                        || (type.equalsIgnoreCase("risk_Elite") && (!iselite || tier2 != 2))
                        || (type.equalsIgnoreCase("orcKing") && (!iselite || tier2 != 2))
                        || (type.equalsIgnoreCase("kingofgreed") && (!iselite || tier2 != 3))
                        || (type.equalsIgnoreCase("skeletonking") && (!iselite || tier2 != 3))
                        || (type.equalsIgnoreCase("impa") && (!iselite || tier2 != 3))
                        || (type.equalsIgnoreCase("bloodbutcher") && (!iselite || tier2 != 4))
                        || (type.equalsIgnoreCase("blayshan") && (!iselite || tier2 != 4))
                        || (type.equalsIgnoreCase("watchMaster") && (!iselite || tier2 != 4))
                        || (type.equalsIgnoreCase("spectralKnight") && (!iselite || tier2 != 4))
                        || (type.equalsIgnoreCase("jayden") && (!iselite || tier2 != 5))
                        || (type.equalsIgnoreCase("kilatan") && (!iselite || tier2 != 5))
                        || (type.equalsIgnoreCase("spiderQueen") && (!iselite || tier2 != 3))
                        || (type.equalsIgnoreCase("frostKing") && (!iselite || tier2 != 5))
                        || (type.equalsIgnoreCase("duranor") && (!iselite || tier2 != 4))
                        || (type.equalsIgnoreCase("weakSkeletonEntity") && (!iselite || tier2 != 5))
                        || (type.equalsIgnoreCase("bossSkeletonDungeon") && (!iselite || tier2 != 5))
                        || (type.equalsIgnoreCase("frozenBoss") && (!iselite || tier2 < 5))
                        || (type.equalsIgnoreCase("frozenGolem") && (!iselite || tier2 < 5))
                        || (type.equalsIgnoreCase("frozenElite") && (!iselite || tier2 < 5))
                        || (type.equalsIgnoreCase("krampus") && (!iselite || tier2 != 5)))
                        || (type.equalsIgnoreCase("warden") && (!iselite || tier2 != 5))) {
                    return false;
                }
            }
            return true;
        }
        final String type2 = data.split(":")[0];
        if (!type2.equalsIgnoreCase("skeleton") && !type2.equalsIgnoreCase("zombie")
                && !type2.equalsIgnoreCase("silverfish") && !type2.equalsIgnoreCase("magmacube")
                && !type2.equalsIgnoreCase("spider") && !type2.equalsIgnoreCase("cavespider")
                && !type2.equalsIgnoreCase("imp") && !type2.equalsIgnoreCase("witherskeleton")
                && !type2.equalsIgnoreCase("daemon") && !type2.equalsIgnoreCase("mitsuki")
                && !type2.equalsIgnoreCase("copjak") && !type2.equalsIgnoreCase("spiderQueen")
                && !type2.equalsIgnoreCase("duranor") && !type2.equalsIgnoreCase("kingofgreed")
                && !type2.equalsIgnoreCase("skeletonking") && !type2.equalsIgnoreCase("frostKing")
                && !type2.equalsIgnoreCase("golem") && !type2.equalsIgnoreCase("wither")
                && !type2.equalsIgnoreCase("giant") && !type2.equalsIgnoreCase("impa")
                && !type2.equalsIgnoreCase("bloodbutcher") && !type2.equalsIgnoreCase("blayshan")
                && !type2.equalsIgnoreCase("jayden") && !type2.equalsIgnoreCase("watchMaster")
                && !type2.equalsIgnoreCase("spectralKnight") && !type2.equalsIgnoreCase("spectralGuard")
                && !type2.equalsIgnoreCase("kilatan") && !type2.equalsIgnoreCase("bossSkeletonDungeon")
                && !type2.equalsIgnoreCase("weakSkeletonEntity") && !type2.equalsIgnoreCase("krampus")
                && !type2.equalsIgnoreCase("risk_Elite") && !type2.equalsIgnoreCase("warden")
                && !type2.equalsIgnoreCase("frozenElite") && !type2.equalsIgnoreCase("frozenGolem")
                && !type2.equalsIgnoreCase("frozenBoss") && !type2.equalsIgnoreCase("orcKing")) {
            return false;
        }
        try {
            final int tier3 = Integer.parseInt(data.split(":")[1].split("@")[0]);
            if (tier3 < 1 || tier3 > 6) {
                return false;
            }
        } catch (Exception e3) {
            return false;
        }
        final String elite2 = data.split("@")[1].split("#")[0];
        if (!elite2.equalsIgnoreCase("true") && !elite2.equalsIgnoreCase("false")) {
            return false;
        }
        try {
            final int amt2 = Integer.parseInt(data.split("#")[1]);
            if (amt2 < 1 || amt2 > 100) {
                return false;
            }
        } catch (Exception e4) {
            return false;
        }
        final int tier4 = Integer.parseInt(data.split(":")[1].split("@")[0]);
        final boolean iselite2 = Boolean.parseBoolean(elite2);
        return (!type2.equalsIgnoreCase("mitsuki") || (iselite2 && tier4 == 1))
                && (!type2.equalsIgnoreCase("copjak") || (iselite2 && tier4 == 2))
                && (!type2.equalsIgnoreCase("risk_Elite") || (iselite2 && tier4 == 2))
                && (!type2.equalsIgnoreCase("orcKing") || (iselite2 && tier4 == 2))
                && (!type2.equalsIgnoreCase("kingofgreed") || (iselite2 && tier4 == 3))
                && (!type2.equalsIgnoreCase("skeletonking") || (iselite2 && tier4 == 3))
                && (!type2.equalsIgnoreCase("impa") || (iselite2 && tier4 == 3))
                && (!type2.equalsIgnoreCase("bloodbutcher") || (iselite2 && tier4 == 4))
                && (!type2.equalsIgnoreCase("blayshan") || (iselite2 && tier4 == 4))
                && (!type2.equalsIgnoreCase("watchMaster") || (iselite2 && tier4 == 4))
                && (!type2.equalsIgnoreCase("spectralKnight") || (iselite2 && tier4 == 4))
                && (!type2.equalsIgnoreCase("jayden") || (iselite2 && tier4 == 5))
                && (!type2.equalsIgnoreCase("kilatan") || (iselite2 && tier4 == 5))
                && (!type2.equalsIgnoreCase("spiderQueen") || (iselite2 && tier4 == 3))
                && (!type2.equalsIgnoreCase("frostKing") || (iselite2 && tier4 == 5))
                && (!type2.equalsIgnoreCase("duranor") || (iselite2 && tier4 == 4))
                && (!type2.equalsIgnoreCase("weakSkeletonEntity") || (iselite2 && tier4 == 5))
                && (!type2.equalsIgnoreCase("bossSkeletonDungeon") || (iselite2 && tier4 == 5))
                && (!type2.equalsIgnoreCase("frozenBoss") || (iselite2 && tier4 > 4))
                && (!type2.equalsIgnoreCase("frozenGolem") || (iselite2 && tier4 > 4))
                && (!type2.equalsIgnoreCase("frozenElite") || (iselite2 && tier4 > 4))
                && (!type2.equalsIgnoreCase("krampus") || (iselite2 && tier4 == 5))
                && (!type2.equalsIgnoreCase("warden") || (iselite2 && tier4 == 5));
    }

    public static int getMobTier(final LivingEntity e) {
        if (e.getEquipment().getItemInMainHand() != null) {
            if (e.getEquipment().getItemInMainHand().getType().name().contains("WOOD_")) {
                return 1;
            }
            if (e.getEquipment().getItemInMainHand().getType().name().contains("STONE_")) {
                return 2;
            }
            if (e.getEquipment().getItemInMainHand().getType().name().contains("IRON_")) {
                return 3;
            }
            if (e.getEquipment().getItemInMainHand().getType().name().contains("DIAMOND_")
                    && !e.getEquipment().getItemInMainHand().getType().name().contains("DIAMOND_") && e.getEquipment()
                    .getItemInMainHand().getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString())) {
                return 4;
            }
            if (e.getEquipment().getItemInMainHand().getType().name().contains("GOLD_")) {
                return 5;
            }
            if (e.getEquipment().getItemInMainHand().getType().name().contains("DIAMOND_")) {
                return 6;
            }
        }
        return 0;
    }

    public static boolean isElite(final LivingEntity e) {
        return e.getEquipment().getItemInMainHand() != null
                && e.getEquipment().getItemInMainHand().getType() != Material.AIR
                && e.getEquipment().getItemInMainHand().getItemMeta().hasEnchants();
    }

    public static int hpCheck(final LivingEntity s) {
        int a = 0;
        ItemStack[] armorContents;
        for (int length = (armorContents = s.getEquipment().getArmorContents()).length, i = 0; i < length; ++i) {
            final ItemStack is = armorContents[i];
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                final int health = getHp(is);
                a += health;
            }
        }
        return a;
    }

    public static LivingEntity mob(final Location loc, final String type) {
        if (type.toLowerCase().contains("skeleton") && !type.equalsIgnoreCase("witherskeleton")
                || type.equalsIgnoreCase("prisoner") || type.equalsIgnoreCase("warden")
                || type.equalsIgnoreCase("skeleton") || type.equalsIgnoreCase("impa")
                || type.equalsIgnoreCase("skeletonking") || type.equalsIgnoreCase("kingofgreed")
                || (type.equalsIgnoreCase("duranor")) || (type.equalsIgnoreCase("watchMaster"))
                || type.equalsIgnoreCase("krampus") || type.equalsIgnoreCase("frostwing")) {
            final Skeleton skeleton = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
            new CreatureSpawnEvent(skeleton, CreatureSpawnEvent.SpawnReason.CUSTOM);

            return skeleton;
        }
        if (type.equalsIgnoreCase("witherskeleton") || type.toLowerCase().contains("kilatan")
                || type.equalsIgnoreCase("jayden") || type.equalsIgnoreCase("frostKing")
                || type.equalsIgnoreCase("frozenElite") || type.equalsIgnoreCase("frozenBoss")) {
            final WitherSkeleton skeleton = (WitherSkeleton) loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
            new CreatureSpawnEvent(skeleton, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return skeleton;
        }
        if (type.equalsIgnoreCase("zombie") || type.equalsIgnoreCase("risk_Elite") || type.equalsIgnoreCase("mitsuki")
                || type.equalsIgnoreCase("blayshan") || type.equalsIgnoreCase("bloodbutcher")
                || type.equalsIgnoreCase("copjak") || type.equalsIgnoreCase("orcKing")) {
            Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
            new CreatureSpawnEvent(zombie, CreatureSpawnEvent.SpawnReason.CUSTOM);
            zombie.setBaby(false);
            return zombie;
        }
        if (type.equalsIgnoreCase("silverfish")) {
            Silverfish fish = (Silverfish) loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
            new CreatureSpawnEvent(fish, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return fish;
        }
        if (type.equalsIgnoreCase("magmacube")) {
            MagmaCube cube = (MagmaCube) loc.getWorld().spawnEntity(loc, EntityType.MAGMA_CUBE);
            new CreatureSpawnEvent(cube, CreatureSpawnEvent.SpawnReason.CUSTOM);
            cube.setSize(3);
            return cube;
        }
        if (type.equalsIgnoreCase("spider") || (type.equalsIgnoreCase("spiderQueen"))) {
            final Spider spider = (Spider) loc.getWorld().spawnEntity(loc, EntityType.SPIDER);
            new CreatureSpawnEvent(spider, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return spider;
        }
        if (type.equalsIgnoreCase("cavespider")) {
            final CaveSpider cspider = (CaveSpider) loc.getWorld().spawnEntity(loc, EntityType.CAVE_SPIDER);
            new CreatureSpawnEvent(cspider, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return cspider;
        }
        if (type.equalsIgnoreCase("daemon") || type.equalsIgnoreCase("spectralKnight")) {
            final PigZombie daemon = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
            daemon.setAngry(true);
            daemon.setBaby(false);
            new CreatureSpawnEvent(daemon, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return daemon;
        }
        if (type.equalsIgnoreCase("imp") || type.equalsIgnoreCase("spectralGuard")) {
            final PigZombie imp = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
            imp.setAngry(true);
            imp.setBaby(true);
            new CreatureSpawnEvent(imp, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return imp;
        }
        if (type.equalsIgnoreCase("turkey")) {
            final Chicken turkey = (Chicken) loc.getWorld().spawnEntity(loc, EntityType.CHICKEN);
            new CreatureSpawnEvent(turkey, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return turkey;
        }
        if (type.equalsIgnoreCase("giant")) {
            final Giant giant = (Giant) loc.getWorld().spawnEntity(loc, EntityType.GIANT);
            new CreatureSpawnEvent(giant, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return giant;
        }
        if (type.equalsIgnoreCase("golem") || type.equalsIgnoreCase("frozenGolem")) {
            final Golem golem = (Golem) loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
            new CreatureSpawnEvent(golem, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return golem;
        }
        if (type.equalsIgnoreCase("wither")) {
            final Wither wither = (Wither) loc.getWorld().spawnEntity(loc, EntityType.WITHER);
            new CreatureSpawnEvent(wither, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return wither;
        }
        return null;
    }

    public static LivingEntity spawnMob(final Location loc, final String type, final int tier, final boolean elite) {
        final int randX = Util.random.nextInt(7) - 3;
        final int randZ = Util.random.nextInt(7) - 3;
        Location sloc = new Location(loc.getWorld(), loc.getX() + randX + 0.5, loc.getY() + 2.0,
                loc.getZ() + randZ + 0.5);
        if (sloc.getWorld().getBlockAt(sloc).getType() != Material.AIR
                || sloc.getWorld().getBlockAt(sloc.add(0.0, 1.0, 0.0)).getType() != Material.AIR) {
            sloc = loc.clone().add(0.0, 1.0, 0.0);
        } else {
            sloc.subtract(0.0, 1.0, 0.0);
        }
        final LivingEntity s = mob(sloc, type);
        String name = "";
        int gearcheck = Util.random.nextInt(3) + 1;
        if (tier == 3) {
            final int m_type = Util.random.nextInt(2);
            if (m_type == 0) {
                gearcheck = 3;
            }
            if (m_type == 1) {
                gearcheck = 4;
            }
        }
        if (tier >= 4 || elite) {
            gearcheck = 4;
        }
        int held = Util.random.nextInt(5);
        if (held == 0) held += 1;
        if (held == 1) {
            held = ThreadLocalRandom.current().nextInt(1, 5);
        }

        final ItemStack hand = Drops.createDrop(tier, held);
        if (elite) {
            hand.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
        }
        ItemStack head = null;
        ItemStack chest = null;
        ItemStack legs = null;
        ItemStack boots = null;
        int a_type = 0;
        while (gearcheck > 0) {
            a_type = Util.random.nextInt(4) + 1;
            if (a_type == 1 && head == null) {
                head = Drops.createDrop(tier, 5);
                if (elite) {
                    head.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
                }
                --gearcheck;
            }
            if (a_type == 2 && chest == null) {
                chest = Drops.createDrop(tier, 6);
                if (elite) {
                    chest.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
                }
                --gearcheck;
            }
            if (a_type == 3 && legs == null) {
                legs = Drops.createDrop(tier, 7);
                if (elite) {
                    legs.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
                }
                --gearcheck;
            }
            if (a_type == 4 && boots == null) {
                boots = Drops.createDrop(tier, 8);
                if (elite) {
                    boots.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);
                }
                --gearcheck;
            }
        }

        /**
         * Halloween int randomHelmetChance =
         * ThreadLocalRandom.current().nextInt(100); if(randomHelmetChance > 65
         * && head != null) { head = new
         * ItemGenerator(Material.JACK_O_LANTERN).setName(head.getItemMeta().getDisplayName()).setLore(head.getItemMeta().getLore()).build();
         * }
         */
        s.setCanPickupItems(false);
        s.setRemoveWhenFarAway(false);

        if (type.equals("bossSkeletonDungeon")) {
            name = ChatColor.DARK_RED + "The Restless Skeleton Deathlord";
        }
        if (type.equalsIgnoreCase("frostwing")) {
            name = ChatColor.YELLOW + "" + ChatColor.BOLD + "Frost-wing The Frozen Titan";
        }

        if (type.equals("weakSkeletonEntity") || type.equals("weakSkeletonEntity_UV")) {
            int id = new Random().nextInt(3);

            switch (id) {
                case 0:
                    name = ChatColor.RED + "Infernal Skeletal Keeper";
                    break;
                case 1:
                    name = ChatColor.RED + "Skeletal Soul Keeper";
                    break;
                case 2:
                    name = ChatColor.RED + "Skeletal Soul Harvester";
                    break;
                case 3:
                    name = ChatColor.RED + "Infernal Skeletal Soul Harvester";
                    break;
            }
        }

        if (type.equals("skellyDSkeletonGuardian")) {

            int id = new Random().nextInt(2);

            switch (id) {
                case 0:
                    name = ChatColor.LIGHT_PURPLE + "Skeletal Guardian Deadlord";
                    break;
                case 1:
                    name = ChatColor.LIGHT_PURPLE + "Skeletal Guardian Overlord";
                    break;
                case 2:
                    name = ChatColor.LIGHT_PURPLE + "Restless Skeletal Guardian";
                    break;
            }
        }

        if (type.equalsIgnoreCase("prisoner")) {
            int id = new Random().nextInt(2);

            switch (id) {
                case 0:
                    name = ChatColor.RED + "Tortured Prisoner";
                    break;
                case 1:
                    name = ChatColor.RED + "Corrupted Prison Guard";
                    break;
                case 2:
                    name = ChatColor.RED + "Tortmented Guard";
                    break;
            }
        }

        if (type.equalsIgnoreCase("skeleton")) {
            if (tier == 1) {
                name = "Broken Skeleton";
            }
            if (tier == 2) {
                name = "Wandering Cracking Skeleton";
            }
            if (tier == 3) {
                name = "Demonic Skeleton";
            }
            if (tier == 4) {
                name = "Skeleton Guardian";
            }
            if (tier == 5) {
                name = "Infernal Skeleton";
            }
            if (tier == 6) {
                name = "Frozen Skeleton";
            }
        }
        if (type.equalsIgnoreCase("spectralGuard")) {
            name = "The Evil Spectral's Impish Guard";
        }
        if (type.equalsIgnoreCase("witherskeleton")) {
            if (tier == 1) {
                name = "Broken Chaos Skeleton";
            }
            if (tier == 2) {
                name = "Wandering Cracking Chaos Skeleton";
            }
            if (tier == 3) {
                name = "Demonic Chaos Skeleton";
            }
            if (tier == 4) {
                name = "Skeleton Chaos Guardian";
            }
            if (tier == 5) {
                name = "Infernal Chaos Skeleton";
            }
            if (tier == 6) {
                name = "Frozen Skeletal Minion";
            }
        }
        if (type.equalsIgnoreCase("golem")) {
            if (tier == 1) {
                name = "Broken Golem";
            }
            if (tier == 2) {
                name = "Rusty Golem";
            }
            if (tier == 3) {
                name = "Restored Golem";
            }
            if (tier == 4) {
                name = "Mountain Golem";
            }
            if (tier == 5) {
                name = "Powerful Golem";
            }
            if (tier == 6) {
                name = "Devastating Golem";
            }
        }
        if (type.equalsIgnoreCase("imp")) {
            if (tier == 1) {
                name = "Ugly Imp";
            }
            if (tier == 2) {
                name = "Angry Imp";
            }
            if (tier == 3) {
                name = "Warrior Imp";
            }
            if (tier == 4) {
                name = "Armoured Imp";
            }
            if (tier == 5) {
                name = "Infernal Imp";
            }
            if (tier == 6) {
                name = "Arctic Imp";
            }
        }
        if (type.equalsIgnoreCase("daemon")) {
            if (tier == 1) {
                name = "Broken Daemon";
            }
            if (tier == 2) {
                name = "Wandering Cracking Daemon";
            }
            if (tier == 3) {
                name = "Demonic Daemon";
            }
            if (tier == 4) {
                name = "Daemon Guardian";
            }
            if (tier == 5) {
                name = "Infernal Daemon";
            }
            if (tier == 6) {
                name = "Chilled Daemon";
            }
        }
        if (type.equalsIgnoreCase("zombie")) {
            if (tier == 1) {
                name = "Rotting Zombie";
            }
            if (tier == 2) {
                name = "Savaged Zombie";
            }
            if (tier == 3) {
                name = "Greater Zombie";
            }
            if (tier == 4) {
                name = "Demonic Zombie";
            }
            if (tier == 5) {
                name = "Infernal Zombie";
            }
            if (tier == 6) {
                name = "Frozen Zombie";
            }
        }
        if (type.equalsIgnoreCase("magmacube")) {
            if (tier == 1) {
                name = "Weak Magma Cube";
            }
            if (tier == 2) {
                name = "Bubbling Magma Cube";
            }
            if (tier == 3) {
                name = "Unstable Magma Cube";
            }
            if (tier == 4) {
                name = "Boiling Magma Cube";
            }
            if (tier == 5) {
                name = "Unstoppable Magma Cube";
            }
            if (tier == 6) {
                name = "Ice Cube";
            }
        }
        if (type.equalsIgnoreCase("silverfish")) {
            if (tier == 1) {
                name = "Weak SilverFish";
            }
            if (tier == 2) {
                name = "Pointy SilverFish";
            }
            if (tier == 3) {
                name = "Unstable SilverFish";
            }
            if (tier == 4) {
                name = "Mean SilverFish";
            }
            if (tier == 5) {
                name = "Rude SilverFish";
            }
            if (tier == 6) {
                name = "Ice-Cold SilverFish";
            }
        }
        if (type.equalsIgnoreCase("spider") || type.equalsIgnoreCase("cavespider")) {
            if (tier == 1) {
                name = ChatColor.WHITE + "" + (elite ? ChatColor.BOLD : "") + "Harmless ";
            }
            if (tier == 2) {
                name = ChatColor.GREEN + "" + (elite ? ChatColor.BOLD : "") + "Wild ";
            }
            if (tier == 3) {
                name = ChatColor.AQUA + "" + (elite ? ChatColor.BOLD : "") + "Fierce ";
            }
            if (tier == 4) {
                name = ChatColor.LIGHT_PURPLE + "" + (elite ? ChatColor.BOLD : "") + "Dangerous ";
            }
            if (tier == 5) {
                name = ChatColor.YELLOW + "" + (elite ? ChatColor.BOLD : "") + "Lethal ";
            }
            if (tier == 6) {
                name = ChatColor.BLUE + "" + (elite ? ChatColor.BOLD : "") + "Devastating ";
            }
            if (type.equalsIgnoreCase("cavespider")) {
                name = String.valueOf(name) + "Cave ";
            }
            name = String.valueOf(name) + "Spider";
        }
        if (type.equalsIgnoreCase("warden")) {
            name = ChatColor.RED + "The Warden";
        } else if (type.equalsIgnoreCase("risk_Elite")) {
            name = "Riskan The Rotten";
        } else if (type.equalsIgnoreCase("duranor")) {
            name = "Duranor The Cruel";
        } else if (type.equalsIgnoreCase("krampus")) {
            name = "Krampus The Warrior";
        } else if (type.equalsIgnoreCase("mitsuki")) {
            name = "Mitsuki The Dominator";
        } else if (type.equalsIgnoreCase("copjak")) {
            name = "Cop'jak";
        } else if (type.equalsIgnoreCase("orcKing")) {
            name = "The Orc King";
        } else if (type.equalsIgnoreCase("kingofgreed")) {
            name = "The King Of Greed";
        } else if (type.equalsIgnoreCase("skeletonking")) {
            name = "The Skeleton King";
        } else if (type.equalsIgnoreCase("impa")) {
            name = "Impa The Impaler";
        } else if (type.equalsIgnoreCase("bloodbutcher")) {
            name = "The Blood Butcher";
        } else if (type.equalsIgnoreCase("blayshan")) {
            name = "Blayshan The Naga";
        } else if (type.equalsIgnoreCase("watchMaster")) {
            name = "The Watchmaster";
        } else if (type.equalsIgnoreCase("jayden")) {
            name = "King Jayden";
        } else if (type.equalsIgnoreCase("frostKing")) {
            name = "Frost Walker";
        } else if (type.equalsIgnoreCase("spiderQueen")) {
            name = "The Spider Queen";
        } else if (type.equalsIgnoreCase("kilatan")) {
            name = "Daemon Lord Kilatan";
        } else if (type.equalsIgnoreCase("spectralKnight")) {
            name = "The Evil Spectral Overlord";
        } else if (type.equalsIgnoreCase("frozenElite")) {
            name = "Frost The Exiled King";
        } else if (type.equalsIgnoreCase("frozenBoss")) {
            name = "The Conquer of The North";
        } else if (type.equalsIgnoreCase("frozenGolem")) {
            name = "Crypt Guardian";
        }
        String color = ChatColor.WHITE.toString();
        switch (tier) {
            case 1: {
                color = ChatColor.WHITE.toString();
                break;
            }
            case 2: {
                color = ChatColor.GREEN.toString();
                break;
            }
            case 3: {
                color = ChatColor.AQUA.toString();
                break;
            }
            case 4: {
                color = ChatColor.LIGHT_PURPLE.toString();
                break;
            }
            case 5: {
                color = ChatColor.YELLOW.toString();
                break;
            }
            case 6: {
                color = ChatColor.BLUE.toString();
                break;
            }
        }
        if (elite) {
            color = String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + ChatColor.BOLD.toString();
        }
        s.setCustomName(String.valueOf(color) + name);
        s.setCustomNameVisible(true);
        s.setMetadata("name", new FixedMetadataValue(PracticeServer.plugin, String.valueOf(color) + name));
        s.setMetadata("type", new FixedMetadataValue(PracticeServer.plugin, type));

        if (elite && !type.equalsIgnoreCase("frozenBoss")) {
            s.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        }
        if (tier > 2 && !type.equalsIgnoreCase("frozenBoss")) {
            if (s.getEquipment().getItemInMainHand() != null
                    && s.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) {
                s.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
            } else {
                s.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            }
        }
        if (type.equalsIgnoreCase("frozenGolem")) {
            s.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        }

        if (type.equals("weakSkeletonEntity"))
            s.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));

        s.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
        s.getEquipment().clear();
        s.getEquipment().setItemInMainHand(hand);

        if (type.equalsIgnoreCase("frostwing")) {
            s.getEquipment().setHelmet(SkullTextures.FROST.getFrostSkull());
        }
        if (type.equals("bossSkeletonDungeon")) {
            s.getEquipment().setHelmet(SkullTextures.WITHER_KING.getSkullByURL());
        } else
            s.getEquipment().setHelmet(head);
        s.getEquipment().setChestplate(chest);
        s.getEquipment().setLeggings(legs);
        s.getEquipment().setBoots(boots);
        if (s.getType().equals(EntityType.SKELETON)
                && ((Skeleton) s).getSkeletonType().equals(Skeleton.SkeletonType.WITHER)) {
            s.getEquipment().setHelmet(null);
        }
        int hp = hpCheck(s);
        if (elite) {
            if (tier == 1) {
                hp = (int) (hp * 1.8);
            }
            if (tier == 2) {
                hp = (int) (hp * 2.5);
            }
            if (tier == 3) {
                hp = hp * 3;
            }
            if (tier == 4) {
                hp = hp * 5;
            }
            if (tier == 5) {
                hp = hp * 6;
            }
            if (tier == 6) {
                hp = hp * 7;
            }
            if (type.equalsIgnoreCase("warden")) {
                hp = 85000;
            }
            if (s.hasMetadata("infernalType")) {
                hp = hp * 2;
            }
            if (type.equalsIgnoreCase("bossSkeletonDungeon")) {
                hp = 115000;
            }
            if (type.equalsIgnoreCase("frostwing")) {
                hp = ThreadLocalRandom.current().nextInt(210000, 234444);
            }
            if (type.equalsIgnoreCase("frozenElite")) {
                hp = PracticeServer.t6 ? 200000 : 100000;
            }
            if (type.equalsIgnoreCase("frozenBoss")) {
                hp = PracticeServer.t6 ? 300000 : 200000;
            }
            if (type.equalsIgnoreCase("frozenGolem")) {
                hp = PracticeServer.t6 ? 400000 : 200000;
            }
        } else {
            if (tier == 1) {
                hp = (int) (hp * 0.4);
            }
            if (tier == 2) {
                hp = (int) (hp * 0.9);
            }
            if (tier == 3) {
                hp = (int) (hp * 1.2);
            }
            if (tier == 4) {
                hp = (int) (hp * 1.4);
            }
            if (tier == 5) {
                hp = hp * 2;
            }
            if (tier == 6) {
                hp = (int) (hp * 2.5);
            }
        }
        if (hp < 1) {
            hp = 1;
        }
        s.setMetadata("customName", new FixedMetadataValue(PracticeServer.getInstance(), type));

        s.setRemoveWhenFarAway(false);
        s.setMaxHealth((double) hp);
        s.setHealth((double) hp);
        new BukkitRunnable() {
            public void run() {
                if (type.equalsIgnoreCase("frozenGolem"))
                    GolemElite.golems.put(s, 0);
                Spawners.mobs.put(s, loc);
            }
        }.runTaskLaterAsynchronously(PracticeServer.plugin, 1L);
        return s;
    }

    public void onEnable() {
        PracticeServer.log.info("[Spawners] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        final File file = new File(PracticeServer.plugin.getDataFolder(), "spawners.yml");
        final YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            config.load(file);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        for (final String key : config.getKeys(false)) {
            final String val = config.getString(key);
            final String[] str = key.split(",");
            final World world = Bukkit.getWorld(str[0]);
            final double x = Double.valueOf(str[1]);
            final double y = Double.valueOf(str[2]);
            final double z = Double.valueOf(str[3]);
            final Location loc = new Location(world, x, y, z);
            Spawners.spawners.put(loc, val);
        }
        Bukkit.getServer().getWorld("jew").getEntities().stream().filter(e3 -> (e3 instanceof LivingEntity))
                .forEach(Entity::remove);
        Bukkit.getServer().getWorlds().get(0).getEntities().stream()
                .filter(e3 -> (e3 instanceof LivingEntity && !(e3 instanceof Player)) || e3 instanceof Item
                        || e3 instanceof EnderCrystal)
                .forEach(Entity::remove);
        new BukkitRunnable() {
            public void run() {
                // if (DeployCommand.patchlockdown) return;
                for (final Entity e : Bukkit.getWorlds().get(0).getEntities()) {
                    try {
                        if (e instanceof LivingEntity) {
                            final LivingEntity s = (LivingEntity) e;
                            if (!Spawners.mobs.containsKey(s)) {
                                continue;
                            }
                            if (s.getEquipment().getHelmet() != null && s.getEquipment().getHelmet().getType() != Material.AIR) {
                                s.getEquipment().getHelmet().setDurability((short) 0);
                            }
                            final Location loc = Spawners.mobs.get(s);
                            final Location newloc = s.getLocation();
                            if (loc.distance(newloc) <= (Mobs.isGolemBoss(s) || MobHandler.isWorldBoss(s) ? 45 : 30)) {
                                continue;
                            }
                            s.setFallDistance(0.0f);
                            if (Mobs.crit.containsKey(s) && Mobs.isElite(s)) continue;
                            int distance = (int)loc.distance(newloc);
                            int MAX_DISTANCE = 30;
                            if(isElite(s)) MAX_DISTANCE = 35;
                            if(MobHandler.isCustomNamedElite(s) || MobHandler.isWorldBoss(s)) MAX_DISTANCE = 45;

                            if(distance > MAX_DISTANCE) {
                                Particles.SPELL.display(0.0f, 0.0f, 0.0f, 0.5f, 80, s.getLocation().clone().add(0.0, 0.15, 0.0), 20);
                                s.teleport(loc);
                            }
                            if (Mobs.crit.containsKey(s)) Mobs.crit.remove(s);

                            if (!s.hasMetadata("name")) {
                                continue;
                            }
                            s.setCustomName(s.getMetadata("name").get(0).asString());
                            s.setCustomNameVisible(true);
                        }
                    } catch (Exception exc) {

                    }
                }

                Spawners.mobs.keySet().stream().filter(l -> l == null || l.isDead()).forEach(l -> {
                    Spawners.mobs.remove(l);
                });
            }
        }.runTaskTimer(PracticeServer.plugin, 1, 1);
        new BukkitRunnable() {
            public void run() {
                // if (DeployCommand.patchlockdown) return;
                for (final Location loc : Spawners.spawners.keySet()) {
                    if (Spawners.isPlayerNearby(loc) && loc.getChunk().isLoaded() && !Spawners.mobs.containsValue(loc)
                            && (!Spawners.respawntimer.containsKey(loc)
                            || System.currentTimeMillis() > Spawners.respawntimer.get(loc))) {
                        final String data = Spawners.spawners.get(loc);
                        if (!Spawners.isCorrectFormat(data)) {
                            continue;

                        }
                        if (data.contains(",")) {
                            String[] split;
                            for (int length = (split = data.split(",")).length, k = 0; k < length; ++k) {
                                final String s = split[k];
                                final String type = s.split(":")[0];
                                int tier = Integer.parseInt(s.split(":")[1].split("@")[0]);
                                if (tier > 5 && !PracticeServer.t6) tier = 5;
                                final boolean elite = Boolean.parseBoolean(s.split("@")[1].split("#")[0]);
                                for (int amt = Integer.parseInt(s.split("#")[1]), i = 0; i < amt; ++i) {
                                    if (!DeployCommand.patchlockdown && ToggleMobsCommand.togglespawners)
                                        Spawners.this.spawnMob(loc, type, tier, elite);
                                }
                            }
                        } else {
                            final String type2 = data.split(":")[0];
                            int tier2 = Integer.parseInt(data.split(":")[1].split("@")[0]);
                            if (tier2 > 5 && !PracticeServer.t6) tier2 = 5;
                            final boolean elite2 = Boolean.parseBoolean(data.split("@")[1].split("#")[0]);
                            for (int amt2 = Integer.parseInt(data.split("#")[1]), j = 0; j < amt2; ++j) {
                                if (!DeployCommand.patchlockdown && ToggleMobsCommand.togglespawners)
                                    Spawners.this.spawnMob(loc, type2, tier2, elite2);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 100L, 20L);
        new BukkitRunnable() {
            public void run() {
                for (final Entity e : Bukkit.getWorlds().get(0).getEntities()) {
                    if (e instanceof LivingEntity && !(e instanceof Player)
                            && !Spawners.isPlayerNearby(e.getLocation()) && !Mobs.isElite((LivingEntity) e)) {
                        if (!MobHandler.isWorldBoss(e)) e.remove();
                        Spawners.mobs.remove(e);
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 200L, 200L);
        new BukkitRunnable() {
            public void run() {
                for (final Entity e : Bukkit.getWorlds().get(0).getEntities()) {
                    if (e instanceof LivingEntity && !(e instanceof Player) && !Mobs.isElite((LivingEntity) e)) {
                        if (!MobHandler.isWorldBoss(e)) e.remove();
                    }
                }
                Spawners.mobs.clear();
                Spawners.respawntimer.clear();
            }
        }.runTaskLater(PracticeServer.plugin, 72000L);
    }

    public void thanksgiving(Location loc, int tier) {
        Random r = new Random();
        int thanksgiving = r.nextInt(300);
        if (thanksgiving == 0) {
            LivingEntity turkey = mob(loc, "turkey");
            turkey.setCustomName(ChatColor.GOLD + "Thanksgiving Turkey");
            turkey.setRemoveWhenFarAway(false);
            turkey.setCustomNameVisible(true);
            turkey.setMetadata("type", new FixedMetadataValue(PracticeServer.plugin, "turkey"));
            turkey.setMetadata("name",
                    new FixedMetadataValue(PracticeServer.plugin, ChatColor.GOLD + "Thanksgiving Turkey"));
            turkey.setMetadata("tier", new FixedMetadataValue(PracticeServer.plugin, Integer.toString(tier)));
            turkey.getEquipment().setItemInMainHand(new ItemStack(Material.FEATHER));
            turkey.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 10));
            turkey.setMaxHealth((double) 35 * tier * tier * tier);
            turkey.setHealth((double) 35 * tier * tier * tier);
        }
    }

    public void onDisable() {
        PracticeServer.log.info("[Spawners] has been disabled.");
        final File file = new File(PracticeServer.plugin.getDataFolder(), "spawners.yml");
        if (file.exists()) {
            file.delete();
        }
        final YamlConfiguration config = new YamlConfiguration();
        if (!spawners.isEmpty()) {
            for (final Location loc : Spawners.spawners.keySet()) {
                final String s = String.valueOf(loc.getWorld().getName()) + "," + (int) loc.getX() + ","
                        + (int) loc.getY() + "," + (int) loc.getZ();
                config.set(s, Spawners.spawners.get(loc));
                try {
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (final Entity e2 : Bukkit.getServer().getWorlds().get(0).getEntities()) {
            if ((e2 instanceof LivingEntity && !(e2 instanceof Player)) || e2 instanceof Item
                    || e2 instanceof EnderCrystal) {
                if (e2 instanceof EnderCrystal) {
                    e2.getLocation().getWorld().getBlockAt(e2.getLocation().subtract(0.0, 1.0, 0.0))
                            .setType(Material.CHEST);
                }
                if (!MobHandler.isWorldBoss(e2)) e2.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawnerCreate(final AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        if (p.isOp() && this.creatingspawner.containsKey(p.getName())) {
            e.setCancelled(true);
            if (e.getMessage().equalsIgnoreCase("cancel")) {
                p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                        .append("     *** SPAWNER CREATION CANCELLED ***").toString());
                this.creatingspawner.remove(p.getName());
            } else if (isCorrectFormat(e.getMessage())) {
                p.sendMessage(ChatColor.GRAY + "Spawner with data '" + ChatColor.YELLOW + e.getMessage()
                        + ChatColor.GRAY + "' created at " + ChatColor.YELLOW
                        + this.creatingspawner.get(p.getName()).toVector());
                Spawners.spawners.put(this.creatingspawner.get(p.getName()), e.getMessage());
                this.creatingspawner.remove(p.getName());
                final File file = new File(PracticeServer.plugin.getDataFolder(), "spawners.yml");
                if (file.exists()) {
                    file.delete();
                }
                final YamlConfiguration config = new YamlConfiguration();
                for (final Location loc : Spawners.spawners.keySet()) {
                    final String s = String.valueOf(loc.getWorld().getName()) + "," + (int) loc.getX() + ","
                            + (int) loc.getY() + "," + (int) loc.getZ();
                    config.set(s, Spawners.spawners.get(loc));
                    try {
                        config.save(file);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                        .append("     *** INCORRECT FORMAT ***").toString());
                p.sendMessage(" ");
                p.sendMessage(ChatColor.YELLOW + "FORMAT: " + ChatColor.GRAY + "mobtype:tier@elite#amount");
                p.sendMessage(ChatColor.YELLOW + "EX: " + ChatColor.GRAY
                        + "skeleton:5@true#1,zombie:4@true#1,magmacube:4@false#5");
                p.sendMessage(" ");
                p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                        .append("     *** INCORRECT FORMAT ***").toString());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        if (p.isOp() && e.getBlock().getType().equals(Material.MOB_SPAWNER)) {
            p.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD)
                    .append("     *** SPAWNER CREATION STARTED ***").toString());
            p.sendMessage(" ");
            p.sendMessage(ChatColor.YELLOW + "FORMAT: " + ChatColor.GRAY + "mobtype:tier@elite#amount");
            p.sendMessage(ChatColor.YELLOW + "EX: " + ChatColor.GRAY
                    + "skeleton:5@true#1,zombie:4@true#1,magmacube:4@false#5");
            p.sendMessage(" ");
            p.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD)
                    .append("     *** SPAWNER CREATION STARTED ***").toString());
            this.creatingspawner.put(p.getName(), e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        final Player p = e.getPlayer();
        if (p.isOp() && e.getBlock().getType().equals(Material.MOB_SPAWNER)) {
            if (Spawners.spawners.containsKey(e.getBlock().getLocation())) {
                p.sendMessage(ChatColor.GRAY + "Spawner with data '" + ChatColor.YELLOW
                        + Spawners.spawners.get(e.getBlock().getLocation()) + ChatColor.GRAY + "' removed at "
                        + ChatColor.YELLOW + e.getBlock().getLocation().toVector());
                Spawners.spawners.remove(e.getBlock().getLocation());
                PracticeServer.log.info("[Spawners] a spawner has been destroyed.");
                final File file = new File(PracticeServer.plugin.getDataFolder(), "spawners.yml");
                if (file.exists()) {
                    file.delete();
                }
                final YamlConfiguration config = new YamlConfiguration();
                for (final Location loc : Spawners.spawners.keySet()) {
                    final String s = String.valueOf(loc.getWorld().getName()) + "," + (int) loc.getX() + ","
                            + (int) loc.getY() + "," + (int) loc.getZ();
                    config.set(s, Spawners.spawners.get(loc));
                    try {
                        config.save(file);
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    }
                }
            }
            if (this.creatingspawner.containsValue(e.getBlock().getLocation())) {
                for (final String s : this.creatingspawner.keySet()) {
                    if (this.creatingspawner.get(s).equals(e.getBlock().getLocation())) {
                        p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                .append("     *** SPAWNER CREATION CANCELLED ***").toString());
                        this.creatingspawner.remove(s);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onBlockClick(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (p.isOp() && e.getAction() == Action.RIGHT_CLICK_BLOCK
                && e.getClickedBlock().getType().equals(Material.MOB_SPAWNER)
                && Spawners.spawners.containsKey(e.getClickedBlock().getLocation())) {
            p.sendMessage(ChatColor.GRAY + "Spawner with data '" + ChatColor.YELLOW
                    + Spawners.spawners.get(e.getClickedBlock().getLocation()) + ChatColor.GRAY + "' at "
                    + ChatColor.YELLOW + e.getClickedBlock().getLocation().toVector());
        }
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent e) {
        Entity[] entities;
        for (int length = (entities = e.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity ent = entities[i];
            if (ent instanceof LivingEntity && !(ent instanceof Player) && !(ent instanceof EnderCrystal)) {
                if (Spawners.mobs.containsKey(ent)) {
                    Spawners.mobs.remove(ent);
                }
                if (!MobHandler.isWorldBoss(ent)) ent.remove();
            }
        }
    }

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent e) {
        Entity[] entities;
        for (int length = (entities = e.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity ent = entities[i];
            if ((ent instanceof LivingEntity && !(ent instanceof Player)) || ent instanceof EnderCrystal) {
                if (Spawners.mobs.containsKey(ent)) {
                    Spawners.mobs.remove(ent);
                }
                if (ent instanceof EnderCrystal) {
                    ent.getLocation().getWorld().getBlockAt(ent.getLocation().subtract(0.0, 1.0, 0.0))
                            .setType(Material.CHEST);
                }
                if (!MobHandler.isWorldBoss(ent)) ent.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeathd(final EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            final LivingEntity s = (LivingEntity) e.getEntity();
            if (e.getDamage() >= s.getHealth() && Spawners.mobs.containsKey(s)) {
                long time = WorldBossHandler.getActiveBoss() != null ? 20L : 40L;
                time *= getMobTier(s);
                time *= isElite(s) ? 1200L : 1000L;
                time += System.currentTimeMillis();
                if (!Spawners.respawntimer.containsKey(Spawners.mobs.get(s))
                        || Spawners.respawntimer.get(Spawners.mobs.get(s)) < time) {
                    Spawners.respawntimer.put(Spawners.mobs.get(s), time);
                }
                Spawners.mobs.remove(s);
            }
        }
    }
}
