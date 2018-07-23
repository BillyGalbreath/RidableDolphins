package net.pl3x.bukkit.ridabledolphins;

import net.pl3x.bukkit.ridabledolphins.command.CmdRidableDolphins;
import net.pl3x.bukkit.ridabledolphins.entity.EntityRidableDolphin;
import net.pl3x.bukkit.ridabledolphins.listener.DolphinListener;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class RidableDolphins extends JavaPlugin implements Listener {
    public static float verticalSpeedModifier = 2.0F;
    public static float horizontalSpeedModifier = 0.75F;
    public static float forwardSpeedModifier = 0.15F;
    public static float shootingSpeed = 8.0F;
    public static float shootingDamage = 5.0F;
    public static int shootingCooldown = 10;

    @Override
    public void onLoad() {
        // DOES NOT WORK RIGHT! need to find the rest of the registration process
        //EntityTypes.a("dolphin", EntityTypes.a.a(EntityRidableDolphin.class, EntityRidableDolphin::new));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

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

        // listeners \o/
        getServer().getPluginManager().registerEvents(new DolphinListener(this), this);

        // commands \o/ idky i'm so excited
        getCommand("ridabledolphins").setExecutor(new CmdRidableDolphins(this));

        // setup initial speed modifiers
        RidableDolphins.verticalSpeedModifier = (float) getConfig().getDouble("speed-modifiers.vertical", 2.0D);
        RidableDolphins.horizontalSpeedModifier = (float) getConfig().getDouble("speed-modifiers.horizontal", 0.75D);
        RidableDolphins.forwardSpeedModifier = (float) getConfig().getDouble("speed-modifiers.forward", 0.15D);
        RidableDolphins.shootingSpeed = (float) getConfig().getDouble("shooting.speed", 8.0D);
        RidableDolphins.shootingDamage = (float) getConfig().getDouble("shooting.damage", 5.0D);
        RidableDolphins.shootingCooldown = (int) getConfig().getDouble("shooting.cooldown", 10);
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
