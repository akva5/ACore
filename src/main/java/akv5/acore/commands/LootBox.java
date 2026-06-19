package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.libs.managers.LootBoxManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LootBox implements CommandExecutor {

    private final LootBoxManager manager;

    public LootBox(LootBoxManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!Methods.hasPermission(sender, "acore.command.lootbox")) {
            Informer.send(player, ACore.getInstance().getConfig().getString("messages.noPermissions"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("tool")) {
            String type = args[1];
            ItemStack item = manager.getPlacerItem(type);
            if (item != null) {
                player.getInventory().addItem(item);
                Informer.send(player, "{prefix}Выдан установщик для лутбокса: &a" + type);
            } else {
                Informer.send(player, "{prefix}&cТакого типа лутбокса не существует в конфиге.");
            }
            return true;
        }

        Informer.send(player, "&a/lootbox tool &7[&aназвание&7] &f- Получить лутбокс для установки.");
        return true;
    }
}