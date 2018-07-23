package net.pl3x.bukkit.ridabledolphins.command;

import net.pl3x.bukkit.ridabledolphins.RidableDolphins;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdRidableDolphins implements TabExecutor {
    private final RidableDolphins plugin;

    public CmdRidableDolphins(RidableDolphins plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ridabledolphins.command.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for that");
                return true;
            }

            plugin.reloadConfig();

            RidableDolphins.verticalSpeedModifier = (float) plugin.getConfig().getDouble("speed-modifiers.vertical");
            RidableDolphins.horizontalSpeedModifier = (float) plugin.getConfig().getDouble("speed-modifiers.horizontal");
            RidableDolphins.forwardSpeedModifier = (float) plugin.getConfig().getDouble("speed-modifiers.forward");

            sender.sendMessage(ChatColor.GREEN + plugin.getName() + " v" + plugin.getDescription().getVersion() + " reloaded config");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + plugin.getName() + " v" + plugin.getDescription().getVersion());
        return true;
    }
}
