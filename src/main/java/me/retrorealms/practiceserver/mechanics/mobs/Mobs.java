package me.retrorealms.practiceserver.mechanics.mobs;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.damage.Staffs;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.mobs.elite.GolemElite;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Mobs implements Listener {
    public static HashMap<LivingEntity, Integer> crit = new HashMap<>();
    public static HashMap<UUID, Long> sound = new HashMap<>();
    static ConcurrentHashMap<Creature, Player> target = new ConcurrentHashMap<>();

    public static boolean isSkeletonElite(Entity entity) {
        return entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equals("bossSkeletonDungeon");
    }

    public static boolean isFrozenBoss(Entity entity) {
        return entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equals("frozenBoss");
    }

    public static boolean isGolemBoss(Entity entity) {
        return entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equals("frozenGolem");
    }

    public static int getGolemStage(Entity entity) {
        return GolemElite.golems.containsKey(entity) ? GolemElite.golems.get(entity) : 0;
    }

    public static boolean isSkeletonMinion(Entity entity) {
        return entity.hasMetadata("type")
                && entity.getMetadata("type").get(0).asString().contains("weakSkeletonEntity");
    }

    public static boolean isType(Entity entity, String type) {
        return entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().contains(type);
    }


    static boolean isPlayerNearby(Creature c) {
        return c.getNearbyEntities(12.0, 12.0, 12.0).stream().anyMatch(ent -> ent instanceof Player && ent == c.getTarget());
    }

    public static int getMobTier(LivingEntity e) {
        String mainHandTypeName = e.getEquipment().getItemInMainHand().getType().name();
        if (mainHandTypeName.contains("WOOD_")) return 1;
        if (mainHandTypeName.contains("STONE_")) return 2;
        if (mainHandTypeName.contains("IRON_")) return 3;
        if (mainHandTypeName.contains("DIAMOND_")) {
            return e.getEquipment().getItemInMainHand().getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) ? 6 : 4;
        }
        if (mainHandTypeName.contains("GOLD_")) return 5;
        return 0;
    }

    public static int getPlayerTier(Player e) {
        int tier = 0;
        for (ItemStack is : e.getInventory().getArmorContents()) {
            if (is != null && is.getType() != Material.AIR) {
                String armorTypeName = is.getType().name();
                if (armorTypeName.contains("LEATHER_")) tier = Math.max(Items.isBlueLeather(is) ? 6 : 1, tier);
                if (armorTypeName.contains("CHAINMAIL_")) tier = Math.max(2, tier);
                if (armorTypeName.contains("IRON_")) tier = Math.max(3, tier);
                if (armorTypeName.contains("DIAMOND_")) tier = Math.max(4, tier);
                if (armorTypeName.contains("GOLD_")) tier = Math.max(5, tier);
            }
        }
        return tier;
    }

    public static boolean isElite(LivingEntity e) {
        if (e.getEquipment().getChestplate().hasItemMeta() && e.getEquipment().getItemInMainHand().hasItemMeta()) {
            return e.getEquipment().getChestplate().getItemMeta().hasEnchants() || e.getEquipment().getItemInMainHand().getItemMeta().hasEnchants();
        }
        return false;
    }

    public static int getBarLength(int tier) {
        int barLength;
        switch (tier) {
            case 2:
                barLength = 30;
                break;
            case 3:
                barLength = 35;
                break;
            case 4:
                barLength = 40;
                break;
            case 5:
                barLength = 50;
                break;
            case 6:
                barLength = 60;
                break;
            default:
                barLength = 25;
                break;
        }
        return barLength;
    }


    public static String generateOverheadBar(LivingEntity ent, double health, double maxHealth, int tier) {
        boolean boss = isElite(ent);
        String str = ChatColor.RED + "";
        if (tier == 1) str = ChatColor.WHITE + "";
        if (tier == 2) str = ChatColor.GREEN + "";
        if (tier == 3) str = ChatColor.AQUA + "";
        if (tier == 4) str = ChatColor.LIGHT_PURPLE + "";
        if (tier == 5) str = ChatColor.YELLOW + "";
        double perc = health / maxHealth;
        int lines = 40;
        String barColor = crit.containsKey(ent) ? ChatColor.LIGHT_PURPLE.toString() : ChatColor.GREEN.toString();
        for (int i = 1; i <= lines; ++i) {
            str = perc >= (double) i / (double) lines ? str + barColor.toString() + "|" : str + ChatColor.GRAY + "|";
        }
        if (!boss) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public void onEnable() {
        PracticeServer.log.info("[Mobs] has been enabled.");
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(this, PracticeServer.plugin);

        BukkitRunnable mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (DeployCommand.patchlockdown) return;
                processMainTask();
            }
        };
        mainTask.runTaskTimer(PracticeServer.plugin, 20, 20);

        BukkitRunnable secondaryTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (DeployCommand.patchlockdown) return;
                processSecondaryTask();
            }
        };
        secondaryTask.runTaskTimer(PracticeServer.plugin, 20, 10);

        BukkitRunnable shootingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (DeployCommand.patchlockdown) return;
                processShootingTask();
            }
        };
        shootingTask.runTaskTimer(PracticeServer.plugin, 20L, 20L);
    }

    public void onDisable() {
        try {
            if (DeployCommand.patchlockdown) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    Location loc = player.getLocation();
                    Bukkit.getWorld("jew").getNearbyEntities(loc, 10, 10, 10).forEach(entity -> {
                        if (!MobHandler.isWorldBoss(entity)) entity.remove();
                    });
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PracticeServer.log.info("[Mobs] has been disabled.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(ProjectileHitEvent e) {
        Projectile pj = e.getEntity();
        if (pj.getShooter() instanceof LivingEntity && !(pj.getShooter() instanceof Player)) {
            LivingEntity d = (LivingEntity) pj.getShooter();
            Player target = null;
            for (Entity ent : pj.getNearbyEntities(2.0, 1.5, 2.0)) {
                if (ent instanceof Player) {
                    target = (Player) ent;
                    break;
                }
            }
            if (target != null) {
                if (pj instanceof SmallFireball) {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                }
                if (pj instanceof EnderPearl) {
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 2.0f, 1.5f);
                }
                target.damage(1.0, d);
            }
        }
    }

    private void processMainTask() {
        World mainWorld = Bukkit.getWorlds().get(0);
        for (Entity ent : mainWorld.getEntities()) {
            if (!(ent instanceof LivingEntity) || ent instanceof Player) continue;

            LivingEntity l = (LivingEntity) ent;
            processEliteEntities(l);
            processNamedEntities(l);
        }
    }

    private void processSecondaryTask() {
        World mainWorld = Bukkit.getWorlds().get(0);
        try {
            for (Entity ent : mainWorld.getEntities()) {
                if (!(ent instanceof LivingEntity) || ent instanceof Player) continue;

                LivingEntity l = (LivingEntity) ent;
                if (Mobs.crit.containsKey(l) && !Mobs.isElite(l) && !isGolemBoss(l)) {
                    int step = Mobs.crit.get(l);
                    if (step > 0) {
                        Mobs.crit.put(l, --step);
                        l.getWorld().playSound(l.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 2.0f);
                    }
                    if (step == 0) {
                        Particles.SPELL_WITCH.display(0.0f, 0.0f, 0.0f, 0.5f, 35, l.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);
                    }
                }
            }
        } catch (Exception e) {
            // Handle exception
        }
    }

    private void processShootingTask() {
        World mainWorld = Bukkit.getWorlds().get(0);
        for (Entity ent : mainWorld.getEntities()) {
            if (!(ent instanceof Creature)) continue;

            Creature c = (Creature) ent;
            if (c.getEquipment().getItemInMainHand() == null || !c.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) continue;
            if (Mobs.isElite(c) && Mobs.crit.containsKey(c)) return;
            if (!Mobs.isPlayerNearby(c) || c.getTarget() == null) continue;

            LivingEntity trgt = c.getTarget();
            if (c.getLocation().distanceSquared(trgt.getLocation()) > 9.0) {
                for (Staffs.StaffType type : Staffs.StaffType.values()) {
                    if (c.getEquipment().getItemInMainHand().getType() == type.itemType) Staffs.shootMagic(c, type);
                }
            }
        }
    }
    private void processEliteEntities(LivingEntity l) {
        if (Mobs.crit.containsKey(l) && Mobs.isElite(l) && !isGolemBoss(l)) {
            int step = Mobs.crit.get(l);
            if (step > 0) {
                processEliteEntityEffects(l, step);
                Mobs.crit.put(l, --step);
                l.getWorld().playSound(l.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 4.0f);
                Particles.EXPLOSION_LARGE.display(0.0f, 0.0f, 0.0f, 0.3f, 40, l.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);
            }
            if (step == 0) {
                processEliteEntityExplosion(l);
                l.setCustomName(Mobs.generateOverheadBar(l, l.getHealth(), l.getMaxHealth(), Mobs.getMobTier(l)));
                l.setCustomNameVisible(true);
                resetEliteEntityPotionEffects(l);
            }
        }
    }

    private void processEliteEntityEffects(LivingEntity l, int step) {
        if (isFrozenBoss(l)) {
            if (l.hasPotionEffect(PotionEffectType.SLOW)) {
                l.removePotionEffect(PotionEffectType.SLOW);
                if (l.getHealth() < (PracticeServer.t6 ? 100000 : 50000)) {
                    l.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 0), true);
                } else {
                    l.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 0), true);
                }
            }
            for (Entity e : l.getNearbyEntities(8.0, 8.0, 8.0)) {
                if (!(e instanceof Player)) continue;
                Player p = (Player) e;
                if (step > 0)
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 25, 1), true);
            }
        }
    }

    private void processEliteEntityExplosion(LivingEntity l) {
        Mobs.crit.remove(l);
        if (isFrozenBoss(l) && l.getHealth() < (PracticeServer.t6 ? 100000 : 50000)) {
            crit.put(l, 3);
        }
        try {
            ItemStack itemStack = l.getEquipment().getItemInMainHand();
            int min = Damage.getDamageRange(itemStack).get(0);
            int max = Damage.getDamageRange(itemStack).get(1);
            int dmg = (ThreadLocalRandom.current().nextInt(max - min + 1) + min) * 3;
            for (Entity e : l.getNearbyEntities(8.0, 8.0, 8.0)) {
                if (!(e instanceof Player)) continue;
                Listeners.mobd.remove(l.getUniqueId());
                Player p = (Player) e;
                crit.put(l, 0);
                p.damage(dmg, l);
                crit.remove(l);
                Vector v = p.getLocation().clone().toVector().subtract(l.getLocation().toVector());
                if (v.getX() != 0.0 || v.getY() != 0.0 || v.getZ() != 0.0) {
                    v.normalize();
                }
                if (isFrozenBoss(l)) {
                    p.setVelocity(v.multiply(-3));
                } else {
                    p.setVelocity(v.multiply(3));
                }
            }
        } catch (Exception e) {
        }

        l.getWorld().playSound(l.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        Particles.EXPLOSION_HUGE.display(0.0f, 0.0f, 0.0f, 1.0f, 40, l.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);
    }
    private void resetEliteEntityPotionEffects(LivingEntity l) {
        if (l.hasPotionEffect(PotionEffectType.SLOW)) {
            l.removePotionEffect(PotionEffectType.SLOW);
            if (l.getEquipment().getItemInMainHand() != null && l.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) {
                l.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 3), true);
            }
        }
        if (l.hasPotionEffect(PotionEffectType.JUMP)) {
            l.removePotionEffect(PotionEffectType.JUMP);
        }
    }

    private void processNamedEntities(LivingEntity l) {
        if (!Listeners.named.containsKey(l.getUniqueId()) || System.currentTimeMillis() - Listeners.named.get(l.getUniqueId()) < 5000)
            return;
        Listeners.named.remove(l.getUniqueId());
        String name = "";
        if (l.hasMetadata("name")) {
            name = l.getMetadata("name").get(0).asString();
        }
        if (!l.getType().equals(EntityType.ARMOR_STAND)) l.setCustomName(name);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        e.getEntity().getEquipment().clear();
    }

    @EventHandler
    public void onCubeSplit(SlimeSplitEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onCombust(EntityCombustEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player) && !e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            e.setCancelled(true);
            e.setDamage(0.0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKnockback(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity l = (LivingEntity) e.getEntity();
            if (e.getDamage() <= 0.0) {
                return;
            }
            if (!sound.containsKey(l.getUniqueId()) || sound.containsKey(l.getUniqueId()) && System.currentTimeMillis() - sound.get(l.getUniqueId()) > 500) {
                sound.put(l.getUniqueId(), System.currentTimeMillis());
                if (e.getEntity() instanceof Skeleton) {
                    if (e.getDamage() >= l.getHealth()) {
                        l.getWorld().playSound(l.getLocation(), Sound.ENTITY_SKELETON_DEATH, 1.0f, 1.0f);
                    }
                    l.getWorld().playSound(l.getLocation(), Sound.ENTITY_SKELETON_HURT, 1.0f, 1.0f);
                }
                if (e.getEntity() instanceof Zombie) {
                    if (e.getDamage() >= l.getHealth()) {
                        l.getWorld().playSound(l.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1.0f, 1.0f);
                    }
                    l.getWorld().playSound(l.getLocation(), Sound.ENTITY_ZOMBIE_HURT, 1.0f, 1.0f);
                }
                if ((e.getEntity() instanceof Spider || e.getEntity() instanceof CaveSpider) && e.getDamage() >= l.getHealth()) {
                    l.getWorld().playSound(l.getLocation(), Sound.ENTITY_SPIDER_DEATH, 1.0f, 1.0f);
                }
                if ((e.getEntity() instanceof Silverfish && e.getDamage() >= l.getHealth())) {
                    l.getWorld().playSound(l.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1.0f, 1.0f);
                }
                if (e.getEntity() instanceof PigZombie) {
                    if (e.getDamage() >= l.getHealth()) {
                        l.getWorld().playSound(l.getLocation(), Sound.ENTITY_ZOMBIE_PIG_DEATH, 1.0f, 1.0f);
                    }
                    l.getWorld().playSound(l.getLocation(), Sound.ENTITY_ZOMBIE_PIG_HURT, 1.0f, 1.0f);
                }

            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER && e.getTarget() instanceof Player && e.getEntity() instanceof Creature) {
            Creature l = (Creature) e.getEntity();
            Player p = (Player) e.getTarget();
            if (p.hasMetadata("NPC")) {
                e.setCancelled(true);
                e.setTarget(null);
                return;
            }
            if (getPlayerTier(p) - getMobTier(l) > 2) {
                e.setCancelled(true);
                e.setTarget(null);
                return;
            }
            if (l.hasPotionEffect(PotionEffectType.SLOW)) {

                if (l.getEquipment().getItemInMainHand() != null && l.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) {
                    l.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                } else {
                    l.removePotionEffect(PotionEffectType.SLOW);
                }
            }
            if (l.hasPotionEffect(PotionEffectType.JUMP)) {
                l.removePotionEffect(PotionEffectType.JUMP);
            }
            if (e.getEntity().getLocation().distance(e.getTarget().getLocation()) > 15) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void fixPigmenAggro(EntityTargetEvent e) {
        if (e.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER && e.getTarget() instanceof Player && e.getEntity() instanceof PigZombie) {
            if (e.getEntity().getLocation().distance(e.getTarget().getLocation()) > 15) {
                e.setCancelled(true);
            }
        } else if (e.getEntity() instanceof PigZombie) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onEntityTargetLastHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Creature && e.getDamager() instanceof Player) {
            Creature c = (Creature) e.getEntity();
            Player p = (Player) e.getDamager();
            if (target.containsKey(c) && target.get(c) != null) {
                if (p.getLocation().distanceSquared(c.getLocation()) < target.get(c).getLocation().distanceSquared(c.getLocation())) {
                    c.setTarget(p);
                    target.put(c, p);
                }
            } else {
                c.setTarget(p);
                target.put(c, p);
            }
        }
    }

    //
    // Knockback Retaliation.
    //

    @EventHandler
    public void onMobHitSpider(EntityDamageEvent e) {
        if (e.getEntity() instanceof Spider || e.getEntity() instanceof CaveSpider || e.getEntity() instanceof Skeleton || e.getEntity() instanceof Zombie || e.getEntity() instanceof PigZombie) {
            Entity m = e.getEntity();
            m.setVelocity(m.getLocation().getDirection().multiply(0.09));
        }
    }
    // ----------------------Knockback End-------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobDeath(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player)) {
            LivingEntity s = (LivingEntity) e.getEntity();
            if (e.getDamage() >= s.getHealth() && crit.containsKey(s)) {
                crit.remove(s);
                String mname = "";
                if (s.getEquipment().getItemInMainHand() != null && s.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                    mname = Mobs.generateOverheadBar(s, 0.0, s.getMaxHealth(), Mobs.getMobTier(s));
                    s.setCustomName(mname);
                    s.setCustomNameVisible(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCrit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player) && e.getDamager() instanceof Player) {
            if (e.getDamage() <= 0.0) {
                return;
            }
            LivingEntity s = (LivingEntity) e.getEntity();
            Random random = new Random();
            int rcrt = random.nextInt(150) + 1;
            if (!crit.containsKey(s) && (Mobs.getMobTier(s) == 1 && rcrt <= 5 || Mobs.getMobTier(s) == 2 && rcrt <= 7 || Mobs.getMobTier(s) == 3 && rcrt <= 10 || Mobs.getMobTier(s) == 4 && rcrt <= 13 || Mobs.getMobTier(s) >= 5 && rcrt <= 20)) {

                if (!(isGolemBoss(s) && getGolemStage(s) == 3)) crit.put(s, 4);
                if (Mobs.isElite(s) && !isGolemBoss(s)) {
                    s.getWorld().playSound(s.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 4.0f);
                    double max = s.getMaxHealth();
                    double hp = s.getHealth() - e.getDamage();
                    s.setCustomName(Mobs.generateOverheadBar(s, hp, max, Mobs.getMobTier(s)));
                    s.setCustomNameVisible(true);
                    Listeners.named.put(s.getUniqueId(), System.currentTimeMillis());
                    if (isFrozenBoss(s)) {
                        for (Entity x : s.getNearbyEntities(8.0, 8.0, 8.0)) {
                            if (!(x instanceof Player)) continue;
                            Player p = (Player) x;
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 1), true);
                        }
                    } else {
                        s.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10), true);
                        s.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 127), true);
                    }
                } else {
                    s.getWorld().playSound(s.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 2.0f);
                    double max = s.getMaxHealth();
                    double hp = s.getHealth() - e.getDamage();
                    s.setCustomName(Mobs.generateOverheadBar(s, hp, max, Mobs.getMobTier(s)));
                    s.setCustomNameVisible(true);
                    Listeners.named.put(s.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    public boolean isSafeSpot(Player player, LivingEntity mob) {
        Location target = player.getLocation();
        Location mobLoc = mob.getLocation();
        // Check if there is a clear line of sight between the player and the mob
        if (!mob.hasLineOfSight(player)) {
            return true;
        }
        double distance = target.distanceSquared(mob.getLocation());

        if (mob.getLocation().getBlock().isLiquid()) {
            return true;
        }
        // Check if there is a solid block between the player and the mob
        if (target.getBlockY() > (mob.getLocation().getBlockY()) && (mob.getLocation().clone().add(0, 1, 0).getBlock().isLiquid())) {
            return true;
        }
        return distance >= 3 && distance <= 6 * 6 && target.getBlockY() > (mob.getLocation().clone().getBlockY() + 1);
        // If none of the above conditions are met, the player is not safespotting
    }


    @EventHandler
    public void onSafeSpot(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player) && e.getDamager() instanceof Player) {
            if (e.getDamage() <= 0.0) {
                return;
            }
            LivingEntity s = (LivingEntity) e.getEntity();
            if (s.getType().equals(EntityType.ARMOR_STAND)) return;

            Player p = (Player) e.getDamager();
            if (isSafeSpot(p, s)) {
                s.teleport(p.getLocation().clone().add(0, 1, 0));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMobHitMob(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof LivingEntity && !(e.getDamager() instanceof Player) && !(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            e.setDamage(0.0);
        }
    }

    @EventHandler
    public void golemFix(EntityTargetEvent e) {
        if (e.getEntity() instanceof Creature && !(e.getTarget() instanceof Player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMobHit(EntityDamageByEntityEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (e.getDamager() instanceof LivingEntity && !(e.getDamager() instanceof Player) && e.getEntity() instanceof Player) {
            LivingEntity s = (LivingEntity) e.getDamager();
            Player p = (Player) e.getEntity();
            Random random = new Random();
            int dmg = 1;
            if (s.getEquipment().getItemInMainHand() != null && s.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                int min = Damage.getDamageRange(s.getEquipment().getItemInMainHand()).get(0);
                int max = Damage.getDamageRange(s.getEquipment().getItemInMainHand()).get(1);
                dmg = random.nextInt(max - min + 1) + min + 1;
            }
            if (crit.containsKey(s) && crit.get(s) == 0) {
                dmg = Mobs.isElite(s) ? (dmg *= 4) : (dmg *= 3);
                if (!Mobs.isElite(s)) {
                    crit.remove(s);
                }
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.3f);
                double max = s.getMaxHealth();
                double hp = s.getHealth() - e.getDamage();
                s.setCustomName(Mobs.generateOverheadBar(s, hp, max, Mobs.getMobTier(s)));
                s.setCustomNameVisible(true);
                Listeners.named.put(s.getUniqueId(), System.currentTimeMillis());
            }
            if (e.getDamage() <= 0.0) {
                return;
            }
            if (s.getEquipment().getItemInMainHand().getType().name().contains("WOOD_")) {
                dmg = s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants() ? (int) ((double) dmg * 2.5) : (int) ((double) dmg * 0.8);
            } else if (s.getEquipment().getItemInMainHand().getType().name().contains("STONE_")) {
                dmg = s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants() ? (int) ((double) dmg * 2.5) : (int) ((double) dmg * 0.9);
            } else if (s.getEquipment().getItemInMainHand().getType().name().contains("IRON_")) {
                dmg = s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants() ? (dmg *= 3) : (int) ((double) dmg * 1.2);
            } else if (s.getEquipment().getItemInMainHand().getType().name().contains("DIAMOND_") && !s.getEquipment().getArmorContents()[0].getType().name().contains("LEATHER_")) {
                dmg = s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants() ? (dmg *= 5) : (int) ((double) dmg * 1.4);
            } else if (s.getEquipment().getItemInMainHand().getType().name().contains("GOLD_")) {
                dmg = s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants() ? (dmg *= 6) : (dmg *= 2);
            } else if (s.getEquipment().getItemInMainHand().getType().name().contains("DIAMOND_")) {
                dmg = s.getEquipment().getItemInMainHand().getItemMeta().hasEnchants() ? (dmg *= 8) : (dmg *= 4);
            }
            if (s instanceof MagmaCube) {
                dmg = (int) ((double) dmg * 0.5);
            }
            if (dmg < 1) {
                dmg = 1;
            }
            e.setDamage(dmg);
        }
    }

}
