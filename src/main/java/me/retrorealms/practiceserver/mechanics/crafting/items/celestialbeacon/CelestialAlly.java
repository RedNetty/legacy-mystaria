package me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon;

import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.utils.StringUtil;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.SoundCategory;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.UUID;

@Getter
public class CelestialAlly extends EntityZombie implements Listener {
    private Player owner;
    private UUID ownerUUID;
    private EntityHuman entityOwner;
    private EntityLiving currentTarget;
    private static final float BASE_DAMAGE = 500.0f;
    private static final int FOLLOW_RANGE = 40;

    public CelestialAlly(World world) {
        super(world);
    }

    public CelestialAlly(Location location, Player owner) {
        super(((CraftWorld) location.getWorld()).getHandle());

        if (location == null || owner == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Location, owner, or world cannot be null");
        }

        this.setPosition(location.getX(), location.getY(), location.getZ());
        this.setCustomName(ChatColor.GOLD + "Celestial Ally");
        this.setCustomNameVisible(true);
        this.owner = owner;
        this.ownerUUID = owner.getUniqueId();
        this.entityOwner = ((CraftPlayer) owner).getHandle();

        setupAttributes();
        setupEffects(location);
        setMetadata();

        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    private void setupAttributes() {
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(100.0D);
        this.setHealth(this.getMaxHealth());
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(BASE_DAMAGE);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(FOLLOW_RANGE);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3D);
    }

    private void setupEffects(Location location) {
        this.setOnFire(Integer.MAX_VALUE);
        location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
        location.getWorld().playSound(location, Sound.ENTITY_ENDERDRAGON_GROWL, 1.0f, 1.0f);
    }



