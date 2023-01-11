package me.retrorealms.practiceserver.mechanics.dungeon.skeleton;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.EliteDrops;
import me.retrorealms.practiceserver.mechanics.dungeon.reflect.Reflection;
import net.minecraft.server.v1_9_R2.EntitySkeleton;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Set;

/**
 * Created by Giovanni on 2-5-2017.
 */
public class SkeletonBoss extends EntitySkeleton {

    private SkeletonDungeon skeletonDungeon;

    SkeletonBoss(World world, SkeletonDungeon skeletonDungeon) {
        super(((CraftWorld) world).getHandle());

        this.skeletonDungeon = skeletonDungeon;

        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(80000);
        this.setHealth(80000);

        this.setCustomName(ChatColor.RED + "The Restless Skeleton Overlord");
        this.setCustomNameVisible(true);

    }

    SkeletonBoss invulnerable(boolean flag) {
        this.getBukkitEntity().setMetadata("damaging", new FixedMetadataValue(PracticeServer.getInstance(), flag));
        this.getBukkitEntity().setInvulnerable(true);

        return this;
    }

    SkeletonBoss clearAI() {
        ((Set) Reflection.getPrivateField("c", PathfinderGoalSelector.class, this.goalSelector)).clear();
        ((Set) Reflection.getPrivateField("b", PathfinderGoalSelector.class, this.goalSelector)).clear();

        ((Set) Reflection.getPrivateField("c", PathfinderGoalSelector.class, this.targetSelector)).clear();
        ((Set) Reflection.getPrivateField("b", PathfinderGoalSelector.class, this.targetSelector)).clear();

        return this;

    }

    void spawn(Location location) {
        Skeleton skeleton = (Skeleton) this.getBukkitEntity();
        skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);

        this.spawnIn(((CraftWorld) location.getWorld()).getHandle());
        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld) location.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);

        skeleton.getEquipment().setHelmet(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 5));
        skeleton.getEquipment().setChestplate(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 6));
        skeleton.getEquipment().setLeggings(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 7));
        skeleton.getEquipment().setBoots(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 8));
        ItemStack itemStack = new ItemStack(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 4));
        itemStack.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);

        skeleton.getEquipment().setItemInMainHand(itemStack);

        skeleton.getEquipment().setItemInOffHand(new ItemStack(Material.BEACON));

        skeleton.setMetadata("name", new FixedMetadataValue(PracticeServer.plugin, this.getCustomName()));
        skeleton.setMetadata("type", new FixedMetadataValue(PracticeServer.plugin, "bossSkeletonDungeon"));

        skeleton.setCanPickupItems(false);
        skeleton.setRemoveWhenFarAway(false);
    }
}
