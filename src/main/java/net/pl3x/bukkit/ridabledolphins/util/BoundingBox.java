package net.pl3x.bukkit.ridabledolphins.util;

import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.util.Vector;

public class BoundingBox {
    private final Vector min;
    private final Vector max;

    public BoundingBox(Block block) {
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        AxisAlignedBB box = world.getType(pos).g(world, pos).a();
        min = new Vector(pos.getX() + box.a, pos.getY() + box.b, pos.getZ() + box.c);
        max = new Vector(pos.getX() + box.d, pos.getY() + box.e, pos.getZ() + box.f);
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }
}
