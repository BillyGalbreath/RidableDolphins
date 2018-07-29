package net.pl3x.bukkit.ridabledolphins.configuration;

import net.pl3x.bukkit.ridabledolphins.RidableDolphins;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Lang {
    public static String COMMAND_NO_PERMISSION;
    public static String COLLECT_NO_PERMISSION;
    public static String RIDE_NO_PERMISSION;
    public static String SHOOT_NO_PERMISSION;
    public static String BUCKET_NAME;
    public static List<String> BUCKET_LORE = new ArrayList<>();
    public static String VERSION;
    public static String RELOAD;

    public static void reload() {
        RidableDolphins plugin = RidableDolphins.getPlugin(RidableDolphins.class);
        String langFile = Config.LANGUAGE_FILE;
        File configFile = new File(plugin.getDataFolder(), langFile);
        if (!configFile.exists()) {
            plugin.saveResource(Config.LANGUAGE_FILE, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        COMMAND_NO_PERMISSION = config.getString("command-no-permission", "&cYou do not have permission for that command!");
        COLLECT_NO_PERMISSION = config.getString("collect-no-permission", "&cYou do not have permission to collect dolphins!");
        RIDE_NO_PERMISSION = config.getString("ride-no-permission", "&cYou do not have permission to ride!");
        SHOOT_NO_PERMISSION = config.getString("shoot-no-permission", "&cYou do not have permission to shoot!");
        VERSION = config.getString("version", "&d{plugin} v{version}.");
        RELOAD = config.getString("reload", "&d{plugin} v{version} reloaded config.");

        BUCKET_NAME = config.getString("bucket-name", "&bBucket of Dolphin");
        String lore = config.getString("bucket-lore", "This bucket contains a dolphin!\nRight click in water to place");
        BUCKET_LORE.clear();
        for (String line : lore.split("\n")) {
            BUCKET_LORE.add(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    public static void send(CommandSender recipient, String message) {
        if (message == null) {
            return; // do not send blank messages
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (ChatColor.stripColor(message).isEmpty()) {
            return; // do not send blank messages
        }

        for (String part : message.split("\n")) {
            recipient.sendMessage(part);
        }
    }
}
