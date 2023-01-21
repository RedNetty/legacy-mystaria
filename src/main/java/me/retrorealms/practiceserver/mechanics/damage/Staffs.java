/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.entity.Snowball
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LargeFireball
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.SmallFireball
 *  org.bukkit.entity.Snowball
 *  org.bukkit.entity.WitherSkull
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockIgniteEvent
 *  org.bukkit.event.block.BlockIgniteEvent$IgniteCause
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityExplodeEvent
 *  org.bukkit.event.entity.ExplosionPrimeEvent
 *  org.bukkit.event.entity.ProjectileHitEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.projectiles.ProjectileSource
 *  org.bukkit.util.Vector
 */
package me.retrorealms.practiceserver.mechanics.damage;

import com.google.common.collect.Lists;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.player.Energy;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.utils.BoundingBox;
import me.retrorealms.practiceserver.utils.Particles;
import me.retrorealms.practiceserver.utils.RayTrace;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
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

import java.util.*;

public class Staffs implements Listener {
    public static HashMap<Player, ItemStack> staff = new HashMap<>();
    public PracticeServer m;

    private static final String STAFF_COOLDOWN = "staffCD";

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

    @EventHandler
    public void onStaffShot(PlayerInteractEvent e) {
        Player p;
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && (p = e.getPlayer()).getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR && p.getInventory().getItemInMainHand().getType().name().contains("_HOE") && p.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
            if (Alignments.isSafeZone(p.getLocation())) {
                p.playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.25f);
                Particles.CRIT_MAGIC.display(0.0f, 0.0f, 0.0f, 0.5f, 20, p.getLocation().add(0.0, 1.0, 0.0), 20.0);
            } else {
                if (Energy.nodamage.containsKey(p.getName()) && System.currentTimeMillis() - Energy.nodamage.get(p.getName()) < 100) {
                    e.setCancelled(true);
                    return;
                }

                if (p.hasMetadata(STAFF_COOLDOWN) && (p.getMetadata(STAFF_COOLDOWN).get(0).asLong() > System.currentTimeMillis())) {
                    e.setCancelled(true);
                    return;
                }

                if (Energy.getEnergy(p) > 0.0f) {
                    for (StaffType staff : StaffType.values()) {
                        if (p.getInventory().getItemInMainHand().getType() == staff.itemType) {
                            shootMagic(p, staff);
                            Energy.removeEnergy(p, staff.nrgCost);
                            p.getInventory().getItemInMainHand().setDurability((short) 0);
                            p.setMetadata(STAFF_COOLDOWN, new FixedMetadataValue(PracticeServer.plugin, System.currentTimeMillis() + 100L));
                        }
                    }
                } else {
                    Energy.setEnergy(p, 0.0f);
                    Energy.cd.put(p.getName(), System.currentTimeMillis());
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 5), true);
                    p.playSound(p.getLocation(), Sound.ENTITY_WOLF_PANT, 10.0f, 1.5f);
                }
            }
        }
    }

    public static void shootMagic(LivingEntity shooter, StaffType type) {
        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_ENDERPEARL_THROW, 1F, 1F);

        new BukkitRunnable() {
            RayTrace magic = new RayTrace(shooter.getEyeLocation().add(0, -0.25, 0).toVector(), shooter.getEyeLocation().getDirection());
            Iterator trail = magic.traverse(15, 1).iterator();

            @Override
            public void run() {
                if (!trail.hasNext()) {
                    this.cancel();
                    return;
                }

                Vector pos = (Vector) trail.next();
                Block block = shooter.getWorld().getBlockAt(pos.toLocation(shooter.getWorld()));

                if (block != null && block.getType().isSolid() && magic.intersectsBox(pos, new BoundingBox(block))) {
                    Particles.CLOUD.display(0F, 0F, 0F, 0.5F, 15, pos.toLocation(shooter.getWorld()), 30D);
                    this.cancel();
                    return;
                }

                for (Entity e : shooter.getWorld().getNearbyEntities(pos.toLocation(shooter.getWorld()), 1F, 1F ,1F)) {
                    if (!(e instanceof LivingEntity))
                        return;

                    if (e == shooter)
                        return;

                    LivingEntity le = (LivingEntity) e;

                    if (magic.intersectsBox(pos, 0.5, new BoundingBox(le))) {
                        if (le instanceof Player && shooter instanceof Player) {
                            if (Duels.duelers.containsKey(le))
                                continue;

                            if (GuildMechanics.getInstance().isInSameGuild((Player) shooter, (Player) le)) {
                                continue;
                            }
                            if (Parties.arePartyMembers((Player) shooter, (Player) le)) {
                                continue;
                            }

                            if (Toggles.getToggles(((Player) shooter).getUniqueId()).contains("Anti PVP")
                                    || (Toggles.getToggles(((Player) shooter).getUniqueId()).contains("Chaotic")
                                    && Alignments.get((Player) le).equals("&aLAWFUL"))) {
                                continue;
                            }
                        }
                        if(le instanceof Horse && le.getPassenger() != null) {
                            le.damage(1);
                            le.remove();
                        }
                        if(!shooter.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) continue;

                        if (shooter instanceof Player && shooter.getEquipment().getItemInMainHand().getType().name().contains("_HOE")) {
                            staff.put((Player) shooter, ((Player) shooter).getInventory().getItemInMainHand());
                            le.damage(1, shooter);
                            staff.remove((Player) shooter);
                        } else {
                            le.damage(1, shooter);
                        }
                        Particles.VILLAGER_HAPPY.display(1F, 1F, 1F, 0.75F, 10, pos.toLocation(shooter.getWorld()), 30D);
                        this.cancel();
                        break;
                    }
                }

                Particles.REDSTONE.display(type.color, pos.toLocation(shooter.getWorld()), 30D);
                Particles.REDSTONE.display(type.color, pos.toLocation(shooter.getWorld()), 30D);
            }
        }.runTaskTimer(PracticeServer.plugin, 0L, 1L);
    }

    public enum StaffType {
        T1(new Particles.OrdinaryColor(Color.WHITE), Material.WOOD_HOE, 7),
        T2(new Particles.OrdinaryColor(Color.GREEN), Material.STONE_HOE, 8),
        T3(new Particles.OrdinaryColor(Color.AQUA), Material.IRON_HOE, 9),
        T4(new Particles.OrdinaryColor(Color.NAVY), Material.DIAMOND_HOE, 10),
        T5(new Particles.OrdinaryColor(Color.YELLOW), Material.GOLD_HOE, 11);

        public Particles.OrdinaryColor color;
        public Material itemType;
        public int nrgCost;

        StaffType(Particles.OrdinaryColor color, Material itemType, int nrgCost) {
            this.color = color;
            this.itemType = itemType;
            this.nrgCost = nrgCost;
        }
    }
}

