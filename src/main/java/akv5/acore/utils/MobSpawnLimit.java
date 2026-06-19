package akv5.acore.utils;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class MobSpawnLimit implements Listener {
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("settings.spawnLimit.enabled")) {
            CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
            if (spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL ||
                    spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                    spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM ||
                    spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN ||
                    spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_WITHER ||
                    spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER ||
                    spawnReason == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) {
                int maxMobs = ACore.getInstance().getConfig().getInt("settings.spawnLimit.mobs");
                int radius = ACore.getInstance().getConfig().getInt("settings.spawnLimit.radius");

                int nearbyMobs = event.getEntity().getNearbyEntities(radius, radius, radius).stream().filter((entity) -> {
                    return entity.getType() == event.getEntityType();
                }).mapToInt((entity) -> {
                    return 1;
                }).sum();

                if (nearbyMobs >= maxMobs) {
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("settings.spawnLimit.enabled") && event.getEntityType() == EntityType.ARMOR_STAND) {

            int maxStands = ACore.getInstance().getConfig().getInt("settings.spawnLimit.stands");
            int radius = ACore.getInstance().getConfig().getInt("settings.spawnLimit.radius");

            int nearbyStands = (int) event.getEntity().getNearbyEntities(radius, radius, radius).stream()
                    .filter(entity -> entity instanceof ArmorStand)
                    .mapToInt(entity -> 1)
                    .sum();

            if (nearbyStands >= maxStands) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("settings.spawnLimit.enabled") && event.getVehicle() instanceof Minecart) {
            Location loc = event.getVehicle().getLocation();
            World world = loc.getWorld();

            assert world != null;

            Block block = world.getBlockAt(loc);
            if (block.getType() == Material.RAIL || block.getType() == Material.ACTIVATOR_RAIL || block.getType() == Material.DETECTOR_RAIL || block.getType() == Material.POWERED_RAIL) {
                int maxMinecarts = ACore.getInstance().getConfig().getInt("settings.spawnLimit.minecarts");
                int radius = ACore.getInstance().getConfig().getInt("settings.spawnLimit.radius");

                int nearbyMinecarts = (int) event.getVehicle().getNearbyEntities(radius, radius, radius).stream()
                        .filter(entity -> entity instanceof Minecart)
                        .mapToInt(entity -> 1)
                        .sum();

                if (nearbyMinecarts >= maxMinecarts) {
                    event.setCancelled(true);

                    Player player = event.getVehicle().getWorld().getPlayers().stream()
                            .filter(p -> p.getLocation().distance(loc) < 5)
                            .findFirst()
                            .orElse(null);

                    String playerName = player != null ? player.getName() : "Unknown";

                    String message = ACore.getInstance().getConfig().getString("settings.spawnLimit.message")
                            .replace("{player}", playerName)
                            .replace("{max}", String.valueOf(maxMinecarts));

                    for (Player admin : world.getPlayers()) {
                        if (Methods.hasPermission(admin, "acore.notify.spawnlimit")) {
                            Informer.send(admin, message);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicle(VehicleCreateEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("settings.spawnLimit.enabled") && event.getVehicle() instanceof Boat) {
            Location loc = event.getVehicle().getLocation();
            World world = loc.getWorld();

            assert world != null;

            int maxBoats = ACore.getInstance().getConfig().getInt("settings.spawnLimit.boats");
            int radius = ACore.getInstance().getConfig().getInt("settings.spawnLimit.radius");

            int nearbyBoats = (int) event.getVehicle().getNearbyEntities(radius, radius, radius).stream()
                    .filter(entity -> entity instanceof Boat)
                    .mapToInt(entity -> 1)
                    .sum();

            if (nearbyBoats >= maxBoats) {
                event.setCancelled(true);

                Player player = event.getVehicle().getWorld().getPlayers().stream()
                        .filter(p -> p.getLocation().distance(loc) < 5)
                        .findFirst()
                        .orElse(null);

                String playerName = player != null ? player.getName() : "Unknown";

                String message = ACore.getInstance().getConfig().getString("settings.spawnLimit.message")
                        .replace("{player}", playerName)
                        .replace("{max}", String.valueOf(maxBoats));

                for (Player onlinePlayer : world.getPlayers()) {
                    if (onlinePlayer.hasPermission("acore.notify.spawnlimit")) {
                        Informer.send(onlinePlayer, message);
                    }
                }
            }
        }
    }
}
