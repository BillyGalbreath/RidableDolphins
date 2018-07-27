package net.pl3x.bukkit.ridabledolphins.listener;

import net.pl3x.bukkit.ridabledolphins.configuration.Lang;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.lang.reflect.Field;

public class DolphinListener implements Listener {
    private Field ax;

    public DolphinListener() {
        try {
            ax = net.minecraft.server.v1_13_R1.Entity.class.getDeclaredField("ax");
            ax.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onClickDolphin(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // dont fire twice
        }

        Entity dolphin = event.getRightClicked();
        if (dolphin.getType() != EntityType.DOLPHIN) {
            return; // not a dolphin
        }

        if (!dolphin.getPassengers().isEmpty()) {
            return; // dolphin already has rider
        }

        Player player = event.getPlayer();
        if (player.getVehicle() != null) {
            return; // player already riding something
        }

        if (!player.hasPermission("allow.dolphin.ride")) {
            Lang.send(player, Lang.RIDE_NO_PERMISSION);
            return;
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

        if (dolphin.isDead()) {
            return; // dolphin died
        }

        if (event.getEntity().getType() != EntityType.PLAYER) {
            return; // not a player
        }

        Player player = (Player) event.getEntity();
        if (player.isSneaking()) {
            return; // dismount from shift
        }

        if (player.isDead()) {
            return; // player died
        }

        // cancel dismount
        event.setCancelled(true);
        setVehicleBack(player, dolphin);
    }

    // Lets fix what md_5 wont. Can be removed in a future version when fixed
    // https://hub.spigotmc.org/jira/browse/SPIGOT-1588
    // https://hub.spigotmc.org/jira/browse/SPIGOT-2466
    // https://hub.spigotmc.org/jira/browse/SPIGOT-4113
    // https://hub.spigotmc.org/jira/browse/SPIGOT-4163
    private void setVehicleBack(Entity rider, Entity vehicle) {
        if (ax != null) {
            try {
                ax.set(((CraftEntity) rider).getHandle(), ((CraftEntity) vehicle).getHandle());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
