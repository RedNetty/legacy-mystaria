package me.retrorealms.practiceserver.mechanics.damage;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.player.Buddies;
import me.retrorealms.practiceserver.mechanics.player.Energy;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.BoundingBox;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.RayTrace;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Staffs implements Listener {
    private static final String STAFF_COOLDOWN = "staffCD";
    private static HashMap<Player, ItemStack> staff = new HashMap<>();
    private static HashMap<Player, Long> staffShotTime = new HashMap<>();
    public void onEnable() {
        PracticeServer.log.info("[Staffs] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[Staffs] has been disabled.");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG) {
            event.setCancelled(true);
        }
    }

    public static HashMap<Player, ItemStack> getStaff() {
        return staff;
    }

    @EventHandler
    public void onStaffShot(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && player.getInventory().getItemInMainHand() != null
                && player.getInventory().getItemInMainHand().getType() != Material.AIR
                && player.getInventory().getItemInMainHand().getType().name().contains("_HOE")
                && player.getInventory().getItemInMainHand().getItemMeta().hasLore()) {

            if (Alignments.isSafeZone(player.getLocation())) {
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.25f);
                Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 0.5f, 20, player.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);
            } else {
                if (player.hasMetadata(STAFF_COOLDOWN) && player.getMetadata(STAFF_COOLDOWN).get(0).asLong() > System.currentTimeMillis()) {
                    return;
                }

                if (Energy.getEnergy(player) > 0.0f) {
                    for (StaffType staffType : StaffType.values()) {
                        if (player.getInventory().getItemInMainHand().getType() == staffType.itemType) {
                            shootMagic(player, staffType);
                            Energy.removeEnergy(player, staffType.nrgCost);
                            player.getInventory().getItemInMainHand().setDurability((short) 0);
                            player.setMetadata(STAFF_COOLDOWN, new FixedMetadataValue(PracticeServer.plugin, System.currentTimeMillis() + 350L)); // Slightly slower firing rate
                            break;
                        }
                    }
                } else {
                    Energy.setEnergy(player, 0.0f);
                    Energy.cooldown.put(player.getName(), System.currentTimeMillis());
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 5), true);
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 10.0f, 1.5f);
                }
            }
        }
    }
    public static ItemStack getLastUsedStaff(Player player) {
        return staff.get(player);
    }

    public static boolean isRecentStaffShot(Player player) {
        Long shotTime = staffShotTime.get(player);
        return shotTime != null && System.currentTimeMillis() - shotTime < 5000; // 5 seconds threshold
    }

    public static void clearStaffShot(Player player) {
        staff.remove(player);
        staffShotTime.remove(player);
    }

    public static void shootMagic(LivingEntity shooter, StaffType type) {
        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1F, 1.5F);

        new BukkitRunnable() {
            final RayTrace magic = new RayTrace(shooter.getEyeLocation().add(0, -0.25, 0).toVector(), shooter.getEyeLocation().getDirection());
            final int distanceSpeed = shooter instanceof Player ? 150 : 60;
            final double precision = shooter instanceof Player ? .8 : .3;
            Iterator<Vector> trail = magic.traverse(distanceSpeed, precision).iterator();
            int ticks = 0;
            boolean hasHit = false;
            @Override
            public void run() {
                try {
                    if (!trail.hasNext() || ticks > 70 || hasHit) { // Maximum 2 seconds flight time (increased from 1 second)
                        this.cancel();
                        return;
                    }

                    for (int i = 0; i < 4; i++) { // Process multiple positions per tick for even faster apparent speed
                        if (!trail.hasNext()) break;
                        Vector pos = trail.next();
                        Block block = shooter.getWorld().getBlockAt(pos.toLocation(shooter.getWorld()));

                        if (block != null && block.getType().isSolid() && magic.intersectsBox(pos, new BoundingBox(block))) {
                            createImpactEffect(pos.toLocation(shooter.getWorld()), type);
                            hasHit = true;
                            this.cancel();
                            return;
                        }

                        for (Entity entity : shooter.getWorld().getNearbyEntities(pos.toLocation(shooter.getWorld()), 1.5F, 1.5F, 1.5F)) {
                            if (!(entity instanceof LivingEntity) || entity == shooter || hasHit) {
                                continue;
                            }

                            LivingEntity livingEntity = (LivingEntity) entity;

                            if (shouldIgnoreDamage(shooter, livingEntity)) {
                                continue;
                            }

                            if (magic.intersectsBox(pos, .8, new BoundingBox(livingEntity))) {
                                if (livingEntity instanceof Horse && livingEntity.getPassenger() != null) {
                                    handleHorsePassengerDamage(shooter, livingEntity);
                                } else if (shooter instanceof Player && shooter.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) {
                                    handlePlayerStaffDamage(shooter, livingEntity);
                                } else {
                                    handleDefaultDamage(shooter, livingEntity);
                                }
                                createImpactEffect(pos.toLocation(shooter.getWorld()), type);
                                hasHit = true;
                                this.cancel();
                                return;
                            }
                        }

                        createTrailEffect(pos.toLocation(shooter.getWorld()), type);
                    }
                    ticks++;
                } catch (Exception e) {
                    // Handle any exceptions
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 0L, 1L);
    }

    private static void createTrailEffect(Location location, StaffType type) {
        Particles.SPELL_MOB.display(type.color, location, 30D);
        Particles.REDSTONE.display(type.color, location, 30D);
    }

    private static void createImpactEffect(Location location, StaffType type) {
        Particles.CRIT_MAGIC.display(0F, 0F, 0F, 0.2F, 5, location, 30D);
        Particles.SPELL_MOB.display(type.color, location, 30D);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.2F);
    }

    private static boolean shouldIgnoreDamage(LivingEntity shooter, LivingEntity target) {
        if (shooter instanceof Player && target instanceof Player) {
            Player playerShooter = (Player) shooter;
            Player playerTarget = (Player) target;

            if (Duels.duelers.containsKey(playerTarget)) {
                return true;
            }

            if (GuildMechanics.getInstance().isInSameGuild(playerShooter, playerTarget)) {
                return true;
            }

            if (Parties.arePartyMembers(playerShooter, playerTarget)) {
                return true;
            }

            if (shouldIgnorePVP(playerShooter, playerTarget)) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldIgnorePVP(Player shooter, Player target) {
        ArrayList<String> buddies = Buddies.getBuddies(shooter.getName().toLowerCase());

        if (buddies.contains(target.getName().toLowerCase()) && !Toggles.isToggled(shooter, "Friendly Fire")) {
            return true;
        }

        if (Toggles.isToggled(shooter,"Anti PVP")) {
            return true;
        }

        if (!Alignments.neutral.containsKey(target.getName()) && !Alignments.chaotic.containsKey(target.getName()) && Toggles.isToggled(shooter,"Chaotic")) {
            return true;
        }

        return false;
    }

    private static void handleHorsePassengerDamage(LivingEntity shooter, LivingEntity horse) {
        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            ArrayList<String> buddies = Buddies.getBuddies(player.getName());

            if (buddies.contains(horse.getPassenger().getName().toLowerCase()) && !Toggles.isToggled(player,"Friendly Fire")) {
                return;
            }

            if (Toggles.isToggled(player,"Anti PVP")) {
                return;
            }

            if (!Alignments.neutral.containsKey(horse.getName()) && !Alignments.chaotic.containsKey(horse.getName()) && Toggles.isToggled(player,"Chaotic")) {
                return;
            }

            horse.damage(1);
            horse.remove();
        } else {
            horse.damage(1);
            horse.remove();
        }
    }

    private static void handlePlayerStaffDamage(LivingEntity shooter, LivingEntity target) {
        staff.put((Player) shooter, ((Player) shooter).getInventory().getItemInMainHand());
        target.damage(1, shooter);
        staff.remove((Player) shooter);
    }

    private static void handleDefaultDamage(LivingEntity shooter, LivingEntity target) {
        target.damage(1, shooter);
    }

    public enum StaffType {
        T1(new Particles.OrdinaryColor(Color.WHITE), Material.WOOD_HOE, 7),
        T2(new Particles.OrdinaryColor(Color.GREEN), Material.STONE_HOE, 8),
        T3(new Particles.OrdinaryColor(Color.AQUA), Material.IRON_HOE, 9),
        T4(new Particles.OrdinaryColor(Color.NAVY), Material.DIAMOND_HOE, 10),
        T5(new Particles.OrdinaryColor(Color.YELLOW), Material.GOLD_HOE, 11);

        public final Particles.OrdinaryColor color;
        public final Material itemType;
        public final int nrgCost;

        StaffType(Particles.OrdinaryColor color, Material itemType, int nrgCost) {
            this.color = color;
            this.itemType = itemType;
            this.nrgCost = nrgCost;
        }
    }
}
