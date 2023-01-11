package me.retrorealms.practiceserver.mechanics.pets.base;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

/**
 * Created by Subby on 8/8/2017.
 */
public class Pet {

    private String owner;
    private Entity entity;

    public Pet(String owner, EntityType type, Player player, boolean baby) {
        this.owner = owner;
        this.entity = player.getWorld().spawnEntity(player.getLocation(), type);
        this.entity.setMetadata("pet", new FixedMetadataValue(PracticeServer.getInstance(), "s"));
        ((CraftEntity)entity).getHandle().setInvulnerable(true);
        this.entity.setCustomName(Bukkit.getPlayer(UUID.fromString(owner)).getName() + "'s Pet");
        this.entity.setCustomNameVisible(true);
        if (baby) {
            if (type == EntityType.PIG_ZOMBIE) {
                ((PigZombie) entity).setBaby(true);
            }
            if (type == EntityType.ZOMBIE) {
                ((Zombie) entity).setBaby(true);
            }
            if (type == EntityType.SHEEP) {
                ((Sheep) entity).setBaby();
            }
        } else {
            if (type == EntityType.SHEEP) {
                if (entity == null)
                if (API.getRainbowSheepTask() == null)
                API.getRainbowSheepTask().register(entity);
            }
        }
        PracticeServer.getManagerHandler().getPetManager().registerPet(this);
    }

    public void walk(Location location) {
        ((CraftCreature) entity).getHandle().getNavigation().a(location.getX(), location.getY(), location.getZ(), 1.8);
    }

    public String getOwner() {
        return owner;
    }

    public Entity getEntity() {
        return entity;
    }
}
