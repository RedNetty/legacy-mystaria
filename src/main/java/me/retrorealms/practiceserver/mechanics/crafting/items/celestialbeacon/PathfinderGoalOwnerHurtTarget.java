package me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon;

import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.PathfinderGoalTarget;
import org.bukkit.event.entity.EntityTargetEvent;

// PathfinderGoalOwnerHurtTarget class
public class PathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {
    private final EntityCreature entity;
    private EntityLiving target;
    private int timestamp;

    public PathfinderGoalOwnerHurtTarget(EntityCreature entity) {
        super(entity, false);
        this.entity = entity;
        this.a(1); // Priority
    }

    @Override
    public boolean a() {
        EntityLiving owner = ((CelestialAlly) this.entity).getEntityOwner();
        if (owner == null) {
            return false;
        } else {
            this.target = owner.bU();
            int i = owner.bV();
            return i != this.timestamp && this.a(this.target, false);
        }
    }

    @Override
    public void c() {
        this.entity.setGoalTarget(this.target, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving owner = ((CelestialAlly) this.entity).getEntityOwner();
        if (owner != null) {
            this.timestamp = owner.bV();
        }
        super.c();
    }
}
