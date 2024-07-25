package me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon;

import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.PathfinderGoalTarget;
import org.bukkit.event.entity.EntityTargetEvent;

public class PathfinderGoalOwnerHurtByTarget extends PathfinderGoalTarget {
    private final EntityCreature entity;
    private EntityLiving attacker;
    private int timestamp;

    public PathfinderGoalOwnerHurtByTarget(EntityCreature entity) {
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
            this.attacker = owner.getLastDamager();
            int i = owner.bT();
            return i != this.timestamp && this.a(this.attacker, false);
        }
    }

    @Override
    public void c() {
        this.entity.setGoalTarget(this.attacker, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
        EntityLiving owner = ((CelestialAlly) this.entity).getEntityOwner();
        if (owner != null) {
            this.timestamp = owner.bT();
        }
        super.c();
    }
}
