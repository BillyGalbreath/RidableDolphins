package net.pl3x.bukkit.ridabledolphins;

import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityDolphin;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumMoveType;
import net.minecraft.server.v1_13_R1.World;

public class EntityRidableDolphin extends EntityDolphin {
    private int bounceCounter = 0;
    private boolean bounceUp = false;

    public EntityRidableDolphin(World world) {
        super(world);

        System.out.println("New Ridable Dolphin Spawned");

        this.persistent = true;
    }

    protected boolean isTypeNotPersistent() {
        return false; // we want persistence
    }

    @Override
    public void a(float f, float f1, float f2) {
        if (++bounceCounter > 10) {
            bounceCounter = 0;
            bounceUp = !bounceUp;
        }

        EntityPlayer rider = getRider();
        if (rider != null) {
            setYawPitch(lastYaw = yaw = rider.yaw, pitch = (rider.pitch * 0.5F));
            aQ = yaw; // renderYawOffset
            aS = aQ; // rotationYawHead

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
                this.a(0, vertical * 2, forward, this.cJ() * 5);
                this.move(EnumMoveType.PLAYER, this.motX / 2, this.motY, this.motZ / 2);
                this.motY *= 0.8999999761581421D;
                this.motX *= 0.8999999761581421D;
                this.motZ *= 0.8999999761581421D;
                motY -= forward == 0 ? bounceUp ? 0.01D : 0.00D : 0.005D;
                return;
            }
        }
        super.a(f, f1, f2);
    }

    public EntityPlayer getRider() {
        if (passengers != null && !passengers.isEmpty()) {
            Entity entity = passengers.get(0);
            if (entity instanceof EntityPlayer) {
                return (EntityPlayer) entity;
            }
        }
        return null;
    }
}
