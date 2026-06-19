package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Plugins;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.List;
import java.util.Objects;

public class PlayerMove implements Listener, Runnable {

    private final ACore plugin;
    private final List<String> protectedRegions;
    private final List<String> commands;
    private final List<String> excludedPlayers;

    public PlayerMove(ACore plugin) {
        this.plugin = plugin;
        this.protectedRegions = plugin.getConfig().getStringList("events.PlayerMove.protectedRegions.regions");
        this.commands = plugin.getConfig().getStringList("events.PlayerMove.protectedRegions.commands");
        this.excludedPlayers = plugin.getConfig().getStringList("admins");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Plugins.WorldGuard.isEnabled()) {
            if (ACore.getInstance().getConfig().getBoolean("events.PlayerMove.protectedRegions.enabled")) {
                Player player = event.getPlayer();
                Location location = player.getLocation();

                if (excludedPlayers.contains(player.getName())) {
                    return;
                }

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld())));
                if (regionManager == null) {
                    return;
                }

                BlockVector3 playerVector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                ApplicableRegionSet regions = regionManager.getApplicableRegions(playerVector);

                for (ProtectedRegion region : regions) {
                    if (protectedRegions.contains(region.getId())) {
                        for (String command : commands) {
                            String cmd = command.replace("{player}", player.getName());
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        if (Plugins.WorldGuard.isEnabled()) {
            if (ACore.getInstance().getConfig().getBoolean("events.PlayerMove.protectedRegions.enabled")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (excludedPlayers.contains(player.getName())) {
                        continue;
                    }

                    org.bukkit.Location location = player.getLocation();
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld())));
                    if (regionManager == null) {
                        return;
                    }

                    BlockVector3 playerVector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
                    ApplicableRegionSet regions = regionManager.getApplicableRegions(playerVector);

                    for (ProtectedRegion region : regions) {
                        if (protectedRegions.contains(region.getId())) {
                            for (String command : commands) {
                                String cmd = command.replace("{player}", player.getName());
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

/*    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        if (!event.getMessage().startsWith("/cmi sethome") && !event.getMessage().startsWith("/cmi setwarp")) {
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld())));
        if (regionManager == null) {
            return;
        }

        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if ("our_house".equals(region.getId())) {
                BlockVector3 regionCenter = region.getMinimumPoint().add(region.getMaximumPoint()).divide(2);
                Location regionCenterLocation = new Location(location.getWorld(), regionCenter.getX(), regionCenter.getY(), regionCenter.getZ());

                if (location.distance(regionCenterLocation) <= 100) {
                    Informer.send(player, ("Вы не можете использовать эту команду здесь.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }*/
}

