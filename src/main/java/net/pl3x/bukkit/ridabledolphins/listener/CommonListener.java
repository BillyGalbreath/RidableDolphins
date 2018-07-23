package net.pl3x.bukkit.ridabledolphins.listener;

import net.pl3x.bukkit.ridabledolphins.RidableDolphins;
import org.bukkit.ChatColor;
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
import org.bukkit.inventory.EquipmentSlot;

import java.util.Arrays;

public class CommonListener implements Listener {
    private RidableDolphins plugin;

    public CommonListener(RidableDolphins plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClickDolphin(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // dont fire twice
        }

        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return; // definitely not a dolphin
        }
        LivingEntity dolphin = plugin.replaceDolphin((LivingEntity) event.getRightClicked());
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

        if (!player.hasPermission("allow.ride.dolphin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission for that");
            return;
        }

        // add player as rider
        dolphin.addPassenger(player);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // replace all dolphins in chunk
        Arrays.stream(event.getChunk().getEntities())
                .filter(e -> e instanceof LivingEntity)
                .forEach(e -> plugin.replaceDolphin((LivingEntity) e));
    }

    @EventHandler
    public void onDolphinSpawn(CreatureSpawnEvent event) {
        // replace dolphin on spawn
        plugin.replaceDolphin(event.getEntity());
    }

    @EventHandler
    public void onDolphinDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.DOLPHIN) {
            return; // not a dolphin
        }

        // eject all dolphin's riders
        event.getEntity().getPassengers().forEach(Entity::eject);
    }
}
