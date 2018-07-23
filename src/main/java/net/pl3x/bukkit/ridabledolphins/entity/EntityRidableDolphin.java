package net.pl3x.bukkit.ridabledolphins.entity;

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
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;

import java.lang.reflect.Field;

public class EntityRidableDolphin extends EntityDolphin {
    private static Field jumping;

    private int bounceCounter = 0;
    private boolean bounceUp = false;

    private int shootCounter = 0;

    public EntityRidableDolphin(World world) {
        super(world);
        this.persistent = true; // we want persistence

        if (jumping == null) {
            try {
                jumping = EntityLiving.class.getDeclaredField("bg");
                jumping.setAccessible(true);
            } catch (NoSuchFieldException ignore) {
            }
        }
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

        float p = rider.pitch;
        p += 8F;
        double d0 = -MathHelper.sin(rider.yaw * 0.017453292F) * MathHelper.cos(p * 0.017453292F);
        double d1 = -MathHelper.sin(p * 0.017453292F);
        double d2 = MathHelper.cos(rider.yaw * 0.017453292F) * MathHelper.cos(p * 0.017453292F);


        float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;

        spit.shoot(d0, d1 + (double) f * 2, d2, RidableDolphins.shootingSpeed, 5.0F);
        world.a((EntityHuman) null, locX, locY, locZ, SoundEffects.ENTITY_DOLPHIN_ATTACK, bV(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        world.addEntity(spit);
    }
}
