package net.pl3x.bukkit.ridabledolphins.configuration;

import net.pl3x.bukkit.ridabledolphins.RidableDolphins;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    public static String LANGUAGE_FILE = "lang-en.yml";
    public static float SPEED_VERTICAL = 2.0F;
    public static float SPEED_HORIZONTAL = 0.75F;
    public static float SPEED_FORWARD = 0.15F;
    public static float SHOOTING_SPEED = 8.0F;
    public static float SHOOTING_DAMAGE = 5.0F;
    public static int SHOOTING_COOLDOWN = 10;

    public static void reload() {
        RidableDolphins plugin = RidableDolphins.getPlugin(RidableDolphins.class);
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        LANGUAGE_FILE = config.getString("language-file", "lang-en.yml");
        SPEED_VERTICAL = (float) config.getDouble("speed-modifiers.vertical", 2.0D);
        SPEED_HORIZONTAL = (float) config.getDouble("speed-modifiers.horizontal", 0.75D);
        SPEED_FORWARD = (float) config.getDouble("speed-modifiers.forward", 0.15D);
        SHOOTING_SPEED = (float) config.getDouble("shooting.speed", 8.0D);
        SHOOTING_DAMAGE = (float) config.getDouble("shooting.damage", 5.0D);
        SHOOTING_COOLDOWN = (int) config.getDouble("shooting.cooldown", 10);
    }
}
