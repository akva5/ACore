package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import akv5.acore.utils.customs.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CItems implements CommandExecutor, TabCompleter {

    private String getMessage(String key) {
        String path = "commands.citems." + key;
        String msg = ACore.getInstance().getConfig().getString(path);
        return msg == null ? "" : Colors.set(msg);
    }

    private String formatMessage(String key, String itemName, String senderName, String targetName) {
        String msg = ACore.getInstance().getConfig().getString("commands.citems." + key);
        if (msg == null) return "";
        if (itemName != null)
            msg = msg.replace("{item}", itemName);
        if (senderName != null)
            msg = msg.replace("{sender}", senderName);
        if (targetName != null)
            msg = msg.replace("{target}", targetName);
        return Colors.set(msg);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!Methods.hasPermission(sender, "acore.command.citems")) {
            Informer.send(sender, ACore.getInstance().getConfig().getString("messages.noPermissions"));
            return true;
        }

        if (sender instanceof Player player) {
            if (args.length == 1) {
                String itemName = args[0];
                ItemStack customItem = CustomItems.getInstance().custom(itemName, player);
                if (customItem != null) {
                    player.getInventory().addItem(customItem);
                    Informer.send(player, formatMessage("itemReceivedSelf", itemName, null, null));
                    return true;
                } else {
                    Informer.send(player, getMessage("itemsNotFound"));
                    return false;
                }
            } else if (args.length == 2) {
                String itemName = args[0];
                String targetName = args[1];
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) {
                    Informer.send(player, ACore.getInstance().getConfig().getString("messages.playerNotFound"));
                    return false;
                }
                ItemStack customItem = CustomItems.getInstance().custom(itemName, target);
                if (customItem != null) {
                    target.getInventory().addItem(customItem);
                    Informer.send(player, formatMessage("itemGivenToOther", itemName, null, targetName));
                    Informer.send(target, formatMessage("itemReceivedFromOther", itemName, player.getName(), null));
                    return true;
                } else {
                    Informer.send(player, getMessage("itemsNotFound"));
                    return false;
                }
            } else {
                Informer.send(player, getMessage("player"));
                return false;
            }
        } else {
            if (args.length == 2) {
                String itemName = args[0];
                String playerName = args[1];
                Player targetPlayer = Bukkit.getPlayerExact(playerName);
                if (targetPlayer != null) {
                    ItemStack customItem = CustomItems.getInstance().custom(itemName, targetPlayer);
                    if (customItem != null) {
                        targetPlayer.getInventory().addItem(customItem);
                        Informer.send(sender, formatMessage("itemGivenToOther", itemName, null, playerName));
                        Informer.send(targetPlayer, formatMessage("itemReceivedSelf", itemName, null, null));
                        return true;
                    } else {
                        Informer.send(sender, getMessage("itemsNotFound"));
                        return false;
                    }
                } else {
                    Informer.send(sender, getMessage("playerNotFound"));
                    return false;
                }
            } else {
                Informer.send(sender, getMessage("console"));
                return false;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, String[] args) {
        Set<String> customItemKeys = ACore.getInstance().getItemsConfig().getConfigurationSection("customItems").getKeys(false);

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String currentArg = args[0].toLowerCase();

            for (String key : customItemKeys) {
                if (key.toLowerCase().startsWith(currentArg)) {
                    completions.add(key);
                }
            }
            return completions;
        } else if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            String currentArg = args[1].toLowerCase();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().toLowerCase().startsWith(currentArg)) {
                    completions.add(onlinePlayer.getName());
                }
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
