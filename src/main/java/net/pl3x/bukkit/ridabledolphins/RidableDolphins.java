package net.pl3x.bukkit.ridabledolphins;

import net.minecraft.server.v1_13_R1.EntityTypes;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

public class RidableDolphins extends JavaPlugin implements Listener {
    @Override
    public void onLoad() {
        EntityTypes.a("dolphin", EntityTypes.a.a(EntityRidableDolphin.class, EntityRidableDolphin::new));
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onClickDolphin(PlayerInteractAtEntityEvent event) {
        Entity dolphin = event.getRightClicked();
        if (!(dolphin instanceof Dolphin)) {
            return;
        }
        if (!dolphin.getPassengers().isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getVehicle() == null) {
            dolphin.addPassenger(player);
        }
    }

    @EventHandler
    public void onDolphinDismount(EntityDismountEvent event) {
        Entity dolphin = event.getDismounted();
        if (!(dolphin instanceof Dolphin)) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        if (!player.isSneaking()) {
            dolphin.addPassenger(player);
        }
    }

    @EventHandler
    public void onDolphinSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Dolphin) {
            System.out.println("[CreatureSpawnEvent] Dolphin spawned!");
        }
    }

    @EventHandler
    public void onDolphinDeath(EntityDeathEvent event) {
        LivingEntity dolphin = event.getEntity();
        if (dolphin instanceof Dolphin) {
            dolphin.getPassengers().forEach(Entity::eject);
        }
    }
}
