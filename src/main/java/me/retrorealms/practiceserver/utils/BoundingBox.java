package me.retrorealms.practiceserver.utils;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BoundingBox {
    public Vector min, max;

    public BoundingBox(Block block) {
        try {
            BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
            AxisAlignedBB box = world.getType(pos).d(world, pos);
            this.min = new Vector(pos.getX() + box.a, pos.getY() + box.b, pos.getZ() + box.c);
            this.max = new Vector(pos.getX() + box.d, pos.getY() + box.e, pos.getZ() + box.f);
        }catch (Exception e){}
    }

    public BoundingBox(Entity entity) {
        try {
            AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox();
            this.min = new Vector(box.a, box.b, box.c);
            this.max = new Vector(box.d, box.e, box.f);
        }catch (Exception e) {

        }
    }
}
