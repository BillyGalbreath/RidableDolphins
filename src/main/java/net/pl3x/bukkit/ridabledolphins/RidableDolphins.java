package net.pl3x.bukkit.ridabledolphins;

import net.pl3x.bukkit.ridabledolphins.entity.EntityRidableDolphin;
import net.pl3x.bukkit.ridabledolphins.listener.CommonListener;
import net.pl3x.bukkit.ridabledolphins.listener.PaperListener;
import net.pl3x.bukkit.ridabledolphins.listener.SpigotListener;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class RidableDolphins extends JavaPlugin implements Listener {
    @Override
    public void onLoad() {
        // DOES NOT WORK RIGHT! need to find the rest of the registration process
        //EntityTypes.a("dolphin", EntityTypes.a.a(EntityRidableDolphin.class, EntityRidableDolphin::new));
    }

    @Override
    public void onEnable() {
        ConsoleCommandSender console = getServer().getConsoleSender();

        try {
            // test for 1.13+ by looking for the Dolphin interface
            Class.forName("org.bukkit.entity.Dolphin");
        } catch (ClassNotFoundException e) {
            console.sendMessage(ChatColor.RED + "This server is unsupported!");
            console.sendMessage(ChatColor.RED + "Please use Spigot or Paper version 1.13 or higher!");
            console.sendMessage(ChatColor.RED + "Plugin is now disabling itself!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            // test for Paper server for better listener API
            Class.forName("com.destroystokyo.paper.PaperConfig");
            getServer().getPluginManager().registerEvents(new PaperListener(), this);
        } catch (ClassNotFoundException e) {
            try {
                // test for Spigot server for minimum listener API
                Class.forName("org.spigotmc.SpigotConfig");
                getServer().getPluginManager().registerEvents(new SpigotListener(), this);
            } catch (ClassNotFoundException e1) {
                // server is not supported
                console.sendMessage(ChatColor.RED + "This server is unsupported!");
                console.sendMessage(ChatColor.RED + "Please use Spigot or Paper version 1.13 or higher!");
                console.sendMessage(ChatColor.RED + "Plugin is now disabling itself!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        // register common listeners
        getServer().getPluginManager().registerEvents(new CommonListener(this), this);
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
