package me.retrorealms.practiceserver.mechanics.pets.base;

import org.bukkit.entity.EntityType;

/**
 * Created by Khalid on 8/8/2017.
 */
public enum PetType {

    RABBIT(EntityType.RABBIT),
    CHICKEN(EntityType.CHICKEN),
    SILVERFISH(EntityType.SILVERFISH),
    CAVE_SPIDER(EntityType.CAVE_SPIDER),
    OCELOT(EntityType.OCELOT),
    PIG_ZOMBIE(EntityType.PIG_ZOMBIE),
    WOLF(EntityType.WOLF),
    MAGMA_CUBE(EntityType.MAGMA_CUBE),
    ZOMBIE(EntityType.ZOMBIE),
    RSHEEP(EntityType.SHEEP),
    ENDERMITE(EntityType.ENDERMITE),
    SNOWMAN(EntityType.SNOWMAN),
    ENDERMAN(EntityType.ENDERMAN),
    BSHEEP(EntityType.SHEEP),
    GUARDIAN(EntityType.GUARDIAN),;

    private EntityType type;

    PetType(EntityType type) {
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }
}