    private void setMetadata() {
        org.bukkit.entity.Entity bukkitEntity = this.getBukkitEntity();
        bukkitEntity.setMetadata("CelestialAlly", new FixedMetadataValue(PracticeServer.getInstance(), ownerUUID.toString()));
    }

    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, true));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.9D, 32.0F));
        this.goalSelector.a(3, new PathfinderGoalFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomStrollLand(this, 0.6D));
        this.goalSelector.a(5, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(1, new PathfinderGoalOwnerHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalOwnerHurtTarget(this));
        this.targetSelector.a(3, new PathfinderGoalHurtByTarget(this, true));
    }
    private void spoofAttackAnimation() {
        this.s();
    }
    @Override
    public void n() {
        super.n();

        if (this.ticksLived % 20 == 0) { // Every second
            checkAndUpdateTarget();
        }

        if (this.currentTarget != null && this.currentTarget.isAlive() && canTarget(this.currentTarget)) {
            double distance = this.h(this.currentTarget);
            if (distance <= 2.0D) {
                this.attackEntity(this.currentTarget);
            } else {
                this.getNavigation().a(this.currentTarget, 1.0D);
            }
        } else {
            this.currentTarget = null;
        }
    }

    private void attackEntity(EntityLiving target) {
        if (this.ticksLived % 10 == 0) {
            boolean attacked = this.B(target);
        }
    }

    private boolean directAttack(EntityLiving target) {
        float damage = (float) this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
        if(target == this || !canTarget(target)) return false;
        CelestialAllyDamageEvent event = new CelestialAllyDamageEvent(
                this,
                owner,
                target.getBukkitEntity(),
                damage
        );

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            double finalDamage = event.getDamage();

            if (target.getHealth() <= finalDamage) {
                handleMobDeath(target.getBukkitEntity());
            } else {
                target.setHealth((float) (target.getHealth() - finalDamage));
            }
            ((WorldServer) this.world).getTracker().a(this, new PacketPlayOutAnimation(this, 0));
            target.world.broadcastEntityEffect(target, (byte)33);
            target.a(this, 0.5F, MathHelper.sin(this.yaw * 0.017453292F), -MathHelper.cos(this.yaw * 0.017453292F));
            if (Toggles.isToggled(owner, "Debug")) {
                String targetName = target.getCustomName();
                StringUtil.sendCenteredMessage(owner, ChatColor.RED + "" + (int) finalDamage + ChatColor.RED + ChatColor.BOLD + " DMG " +
                        ChatColor.RED + "-> " + ChatColor.RESET + targetName + " [" +
                        (int) target.getHealth() + "HP] &e(Celestial Ally)");
            }

            return true;
        }

        return false;
    }

    private void handleMobDeath(org.bukkit.entity.Entity bukkitEntity) {
        if (bukkitEntity instanceof LivingEntity && MobHandler.isMobOnly(bukkitEntity)) {
            LivingEntity target = (LivingEntity) bukkitEntity;

            try {
                EntityDamageByEntityEvent dummyEvent = new EntityDamageByEntityEvent(
                        this.getBukkitEntity(),
                        target,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        target.getHealth()
                );

                Mobdrops mobdrops = new Mobdrops();
                mobdrops.handleMobDeath(target, owner, dummyEvent);
            } catch (Exception e) {
                PracticeServer.log.warning("Error in CelestialAlly handleMobDeath: " + e.getMessage());
                e.printStackTrace();
            }

            target.setHealth(0);
            ((CraftLivingEntity) target).getHandle().die();
        }
    }

    @Override
    public boolean B(Entity entity) {
        if (entity instanceof EntityLiving) {
            return this.directAttack((EntityLiving) entity);
        }
        return false;
    }

    @EventHandler
    public void onOwnerDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            LivingEntity target = (LivingEntity) event.getEntity();

            if (player.equals(owner)) {
                EntityLiving entityTarget = ((CraftLivingEntity) target).getHandle();
                if (entityTarget != null && entityTarget.isAlive()) {
                    this.setGoalTarget(entityTarget, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
                    this.getNavigation().a(entityTarget, 1.0D);
                    this.currentTarget = entityTarget;
                }
            }
        }
    }

    public boolean canTarget(EntityLiving entityLiving) {
        if (entityLiving == null || entityOwner == null) {
            return false;
        }

        // Check if the target is the owner
        if (entityLiving.equals(entityOwner) || (entityLiving.getBukkitEntity() instanceof Player &&
                ((Player) entityLiving.getBukkitEntity()).getUniqueId().equals(ownerUUID))) {
            return false;
        }

        LivingEntity livingEntity = (LivingEntity) entityLiving.getBukkitEntity();
        if (livingEntity == null) {
            return false;
        }

        if (livingEntity instanceof Player) {
            Player target = (Player) livingEntity;
            if (owner != null && Parties.arePartyMembers(target, owner)) {
                return false;
            }
        }

        return true;
    }

    public void checkAndUpdateTarget() {
        EntityLiving ownerTarget = null;
        if (entityOwner != null) {
            ownerTarget = this.entityOwner.bU();
        }

        if (ownerTarget != null && ownerTarget.isAlive()) {
            if (ownerTarget != this.currentTarget) {
                this.currentTarget = ownerTarget;
                if (canTarget(currentTarget)) {  // Add null check here
                    this.setGoalTarget(ownerTarget, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
                }
            }
        } else if (this.currentTarget == null || !this.currentTarget.isAlive()) {
            EntityLiving newTarget = findNearestTarget();
            if (newTarget != null && canTarget(newTarget)) {  // Add null check here
                this.currentTarget = newTarget;
                this.setGoalTarget(newTarget, EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
            }
        }
    }

    private EntityLiving findNearestTarget() {
        List<Entity> nearbyEntities = this.world.getEntities(this, this.getBoundingBox().grow(FOLLOW_RANGE, 4.0D, FOLLOW_RANGE));
        EntityLiving nearestTarget = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof EntityLiving && !(entity instanceof EntityHuman) && !(entity instanceof CelestialAlly)) {
                double distanceSquared = this.h(entity);
                if (distanceSquared < nearestDistanceSquared) {
                    nearestTarget = (EntityLiving) entity;
                    nearestDistanceSquared = distanceSquared;
                }
            }
        }

        return nearestTarget;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCelestialAllyDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() == this.getBukkitEntity()) {
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (damager.getUniqueId().equals(this.ownerUUID)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private class PathfinderGoalFollowOwner extends PathfinderGoal {
        private final EntityCreature entity;
        private EntityLiving owner;
        private final double speed;
        private final float maxDist;
        private final float minDist;
        private int timeToRecalcPath;

        public PathfinderGoalFollowOwner(EntityCreature entity, double speed, float maxDist, float minDist) {
            this.entity = entity;
            this.speed = speed;
            this.maxDist = maxDist;
            this.minDist = minDist;
        }

        @Override
        public boolean a() {
            EntityLiving entityliving = entityOwner;
            if (entityliving == null) {
                return false;
            } else if (entityliving.dead) {
                return false;
            } else {
                entityOwner = (EntityHuman) entityliving;
                return true;
            }
        }

        @Override
        public void c() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void d() {
            entityOwner = null;
        }

        @Override
        public void e() {
            if (CelestialAlly.this.getGoalTarget() != null && CelestialAlly.this.getGoalTarget().isAlive()) {
                return; // Do not follow the owner if there is an active target.
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (!this.entity.getNavigation().a(entityOwner, this.speed)) {
                    if (this.entity.h(entityOwner) > (double) (this.maxDist * this.maxDist)) {
                        int x = MathHelper.floor(entityOwner.locX) - 2;
                        int y = MathHelper.floor(entityOwner.getBoundingBox().b);
                        int z = MathHelper.floor(entityOwner.locZ) - 2;

                        for (int l = 0; l <= 4; ++l) {
                            for (int i1 = 0; i1 <= 4; ++i1) {
                                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.a(x, y, z, l, i1)) {
                                    this.entity.setPositionRotation((double) ((float) (x + l) + 0.5F), (double) y, (double) ((float) (z + i1) + 0.5F), this.entity.yaw, this.entity.pitch);
                                    this.entity.getNavigation().o();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        protected boolean a(int x, int y, int z, int offsetX, int offsetZ) {
            BlockPosition blockposition = new BlockPosition(x + offsetX, y - 1, z + offsetZ);
            IBlockData iblockdata = this.entity.world.getType(blockposition);
            return iblockdata.d(this.entity.world, blockposition, EnumDirection.DOWN) == EnumBlockFaceShape.SOLID && iblockdata.a(this.entity) && this.entity.world.isEmpty(blockposition.up()) && this.entity.world.isEmpty(blockposition.up(2));
        }
    }

    public static void registerEntity() {
        try {
            MinecraftKey key = new MinecraftKey("celestial_ally");
            EntityTypes.b.a(54, key, CelestialAlly.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error registering CelestialAlly entity: " + e.getMessage());
        }
    }

}