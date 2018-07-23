package net.pl3x.bukkit.ridabledolphins.entity;

import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.DamageSource;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityShulkerBullet;
import net.minecraft.server.v1_13_R1.IProjectile;
import net.minecraft.server.v1_13_R1.Material;
import net.minecraft.server.v1_13_R1.MathHelper;
import net.minecraft.server.v1_13_R1.MovingObjectPosition;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.Vec3D;
import net.minecraft.server.v1_13_R1.World;
import net.pl3x.bukkit.ridabledolphins.RidableDolphins;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EntityDolphinSpit extends EntityShulkerBullet implements IProjectile {
    private EntityLiving dolphin;
    private EntityHuman rider;
    private NBTTagCompound nbt;
    private int life;

    public EntityDolphinSpit(World world) {
        super(world);
        setSize(0.25F, 0.25F);
        setNoGravity(true);
    }

    public EntityDolphinSpit(World world, EntityRidableDolphin dolphin, EntityHuman rider) {
        this(world);
        this.dolphin = dolphin;
        this.rider = rider;
        setPosition(dolphin.locX - (double) (dolphin.width + 1.0F) * 0.5D * (double) MathHelper.sin(dolphin.aQ * 0.017453292F),
                dolphin.locY + (double) dolphin.getHeadHeight() - 0.10000000149011612D,
                dolphin.locZ + (double) (dolphin.width + 1.0F) * 0.5D * (double) MathHelper.cos(dolphin.aQ * 0.017453292F));
    }

    // tick
    public void tick() {
        super.tick();
        if (nbt != null) {
            restoreOwnerFromSave();
        }

        Vec3D minVec = new Vec3D(locX, locY, locZ);
        Vec3D maxVec = new Vec3D(locX + motX, locY + motY, locZ + motZ);
        MovingObjectPosition rayTraceResult = world.rayTrace(minVec, maxVec);
        minVec = new Vec3D(locX, locY, locZ);
        maxVec = new Vec3D(locX + motX, locY + motY, locZ + motZ);

        if (rayTraceResult != null) {
            maxVec = new Vec3D(rayTraceResult.pos.x, rayTraceResult.pos.y, rayTraceResult.pos.z);
        }

        Entity hitEntity = getHitEntity(minVec, maxVec);
        if (hitEntity != null) {
            rayTraceResult = new MovingObjectPosition(hitEntity);
        }

        if (rayTraceResult != null) {
            onHit(rayTraceResult);
        }

        locX += motX;
        locY += motY;
        locZ += motZ;
        float f = MathHelper.sqrt(motX * motX + motZ * motZ);

        yaw = (float) (MathHelper.c(motX, motZ) * 57.2957763671875D);

        for (pitch = (float) (MathHelper.c(motY, (double) f) * 57.2957763671875D); pitch - lastPitch < -180.0F; lastPitch -= 360.0F) {
            ;
        }

        while (pitch - lastPitch >= 180.0F)
            lastPitch += 360.0F;
        while (yaw - lastYaw < -180.0F)
            lastYaw -= 360.0F;
        while (yaw - lastYaw >= 180.0F)
            lastYaw += 360.0F;

        pitch = lastPitch + (pitch - lastPitch) * 0.2F;
        yaw = lastYaw + (yaw - lastYaw) * 0.2F;

        if (!world.a(getBoundingBox(), Material.AIR) && !aq()) {
            die(); // die if not in air or water
        } else {
            motX *= 0.9900000095367432D;
            motY *= 0.9900000095367432D;
            motZ *= 0.9900000095367432D;
            if (!isNoGravity()) {
                motY -= 0.05999999865889549D;
            }

            setPosition(locX, locY, locZ);
        }

        if (++life > 100) {
            die();
        }
    }

    // getHitEntity
    private Entity getHitEntity(Vec3D vec3d, Vec3D vec3d1) {
        Entity entity = null;
        List list = world.getEntities(this, getBoundingBox().b(motX, motY, motZ).g(1.0D));
        double d0 = 0.0D;
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Entity entity1 = (Entity) iterator.next();
            if (entity1 != dolphin && entity1 != rider) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox().g(0.30000001192092896D);
                MovingObjectPosition movingobjectposition = axisalignedbb.b(vec3d, vec3d1);
                if (movingobjectposition != null) {
                    double d1 = vec3d.distanceSquared(movingobjectposition.pos);
                    if (d1 < d0 || d0 == 0.0D) {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }
        return entity;
    }

    // shoot
    public void shoot(double d0, double d1, double d2, float f, float f1) {
        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        //d0 += random.nextGaussian() * 0.007499999832361937D * (double) f1;
        //d1 += random.nextGaussian() * 0.007499999832361937D * (double) f1;
        //d2 += random.nextGaussian() * 0.007499999832361937D * (double) f1;
        d0 *= (double) f;
        d1 *= (double) f;
        d2 *= (double) f;
        motX = d0;
        motY = d1;
        motZ = d2;
        float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        yaw = (float) (MathHelper.c(d0, d2) * 57.2957763671875D);
        pitch = (float) (MathHelper.c(d1, (double) f3) * 57.2957763671875D);
        lastYaw = yaw;
        lastPitch = pitch;
    }

    // onHit
    public void onHit(MovingObjectPosition pos) {
        if (pos.entity != null && dolphin != null) {
            pos.entity.damageEntity(DamageSource.a(this, dolphin).c(), RidableDolphins.shootingDamage);
        }
        die();
    }

    // entityInit
    protected void x_() {
    }

    // readEntityFromNBT
    protected void a(NBTTagCompound nbt) {
        if (nbt.hasKeyOfType("Owner", 10)) {
            this.nbt = nbt.getCompound("Owner");
        }

    }

    // writeEntityToNBT
    protected void b(NBTTagCompound nbttagcompound) {
        if (dolphin != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.a("OwnerUUID", dolphin.getUniqueID());
            nbttagcompound.set("Owner", nbt);
        }
    }

    // restoreOwnerFromSave
    private void restoreOwnerFromSave() {
        if (nbt != null && nbt.b("OwnerUUID")) {
            UUID uuid = nbt.a("OwnerUUID");
            List list = world.a(EntityRidableDolphin.class, getBoundingBox().g(15.0D));
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                EntityRidableDolphin dolphin = (EntityRidableDolphin) iterator.next();
                if (dolphin.getUniqueID().equals(uuid)) {
                    this.dolphin = dolphin;
                    break;
                }
            }
        }
        nbt = null;
    }

    // override ShulkerBullet's crap back to EntityLiving's crap

    public boolean isBurning() {
        return false;
    }

    public float az() {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(MathHelper.floor(locX), 0, MathHelper.floor(locZ));
        if (world.isLoaded(blockposition_mutableblockposition)) {
            blockposition_mutableblockposition.p(MathHelper.floor(locY + (double) getHeadHeight()));
            return world.A(blockposition_mutableblockposition);
        } else {
            return 0.0F;
        }
    }

    protected void a(MovingObjectPosition movingobjectposition) {

    }

    public boolean isInteractable() {
        return false;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (isInvulnerable(damagesource)) {
            return false;
        } else {
            aA();
            return false;
        }
    }
}
