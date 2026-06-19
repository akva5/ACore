package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.managers.LootBoxManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class LootBoxListener implements Listener {

    private final ACore plugin;
    private final LootBoxManager manager;

    public LootBoxListener(ACore plugin, LootBoxManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack hand = e.getItemInHand();
        if (hand.getType() == Material.END_PORTAL_FRAME && hand.hasItemMeta() && Objects.requireNonNull(hand.getItemMeta()).hasDisplayName()) {
            String name = hand.getItemMeta().getDisplayName();
            if (name.contains("Установщик Лутбокса")) {
                String typeId = ChatColor.stripColor(name).split(": ")[1].trim();

                manager.createLootBox(e.getBlockPlaced().getLocation(), typeId);
                Informer.send(e.getPlayer(), "{prefix}Лутбокс типа &a" + typeId + " &fуспешно установлен.");
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (manager.isLootBox(e.getBlock().getLocation())) {
            if (e.getPlayer().isOp() && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
                manager.removeLootBox(e.getBlock().getLocation());
                Informer.send(e.getPlayer(), "{prefix}&cЛутбокс удален.");
            } else {
                e.setCancelled(true);
                Informer.send(e.getPlayer(), "{prefix}&cВы не можете сломать этот лутбокс.");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            Location loc = e.getClickedBlock().getLocation();
            if (manager.isLootBox(loc)) {
                e.setCancelled(true);
                manager.openLootBox(e.getPlayer(), loc);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (manager.isLootBoxInventory(e.getView().getTopInventory())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = e.getView().getTopInventory();
        Inventory clickedInv = e.getClickedInventory();

        if (manager.isLootBoxInventory(topInv)) {

            if (clickedInv == topInv) {
                if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                    e.setCancelled(true);
                    return;
                }

                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    String typeId = manager.getTypeIdByInventory(topInv);

                    if (typeId != null) {
                        if (!manager.checkCooldown(player, typeId)) {
                            e.setCancelled(true);
                            Informer.send(player, "{prefix}&cПодождите немного.");
                            return;
                        }
                    }
                }
            }

            else {
                if (e.isShiftClick()) {
                    e.setCancelled(true);
                }
            }

            if (e.getAction().name().contains("COLLECT_TO_CURSOR")) {
                e.setCancelled(true);
            }
        }
    }
}