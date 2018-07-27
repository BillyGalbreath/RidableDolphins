package net.pl3x.bukkit.ridabledolphins;

import net.pl3x.bukkit.ridabledolphins.command.CmdRidableDolphins;
import net.pl3x.bukkit.ridabledolphins.configuration.Config;
import net.pl3x.bukkit.ridabledolphins.configuration.Lang;
import net.pl3x.bukkit.ridabledolphins.entity.EntityRidableDolphin;
import net.pl3x.bukkit.ridabledolphins.listener.DolphinListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

public class RidableDolphins extends JavaPlugin implements Listener {
    @Override
    public void onLoad() {
        // DOES NOT WORK RIGHT! need to find the rest of the registration process
        //EntityTypes.a("dolphin", EntityTypes.a.a(EntityRidableDolphin.class, EntityRidableDolphin::new));
    }

    @Override
    public void onEnable() {
        Config.reload();
        Lang.reload();

        try {
            // test for 1.13+ by looking for the Dolphin interface
            Class.forName("org.bukkit.entity.Dolphin");
            // test for Spigot/Paper server listener API
            Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
        } catch (ClassNotFoundException e) {
            ConsoleCommandSender console = getServer().getConsoleSender();
            console.sendMessage(ChatColor.RED + "This server is unsupported!");
            console.sendMessage(ChatColor.RED + "Please use Spigot or Paper version 1.13 or higher!");
            console.sendMessage(ChatColor.RED + "Plugin is now disabling itself!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // check for cancellable EntityDismountEvent
        if (!(new EntityDismountEvent(null, null) instanceof Cancellable)) {
            ConsoleCommandSender console = getServer().getConsoleSender();
            console.sendMessage(ChatColor.RED + "This version of Spigot is too old!");
            console.sendMessage(ChatColor.RED + "Please re-run BuildTools for an updated copy!");
            console.sendMessage(ChatColor.RED + "Plugin is now disabling itself!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // listeners \o/
        getServer().getPluginManager().registerEvents(new DolphinListener(this), this);

        // commands \o/ idky i'm so excited
        getCommand("ridabledolphins").setExecutor(new CmdRidableDolphins(this));

        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("server_version", () -> {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                return "Paper";
            } catch (Exception ignore) {
            }
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                return "Spigot";
            } catch (Exception ignore2) {
            }
            return "CraftBukkit";
        }));
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
        ridableDolphin.setPositionRotation(nmsDolphin.locX, nmsDolphin.locY, nmsDolphin.locZ,
                dolphin.getLocation().getYaw(), dolphin.getLocation().getPitch());
        nmsDolphin.world.addEntity(ridableDolphin);

        // ridable dolphin \o/
        return (LivingEntity) ridableDolphin.getBukkitEntity();
    }
}
