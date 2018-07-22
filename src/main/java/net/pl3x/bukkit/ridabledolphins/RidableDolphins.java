package net.pl3x.bukkit.ridabledolphins;

import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Arrays;

public class RidableDolphins extends JavaPlugin implements Listener {
    @Override
    public void onLoad() {
        // DOES NOT WORK RIGHT! need to find the rest of the registration process
        //EntityTypes.a("dolphin", EntityTypes.a.a(EntityRidableDolphin.class, EntityRidableDolphin::new));
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onClickDolphin(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return; // definitely not a dolphin
        }
        LivingEntity dolphin = replaceDolphin((LivingEntity) event.getRightClicked());
        if (dolphin == null) {
            return; // not a dolphin
        }

        if (!dolphin.getPassengers().isEmpty()) {
            return; // dolphin already has rider
        }

        Player player = event.getPlayer();
        if (player.getVehicle() != null) {
            return; // player already riding something
        }

        // add player as rider
        dolphin.addPassenger(player);
    }

    @EventHandler
    public void onDolphinDismount(EntityDismountEvent event) {
        Entity dolphin = event.getDismounted();
        if (dolphin.getType() != EntityType.DOLPHIN) {
            return; // not a dolphin
        }

        if (event.getEntity().getType() != EntityType.PLAYER) {
            return; // not a player
        }

        if (((Player) event.getEntity()).isSneaking()) {
            return; // dismount from shift
        }

        // cancel dismount
        event.setCancelled(true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // replace all dolphins in chunk
        Arrays.stream(event.getChunk().getEntities())
                .filter(e -> e instanceof LivingEntity)
                .forEach(e -> replaceDolphin((LivingEntity) e));
    }

    @EventHandler
    public void onDolphinSpawn(CreatureSpawnEvent event) {
        // replace dolphin on spawn
        replaceDolphin(event.getEntity());
    }

    @EventHandler
    public void onDolphinDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.DOLPHIN) {
            return; // not a dolphin
        }

        // eject all dolphin's riders
        event.getEntity().getPassengers().forEach(Entity::eject);
    }

    public LivingEntity replaceDolphin(LivingEntity dolphin) {
        if (dolphin.getType() != EntityType.DOLPHIN) {
            return null; // not a dolphin
        }

        net.minecraft.server.v1_13_R1.Entity nmsDolphin = ((CraftEntity) dolphin).getHandle();
        if (nmsDolphin instanceof EntityRidableDolphin) {
            return dolphin; // dolphin is already ridable
        }

        // remove non-ridable dolphin
        dolphin.remove(); // paper bug not removing dead entities?
        nmsDolphin.die(); // has to be a bug... this didn't work either..
        nmsDolphin.world.removeEntity(nmsDolphin); // ok, this worked. *whew*

        // spawn ridable dolphin
        EntityRidableDolphin ridableDolphin = new EntityRidableDolphin(nmsDolphin.world);
        ridableDolphin.setPosition(nmsDolphin.locX, nmsDolphin.locY, nmsDolphin.locZ);
        nmsDolphin.world.addEntity(ridableDolphin);

        // ridable dolphin \o/
        return (LivingEntity) ridableDolphin.getBukkitEntity();
    }
}
