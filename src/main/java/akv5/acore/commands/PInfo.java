/*
package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Colors;
import akv5.acore.libs.Informer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PInfo implements CommandExecutor, Listener {

    List<String> adminList = ACore.getInstance().getConfig().getStringList("admins");
    List<String> adminIpList = ACore.getInstance().getConfig().getStringList("adminIps");

    private boolean isAdmin(Player player) {
        if (adminList.contains(player.getName())) {
            return true;
        }
        String playerIp = player.getAddress().getAddress().getHostAddress();
        return adminIpList.contains(playerIp);
    }

    public PInfo() {
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("acore.command.pinfo")) {
            Informer.send(sender,
                    ACore.getInstance().getConfig().getString("messages.noPermissions"));
        } else if (args.length == 0) {
            Informer.send(sender, 
                    ACore.getInstance().getConfig().getString("commands.pinfo.usage"));
        } else {
            String playerName = args[0];
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                Informer.send(sender, 
                        ACore.getInstance().getConfig().getString("messages.playerNotFound"));
            } else if (isAdmin(targetPlayer)) {
                Informer.send(sender, 
                        ACore.getInstance().getConfig().getString("messages.isAdmin"));
            } else {
                showPlayerInfo(sender, targetPlayer);
            }
        }
        return true;
    }

    private void showPlayerInfo(CommandSender sender, Player player) {
        ItemStack head = getPlayerHead(player);
        Inventory menu = Bukkit.createInventory(null, 9,
                Colors.set(ACore.getInstance().getConfig().getString("commands.pinfo.menu_title")));
        menu.setItem(ACore.getInstance().getConfig().getInt("commands.pinfo.slot"), head);
        ((Player) sender).openInventory(menu);
    }

    private ItemStack getPlayerHead(Player player) {
        String itemType = ACore.getInstance().getConfig().getString("commands.pinfo.material");
        ItemStack skull = new ItemStack(Material.valueOf(itemType));
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(player);
        meta.setDisplayName(Colors.set(
                ACore.getInstance().getConfig().getString("commands.pinfo.displayName")));
        meta.setLore(
                PlaceholderAPI.setPlaceholders(player,
                        Colors.set(ACore.getInstance().getConfig().getStringList("commands.pinfo.lore"))));
        skull.setItemMeta(meta);
        return skull;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String menuTitle = Colors.set(ACore.getInstance().getConfig().getString("commands.pinfo.menu_title"));
        if (event.getView().getTitle().equals(menuTitle)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            String itemType = ACore.getInstance().getConfig().getString("commands.pinfo.material");
            if (clickedItem.getType() == Material.valueOf(itemType)) {
                if (clickedItem.getType() == Material.STONE) {
                    ItemStack originalItem = ACore.getInstance().getConfig().getItemStack("commands.pinfo.material");
                    if (originalItem != null) {
                        event.getInventory().setItem(event.getSlot(), originalItem);
                    }
                }
                player.closeInventory();
            }
        }
    }
}*/
