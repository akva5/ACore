package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class BlockLaunch implements Listener {
    public final ACore plugin;
    public String perm = "acore.protect.launch";

    public BlockLaunch(ACore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (player.hasPermission(perm)) {
            return;
        }

        if (item != null) {
            if (
                    item.getType() == Material.FIREWORK_ROCKET ||
                    item.getType() == Material.EXPERIENCE_BOTTLE ||
                    item.getType() == Material.SNOWBALL ||
                    item.getType() == Material.FISHING_ROD ||
                    item.getType() == Material.TRIDENT ||
                    item.getType() == Material.ARROW ||
                    item.getType() == Material.SPECTRAL_ARROW ||

                    item.getType() == Material.FLINT_AND_STEEL ||
                    item.getType() == Material.END_CRYSTAL ||
                    item.getType() == Material.TNT ||

                    item.getType() == Material.BELL ||
                    item.getType() == Material.NOTE_BLOCK ||
                    item.getType() == Material.JUKEBOX ||

                    item.getType() == Material.ENDER_EYE ||
                    item.getType() == Material.ENDER_PEARL ||

                    item.getType() == Material.ANVIL ||
                    item.getType() == Material.DAMAGED_ANVIL ||
                    item.getType() == Material.CHIPPED_ANVIL ||

                    item.getType() == Material.OAK_BOAT ||
                    item.getType() == Material.DARK_OAK_BOAT ||
                    item.getType() == Material.SPRUCE_BOAT ||
                    item.getType() == Material.JUNGLE_BOAT ||
                    item.getType() == Material.ACACIA_BOAT ||
                    item.getType() == Material.BIRCH_BOAT ||

                    item.getType() == Material.MINECART ||
                    item.getType() == Material.CHEST_MINECART ||
                    item.getType() == Material.FURNACE_MINECART ||
                    item.getType() == Material.TNT_MINECART ||
                    item.getType() == Material.HOPPER_MINECART ||
                    item.getType() == Material.COMMAND_BLOCK_MINECART ||

                    item.getType() == Material.WATER_BUCKET ||
                    item.getType() == Material.LAVA_BUCKET ||

                    item.getType() == Material.LINGERING_POTION ||
                    item.getType() == Material.SPLASH_POTION ||

                    item.getType() == Material.PUFFERFISH_BUCKET ||
                    item.getType() == Material.SALMON_BUCKET ||
                    item.getType() == Material.TROPICAL_FISH_BUCKET ||
                    item.getType() == Material.COD_BUCKET
            ) {
                if (isInRestrictedArea(player.getLocation())) {
                    event.setCancelled(true);
                    send(player);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player player) {
            if (player.hasPermission(perm)) {
                return;
            }

            if (projectile instanceof Firework ||
                    projectile instanceof Snowball ||
                    projectile instanceof Trident ||
                    projectile instanceof Arrow ||
                    projectile instanceof SpectralArrow ||
                    projectile instanceof Egg) {
                if (isInRestrictedArea(player.getLocation())) {
                    event.setCancelled(true);
                    send(player);
                }
            }
        }
    }

/*    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(perm)) {
            return;
        }

        if (isInRestrictedArea(player.getLocation())) {
            event.setCancelled(true);
            send(player);
        }
    }*/

    private boolean isInRestrictedArea(Location location) {
        for (Player player : Objects.requireNonNull(location.getWorld()).getPlayers()) {
            if (player.hasPermission(perm)) {
                if (location.distance(player.getLocation()) <= 24) {
                    return true;
                }
            }
        }
        return false;
    }

    private void send(Player player) {
        Informer.send(player, "&7\n{prefix}Вы не можете сделать этого здесь,\n     так как &bрядом &fнаходится &cадминистратор&f.\n     Попробуйте написать команду &a/rtp &fили\n     &aулететь&7/&aубежать &fподальше от этого места. &b:/\n&7");
    }
}
