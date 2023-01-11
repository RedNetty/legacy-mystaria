package me.retrorealms.practiceserver.mechanics.dungeon.abyss;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.EliteDrops;
import me.retrorealms.practiceserver.mechanics.dungeon.reflect.Reflection;
import net.minecraft.server.v1_9_R2.EntityWither;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Set;

/**
 * Created by Giovanni on 20-5-2017.
 */
public class EntityDaemonLord extends EntityWither {


    public EntityDaemonLord(World world) {
        super(((CraftWorld) world).getHandle());

        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(150000);
        this.setHealth(150000);

        this.setCustomName(ChatColor.DARK_RED + "Infernal Daemon Deathlord of the Abyss");
        this.setCustomNameVisible(true);
    }

    public EntityDaemonLord clearAI() {
        ((Set) Reflection.getPrivateField("c", PathfinderGoalSelector.class, this.goalSelector)).clear();
        ((Set) Reflection.getPrivateField("b", PathfinderGoalSelector.class, this.goalSelector)).clear();

        ((Set) Reflection.getPrivateField("c", PathfinderGoalSelector.class, this.targetSelector)).clear();
        ((Set) Reflection.getPrivateField("b", PathfinderGoalSelector.class, this.targetSelector)).clear();

        return this;

    }

    public void spawn(Location location) {
        Wither wither = (Wither) this.getBukkitEntity();

        this.spawnIn(((CraftWorld) location.getWorld()).getHandle());
        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld) location.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);

        wither.getEquipment().setHelmet(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 5));
        wither.getEquipment().setChestplate(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 6));
        wither.getEquipment().setLeggings(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 7));
        wither.getEquipment().setBoots(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeon", 8));
        ItemStack itemStack = new ItemStack(EliteDrops.createCustomDungeonDrop("bossSkeletonDungeonD", 4));
        itemStack.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 1);

        wither.getEquipment().setItemInMainHand(itemStack);

        wither.getEquipment().setItemInOffHand(new ItemStack(Material.BEACON));

        wither.setMetadata("name", new FixedMetadataValue(PracticeServer.plugin, this.getCustomName()));
        wither.setMetadata("type", new FixedMetadataValue(PracticeServer.plugin, "bossSkeletonDungeonDAEMON"));

        wither.setCanPickupItems(false);
        wither.setRemoveWhenFarAway(false);

        wither.setMetadata("improvedDropRate", new FixedMetadataValue(PracticeServer.getInstance(), 35));
    }
}
