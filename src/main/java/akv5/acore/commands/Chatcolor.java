package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chatcolor implements CommandExecutor, TabCompleter {
    private final ACore plugin;

    String perms = ACore.getInstance().getConfig().getString("messages.noPermissions");

    public Chatcolor(ACore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!Methods.hasPermission(sender, "acore.command.chatcolor")) {
            Informer.send(sender, perms);
            return true;
        }

        String playerId = player.getUniqueId().toString();

        if (args.length < 1) {
            Informer.send(player, "&a/chatcolor &7[&aцвет&7] &f- Сменить цвет сообщения в чате.");
            return true;
        }

        String color = args[0].toLowerCase();
        String colorCode = plugin.getPlaceholdersConfig().getString("chatcolors." + color);

        if (colorCode == null) {
            Informer.send(player, "{prefix}&cНеверный цвет!");
            return true;
        }

        String permission = "acore.command.chatcolor." + color;
        if (!Methods.hasPermission(sender, permission)) {
            Informer.send(player, "{prefix}&cУ вас нет права на использование этого цвета.");
            return true;
        }

        plugin.getPlayersConfig().set(playerId + ".chatcolor", colorCode);
        plugin.savePlayersConfig();

        Informer.send(player, "{prefix}Цвет чата &aуспешно &fустановлен на " + colorCode + "Пример");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> allColors = new ArrayList<>();
            plugin.getPlaceholdersConfig().getConfigurationSection("chatcolors").getKeys(false).forEach(allColors::add);

            if (Methods.isAdmin(player)) {
                return allColors;
            }

            return allColors.stream()
                    .filter(color -> player.hasPermission("acore.command.chatcolor." + color))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
