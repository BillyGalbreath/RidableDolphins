package net.pl3x.bukkit.ridabledolphins.listener;

import net.pl3x.bukkit.ridabledolphins.configuration.Lang;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spigotmc.event.entity.EntityDismountEvent;

public class DolphinListener implements Listener {
    private final ItemStack dolphin_bucket = new ItemStack(Material.COD_BUCKET, 1);

    public DolphinListener() {
        ItemMeta meta = dolphin_bucket.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Lang.BUCKET_NAME));
        meta.setLore(Lang.BUCKET_LORE);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        dolphin_bucket.setItemMeta(meta);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCollectDolphin(PlayerInteractAtEntityEvent event) {
        Entity dolphin = event.getRightClicked();
        if (dolphin.getType() != EntityType.DOLPHIN) {
            return; // not a dolphin
        }

        if (!dolphin.getPassengers().isEmpty()) {
            return; // dolphin has a rider
        }

        Player player = event.getPlayer();
        ItemStack bucket = player.getInventory().getItem(event.getHand());
        if (bucket.getType() != Material.WATER_BUCKET) {
            return; // not a water bucket
        }

        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle.getUniqueId().equals(dolphin.getUniqueId())) {
            return; // player is riding this dolphin
        }

        if (!player.hasPermission("allow.dolphin.collect")) {
            Lang.send(player, Lang.COLLECT_NO_PERMISSION);
            return;
        }

        player.getInventory().setItem(event.getHand(), dolphin_bucket.clone());
        dolphin.remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlaceDolphin(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.COD_BUCKET) {
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot hand = EquipmentSlot.HAND;
        ItemStack bucket = player.getInventory().getItemInMainHand();
        if (!bucket.isSimilar(dolphin_bucket)) {
            hand = EquipmentSlot.OFF_HAND;
            bucket = player.getInventory().getItemInOffHand();
            if (!bucket.isSimilar(dolphin_bucket)) {
                return;
            }
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            bucket.setAmount(Math.max(0, bucket.getAmount() - 1));
            player.getInventory().setItem(hand, bucket);
        }

        player.getWorld().spawnEntity(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation().add(0.5, 0.5, 0.5), EntityType.DOLPHIN);
        event.setCancelled(true); // do not spawn a cod!
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
