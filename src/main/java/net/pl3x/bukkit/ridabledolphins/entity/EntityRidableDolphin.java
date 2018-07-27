package net.pl3x.bukkit.ridabledolphins.entity;

import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityDolphin;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumMoveType;
import net.minecraft.server.v1_13_R1.ParticleType;
import net.minecraft.server.v1_13_R1.Particles;
import net.minecraft.server.v1_13_R1.SoundEffect;
import net.minecraft.server.v1_13_R1.SoundEffects;
import net.minecraft.server.v1_13_R1.World;
import net.minecraft.server.v1_13_R1.WorldServer;
import net.pl3x.bukkit.ridabledolphins.configuration.Config;
import net.pl3x.bukkit.ridabledolphins.configuration.Lang;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class EntityRidableDolphin extends EntityDolphin {
    private static Field jumping;

    private int bounceCounter = 0;
    private boolean bounceUp = false;

    private int spacebarCooldown = 0;

    private boolean dashing = false;
    private int dashCounter = 0;

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

        if (spacebarCooldown > 0) {
            spacebarCooldown--;
        }

        EntityPlayer rider = getRider();
        if (rider != null && getAirTicks() > 150) {
            setYawPitch(lastYaw = yaw = rider.yaw, pitch = (rider.pitch * 0.5F));
            aQ = yaw; // renderYawOffset
            aS = aQ; // rotationYawHead

            if (jumping != null) {
                try {
                    if (jumping.getBoolean(rider) && spacebarCooldown == 0) {
                        if (Config.SPACEBAR_MODE != null) {
                            if (Config.SPACEBAR_MODE.equalsIgnoreCase("shoot")) {
                                shoot(rider);
                            } else if (Config.SPACEBAR_MODE.equalsIgnoreCase("dash")) {
                                spacebarCooldown = Config.DASHING_COOLDOWN;
                                if (!dashing && rider.getBukkitEntity().hasPermission("allow.dolphin.dash")) {
                                    dashing = true;
                                    dashCounter = 0;
                                    playSound(SoundEffects.ENTITY_DOLPHIN_JUMP);
                                }
                            }
                        }
                    }
                } catch (IllegalAccessException ignore) {
                }
            }

            // only move when in water
            if (isInWater()) {
                float forward = rider.bj; // forward motion
                float vertical = -(rider.pitch / 90); // vertical motion
                float strafe = rider.bh; // sideways motion

                if (dashing) {
                    if (++dashCounter > Config.DASHING_DURATION) {
                        dashCounter = 0;
                        dashing = false;
                    }
                    forward = 1F;
                }

                if (forward < 0.0F) {
                    // slow down reverse motion
                    forward *= 0.25F;
                    vertical = -vertical * 0.1F;
                    strafe *= 0.25F;
                } else if (forward == 0F) {
                    // prevent vertical movement without forward movement
                    vertical = 0F;
                }

                // bubbles! \o/
                if (Config.BUBBLES) {
                    double velocity = motX * motX + motY * motY + motZ * motZ;
                    if (velocity > 0.2 || velocity < -0.2) {
                        int i = (int) (velocity * 5);
                        for (int j = 0; j < i; j++) {
                            spawnParticle(Particles.e);
                        }
                    }
                }

                // move it
                a(strafe, vertical * Config.SPEED_VERTICAL, forward, cJ() * Config.SPEED_FORWARD * (dashing ? Config.DASHING_BOOST : 1));
                move(EnumMoveType.PLAYER, this.motX * Config.SPEED_HORIZONTAL, motY, motZ * Config.SPEED_HORIZONTAL);

                // friction
                motY *= 0.8999999761581421D;
                motX *= 0.8999999761581421D;
                motZ *= 0.8999999761581421D;

                // bounce
                motY -= Config.BOUNCE && forward == 0 ? bounceUp ? 0.01D : 0.00D : 0.005D;
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
        spacebarCooldown = Config.SHOOTING_COOLDOWN;

        if (rider == null) {
            return;
        }

        CraftPlayer player = rider.getBukkitEntity();
        if (!player.hasPermission("allow.dolphin.shoot")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return;
        }

        EntityDolphinSpit spit = new EntityDolphinSpit(world, this, rider);

        Location loc = rider.getBukkitEntity().getEyeLocation();
        loc.setPitch(loc.getPitch() - 10);
        Vector target = loc.getDirection().normalize().multiply(10).add(loc.toVector());

        spit.shoot(target.getX() - locX, target.getY() - locY, target.getZ() - locZ, Config.SHOOTING_SPEED, 5.0F);

        playSound(SoundEffects.ENTITY_DOLPHIN_ATTACK);
        world.addEntity(spit);
    }

    private void spawnParticle(ParticleType particle) {
        ((WorldServer) world).sendParticles(null, particle,
                lastX + random.nextFloat() / 2 - 0.25F,
                lastY + random.nextFloat() / 2 - 0.25F,
                lastZ + random.nextFloat() / 2 - 0.25F,
                1, 0, 0, 0, 0);
    }

    private void playSound(SoundEffect sound) {
        a(sound, 1.0F, 1.0F);
    }
}
