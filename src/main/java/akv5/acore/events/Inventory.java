/*
package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.utils.customs.models.CustomModelDataUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Inventory implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().contains("(страница")) {
            if (event.getClickedInventory() == event.getView().getBottomInventory()) return;

            ItemStack clicked = event.getCurrentItem();
            ItemStack item = player.getInventory().getItemInMainHand();
            int page = Integer.parseInt(event.getView().getTitle().replace(" (страница ", "")
                    .replace(item.getType().toString(), "").replace(")", ""));

            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW
                    && clicked.getItemMeta().getDisplayName().contains("Предыдущая страница")) {
                page--;
                if (page <= 0) {
                    Informer.send(player,
                            ACore.getInstance().getConfig().getString("messages.thispageno"));
                    event.setCancelled(true);
                    return;
                }
                CustomModelDataUI.open(player, item, page);
            } else if (clicked.getType() == Material.ARROW
                    && clicked.getItemMeta().getDisplayName().contains("Следующая страница")) {
                page++;
                CustomModelDataUI.open(player, item, page);
            } else {
                player.getInventory().addItem(clicked);
            }

            event.setCancelled(true);
        }
    }
}
*/
