package net.pl3x.bukkit.ridabledolphins.listener;

import net.pl3x.bukkit.ridabledolphins.configuration.Lang;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spigotmc.event.entity.EntityDismountEvent;

public class DolphinListener implements Listener {
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
    }
}
