package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Abroadcast implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!Methods.hasPermission(sender, "acore.command.abroadcast")) {
            if (args.length == 0) {
                Informer.send(sender,
                        ACore.getInstance().getConfig().getString("commands.abroadcast"));
                return true;
            }

            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }

            String formattedMessage = Colors.set(message.toString().trim());

            String[] lines = formattedMessage.split("\\|");
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (String line : lines) {
                    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        line = PlaceholderAPI.setPlaceholders(player, line);
                    }

                    line = line.replace("%player%", player.getName());
                    line = line.replace("%displayname%", player.getDisplayName());
                    line = line.replace("%ping%", String.valueOf(player.getPing()));
                    line = line.replace("%world%", player.getWorld().getName());
                    line = line.replace("%health%", String.format("%.1f", player.getHealth()));
                    line = line.replace("%food%", String.valueOf(player.getFoodLevel()));
                    line = line.replace("%x%", String.valueOf(player.getLocation().getBlockX()));
                    line = line.replace("%y%", String.valueOf(player.getLocation().getBlockY()));
                    line = line.replace("%z%", String.valueOf(player.getLocation().getBlockZ()));

                    Informer.send(player, line);
                }
            }

            String consoleMessage = Colors.set(message.toString().trim());
            Bukkit.getConsoleSender().sendMessage(consoleMessage);

            return true;
        } else {
            Informer.send(sender,
                    ACore.getInstance().getConfig().getString("messages.noPermissions"));
        }
        return false;
    }
}