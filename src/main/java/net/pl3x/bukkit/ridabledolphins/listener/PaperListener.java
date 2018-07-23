package net.pl3x.bukkit.ridabledolphins.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

public class PaperListener implements Listener {
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
}
