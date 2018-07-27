package net.pl3x.bukkit.ridabledolphins.configuration;

import net.pl3x.bukkit.ridabledolphins.RidableDolphins;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    public static String LANGUAGE_FILE = "lang-en.yml";
    public static float SPEED_VERTICAL = 2.0F;
    public static float SPEED_HORIZONTAL = 0.75F;
    public static float SPEED_FORWARD = 0.15F;
    public static boolean BOUNCE = true;
    public static boolean BUBBLES = true;
    public static String SPACEBAR_MODE = "shoot";
    public static int SHOOTING_COOLDOWN = 10;
    public static float SHOOTING_SPEED = 1.0F;
    public static float SHOOTING_DAMAGE = 5.0F;
    public static int DASHING_COOLDOWN = 10;
    public static float DASHING_BOOST = 1.5F;
    public static int DASHING_DURATION = 20;

    public static void reload() {
        RidableDolphins plugin = RidableDolphins.getPlugin(RidableDolphins.class);
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        LANGUAGE_FILE = config.getString("language-file", "lang-en.yml");
        SPEED_VERTICAL = (float) config.getDouble("speed-modifiers.vertical", 2.0D);
        SPEED_HORIZONTAL = (float) config.getDouble("speed-modifiers.horizontal", 0.75D);
        SPEED_FORWARD = (float) config.getDouble("speed-modifiers.forward", 0.15D);
        BOUNCE = config.getBoolean("bounce", true);
        BUBBLES = config.getBoolean("bubbles", true);
        SPACEBAR_MODE = config.getString("spacebar", "shoot");
        SHOOTING_COOLDOWN = (int) config.getDouble("shooting.cooldown", 10);
        SHOOTING_SPEED = (float) config.getDouble("shooting.speed", 1.0D);
        SHOOTING_DAMAGE = (float) config.getDouble("shooting.damage", 5.0D);
        DASHING_COOLDOWN = (int) config.getDouble("dashing.cooldown", 100);
        DASHING_BOOST = (float) config.getDouble("dashing.boost", 1.5D);
        DASHING_DURATION = (int) config.getDouble("dashing.duration", 20);
    }
}
