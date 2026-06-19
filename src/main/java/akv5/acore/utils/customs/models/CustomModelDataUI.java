package akv5.acore.utils.customs.models;

import akv5.acore.libs.Colors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomModelDataUI {
    public static void open(Player player, ItemStack item, int page) {
        Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, item.getType() + " (страница " + page + ")");

        inv.setItem(52, createArrowItem("&fПредыдущая страница"));
        inv.setItem(53, createArrowItem("&fСледующая страница"));

        for (int i = (52 * (page - 1)) + 1; i < (52 * (page - 1)) + 54; i++) {
            ItemStack modelItem = new ItemStack(item.getType());
            modelItem.setAmount(1);
            modelItem = ItemUtil.changeModelData(modelItem, i);

            ItemMeta meta = modelItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Colors.set("&a" + item.getType() + " &7(&6" + i + "&7)"));
                modelItem.setItemMeta(meta);
            }

            inv.addItem(modelItem);
        }

        player.openInventory(inv);
    }

    private static ItemStack createArrowItem(String name) {
        ItemStack arrowItem = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrowItem.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setDisplayName(Colors.set(name));
            arrowItem.setItemMeta(arrowMeta);
        }
        return arrowItem;
    }
}