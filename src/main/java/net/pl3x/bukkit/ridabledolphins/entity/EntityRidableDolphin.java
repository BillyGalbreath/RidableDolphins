package net.pl3x.bukkit.ridabledolphins.entity;

import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityDolphin;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumMoveType;
import net.minecraft.server.v1_13_R1.MathHelper;
import net.minecraft.server.v1_13_R1.ParticleType;
import net.minecraft.server.v1_13_R1.Particles;
import net.minecraft.server.v1_13_R1.SoundEffects;
import net.minecraft.server.v1_13_R1.World;
import net.minecraft.server.v1_13_R1.WorldServer;
import net.pl3x.bukkit.ridabledolphins.configuration.Config;
import net.pl3x.bukkit.ridabledolphins.configuration.Lang;
import net.pl3x.bukkit.ridabledolphins.util.BoundingBox;
import net.pl3x.bukkit.ridabledolphins.util.RayTrace;
import net.pl3x.bukkit.ridabledolphins.util.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EntityRidableDolphin extends EntityDolphin {
    private static Field jumping;

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
                jumping.setAccessible(true);
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
        if (rider != null && getAirTicks() > 150) {
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
                float vertical = -(rider.pitch / 90); // vertical motion
                float strafe = rider.bh; // sideways motion
                if (forward <= 0.0F) {
                    forward *= 0.25F; // slow down reverse motion
                    vertical = -vertical * 0.1F;
                    strafe *= 0.25F;
                }
                if (forward == 0F) {
                    vertical = 0F;
                }

                a(strafe, vertical * Config.SPEED_VERTICAL, forward, cJ() * Config.SPEED_FORWARD);
                move(EnumMoveType.PLAYER, this.motX * Config.SPEED_HORIZONTAL, motY, motZ * Config.SPEED_HORIZONTAL);

                double velocity = motX * motX + motY * motY + motZ * motZ;
                if (velocity > 0.2 || velocity < -0.2) {
                    int i = (int) (velocity * 5);
                    for (int j = 0; j < i; j++) {
                        spawnParticle(Particles.e);
                    }
                }

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
        shootCounter = Config.SHOOTING_COOLDOWN;

        if (rider == null) {
            return;
        }

        CraftPlayer player = rider.getBukkitEntity();
        if (!player.hasPermission("allow.dolphin.shoot")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return;
        }

        EntityDolphinSpit spit = new EntityDolphinSpit(world, this, rider);

        double d0, d1, d2;
        // TODO: FIX THIS
        // https://www.spigotmc.org/threads/1-13-ridable-dolphins-spigot-paper.330108/page-2#post-3086806
        //Vector targetBlock = getTargetLocation(rider);
        Vector targetBlock = null;
        EntityLiving targetEntity = getTargetEntity(rider);
        if (targetEntity != null) {
            d0 = targetEntity.locX - locX;
            d1 = (targetEntity.locY + (targetEntity.getBoundingBox().a()) / 2) - locY;
            d2 = targetEntity.locZ - locZ;
        } else if (targetBlock != null) {
            d0 = (targetBlock.getX()) - locX;
            d1 = (targetBlock.getY() - 0.5) - locY;
            d2 = (targetBlock.getZ()) - locZ;
        } else {
            Block block = rider.getBukkitEntity().getTargetBlock(transparent, 120);
            if (block != null) {
                Location l = block.getLocation();
                d0 = (l.getX() + 0.5) - locX;
                d1 = (l.getY() + 0.5) - locY;
                d2 = (l.getZ() + 0.5) - locZ;
            } else {
                // this shit is all fucked up. only use as last resort
                float p = rider.pitch;
                p += 10F;
                d0 = -MathHelper.sin(rider.yaw * 0.017453292F) * MathHelper.cos(p * 0.017453292F);
                d1 = -MathHelper.sin(p * 0.017453292F);
                d2 = MathHelper.cos(rider.yaw * 0.017453292F) * MathHelper.cos(p * 0.017453292F);
            }
        }

        spit.shoot(d0, d1, d2, Config.SHOOTING_SPEED, 5.0F);
        a(SoundEffects.ENTITY_DOLPHIN_ATTACK, 1.0F, 1.0F);
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
            if (entity instanceof EntityLiving && entity != this && entity != rider && hasIntersection(playerStart, playerEnd, minimum, maximum) && hasLineOfSight(entity)) {
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

    private void spawnParticle(ParticleType particle) {
        ((WorldServer) world).sendParticles(null, particle,
                locX + random.nextFloat() * 2F - 1F,
                locY + random.nextFloat() * 2F - 1F,
                locZ + random.nextFloat() * 2F - 1F,
                1, 0, 0, 0, 0);
    }

    private Vector getTargetLocation(EntityHuman rider) {
        RayTrace rayTrace = new RayTrace(rider.getBukkitEntity().getEyeLocation());
        ArrayList<Vector> positions = rayTrace.traverse(120, 0.1);

        org.bukkit.World w = rider.getBukkitEntity().getWorld();
        for (Vector pos : positions) {
            Block block = pos.toBlockVector().toLocation(w).getBlock();
            if (block.getType() == Material.AIR || block.getType() == Material.WATER) {
                continue; // ignore air and water blocks
            }
            BoundingBox box = new BoundingBox(block);
            for (Vector position : positions) {
                if (rayTrace.intersects(position, box.getMin(), box.getMax())) {
                    return pos;
                }
            }
        }

        return null;
    }
}
