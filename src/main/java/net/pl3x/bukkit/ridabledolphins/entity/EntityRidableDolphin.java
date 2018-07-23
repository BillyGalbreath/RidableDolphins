package net.pl3x.bukkit.ridabledolphins.entity;

import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityDolphin;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumMoveType;
import net.minecraft.server.v1_13_R1.MathHelper;
import net.minecraft.server.v1_13_R1.SoundEffects;
import net.minecraft.server.v1_13_R1.World;
import net.pl3x.bukkit.ridabledolphins.RidableDolphins;
import net.pl3x.bukkit.ridabledolphins.util.Vector3D;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class EntityRidableDolphin extends EntityDolphin {
    private static Field jumping;
    private static Field bG;

    private int bounceCounter = 0;
    private boolean bounceUp = false;

    private int shootCounter = 0;

    private Set<Material> transparent = new HashSet<>();

    public EntityRidableDolphin(World world) {
        super(world);
        this.persistent = true; // we want persistence

        if (jumping == null) {
            try {
                jumping = EntityLiving.class.getDeclaredField("bg");
                bG = EntityLiving.class.getDeclaredField("bG");
                jumping.setAccessible(true);
                bG.setAccessible(true);
            } catch (NoSuchFieldException ignore) {
            }
        }

        transparent.add(Material.AIR);
        transparent.add(Material.WATER);
    }

    protected boolean isTypeNotPersistent() {
        return false; // we definitely want persistence
    }

    @Override
    public void a(float f, float f1, float f2) {
        if (++bounceCounter > 10) {
            bounceCounter = 0;
            bounceUp = !bounceUp; // bounce dat ass!
        }

        if (shootCounter > 0) {
            shootCounter--;
        }

        EntityPlayer rider = getRider();
        if (rider != null) {
            setYawPitch(lastYaw = yaw = rider.yaw, pitch = (rider.pitch * 0.5F));
            aQ = yaw; // renderYawOffset
            aS = aQ; // rotationYawHead

            boolean isJumping = false;
            if (jumping != null) {
                try {
                    isJumping = jumping.getBoolean(rider);
                } catch (IllegalAccessException ignore) {
                }
            }

            if (isJumping && shootCounter == 0) {
                shoot(rider);
            }

            if (isInWater()) {
                float forward = rider.bj; // forward motion
                float vertical = f1; // vertical motion
                if (forward <= 0.0F) {
                    forward *= 0.25F; // slow down reverse motion
                    vertical = -vertical;
                }
                if (forward == 0F) {
                    vertical = 0F;
                }

                a(0, vertical * RidableDolphins.verticalSpeedModifier, forward, cJ() * RidableDolphins.forwardSpeedModifier);
                move(EnumMoveType.PLAYER, this.motX * RidableDolphins.horizontalSpeedModifier, motY, motZ * RidableDolphins.horizontalSpeedModifier);
                motY *= 0.8999999761581421D;
                motX *= 0.8999999761581421D;
                motZ *= 0.8999999761581421D;
                motY -= forward == 0 ? bounceUp ? 0.01D : 0.00D : 0.005D;
                return;
            }
        }
        super.a(f, f1, f2);
    }

    public EntityPlayer getRider() {
        if (passengers != null && !passengers.isEmpty()) {
            Entity entity = passengers.get(0); // only care about first rider
            if (entity instanceof EntityPlayer) {
                return (EntityPlayer) entity;
            }
        }
        return null; // aww, lonely dolphin is lonely
    }

    public void shoot(EntityPlayer rider) {
        shootCounter = RidableDolphins.shootingCooldown;

        if (rider == null) {
            return;
        }

        CraftPlayer player = rider.getBukkitEntity();
        if (!player.hasPermission("allow.dolphin.shoot")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to shoot");
            return;
        }

        EntityDolphinSpit spit = new EntityDolphinSpit(world, this, rider);

        double d0, d1, d2;
        Block targetBlock = rider.getBukkitEntity().getTargetBlock(transparent, 120);
        EntityLiving targetEntity = getTargetEntity(rider);
        if (targetEntity != null) {
            d0 = targetEntity.locX - locX;
            d1 = targetEntity.locY - locY;
            d2 = targetEntity.locZ - locZ;
        } else if (targetBlock != null) {
            Location loc = targetBlock.getLocation();
            d0 = (loc.getX() + 0.5F) - locX;
            d1 = (loc.getY() + 0.5F) - locY;
            d2 = (loc.getZ() + 0.5F) - locZ;
        } else {
            float p = rider.pitch;
            p += 10F;
            d0 = -MathHelper.sin(rider.yaw * 0.017453292F) * MathHelper.cos(p * 0.017453292F);
            d1 = -MathHelper.sin(p * 0.017453292F);
            d2 = MathHelper.cos(rider.yaw * 0.017453292F) * MathHelper.cos(p * 0.017453292F);
        }

        spit.shoot(d0, d1, d2, RidableDolphins.shootingSpeed, 5.0F);
        world.a((EntityHuman) null, locX, locY, locZ, SoundEffects.ENTITY_DOLPHIN_ATTACK, bV(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        world.addEntity(spit);
    }

    private EntityLiving getTargetEntity(EntityHuman rider) {
        EntityLiving target = null;
        Vector3D targetPos = null;
        Location playerLoc = rider.getBukkitEntity().getEyeLocation();
        Vector3D playerDir = new Vector3D(playerLoc.getDirection());
        Vector3D playerStart = new Vector3D(playerLoc);
        Vector3D playerEnd = playerStart.add(playerDir.multiply(100));
        for (Entity entity : world.getEntities(this, new AxisAlignedBB(locX - 120, locY - 120, locZ - 120, locX + 120, locY + 120, locZ + 120))) {
            Vector3D entityPos = new Vector3D(entity.locX, entity.locY, entity.locZ);
            Vector3D minimum = entityPos.add(-0.5, 0, -0.5);
            Vector3D maximum = entityPos.add(0.5, 1.67, 0.5);
            if (entity instanceof EntityLiving && entity != this && entity != rider && hasIntersection(playerStart, playerEnd, minimum, maximum)) {
                if (target == null || targetPos.distanceSquared(playerStart) > entityPos.distanceSquared(playerStart)) {
                    target = (EntityLiving) entity;
                    targetPos = new Vector3D(target.locX, target.locY, target.locZ);
                }
            }
        }

        return target;
    }

    private boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        final double epsilon = 0.0001f;
        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();
        if (Math.abs(c.x) > e.x + ad.x) return false;
        if (Math.abs(c.y) > e.y + ad.y) return false;
        if (Math.abs(c.z) > e.z + ad.z) return false;
        if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon) return false;
        if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon) return false;
        if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon) return false;
        return true;
    }
}
