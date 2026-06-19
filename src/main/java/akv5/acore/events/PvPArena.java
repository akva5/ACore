package akv5.acore.events;

import akv5.acore.ACore;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.leymooo.antirelog.event.PvpStartedEvent;
import ru.leymooo.antirelog.event.PvpStoppedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PvPArena implements Listener {

    private final Map<BlockVector3, BlockState> savedBlockStates = new HashMap<>();
    private final ACore plugin;

    public PvPArena(ACore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPvpStart(PvpStartedEvent event) {
        if (!plugin.getConfig().getBoolean("events.PvPArena.border")) return;

        Player player = event.getAttacker();
        World world = player.getWorld();

        RegionManager regionManager = WorldGuard.getInstance().getPlatform()
                .getRegionContainer().get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
        if (regionManager == null) return;

        ProtectedRegion region = regionManager.getRegion(Objects.requireNonNull(plugin.getConfig().getString("events.PvPArena.region")));
        if (region == null) return;

        int baseY = region.getMinimumPoint().getY() + 1;
        int topY = baseY + 2;

        for (int y = baseY; y <= topY; y++) {
            for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {

                    if (isEdge(x, z, region)) {
                        Block block = world.getBlockAt(x, y, z);
                        BlockVector3 pos = BlockVector3.at(x, y, z);

                        if (!savedBlockStates.containsKey(pos)) {
                            savedBlockStates.put(pos, block.getState());
                        }

                        block.setType(Material.RED_STAINED_GLASS);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPvpStop(PvpStoppedEvent event) {
        if (!plugin.getConfig().getBoolean("events.PvPArena.border")) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        RegionManager regionManager = WorldGuard.getInstance().getPlatform()
                .getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) return;

        ProtectedRegion region = regionManager.getRegion(Objects.requireNonNull(plugin.getConfig().getString("events.PvPArena.region")));
        if (region == null) return;

        for (Map.Entry<BlockVector3, BlockState> entry : savedBlockStates.entrySet()) {
            BlockVector3 pos = entry.getKey();
            BlockState state = entry.getValue();

            Block block = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

            state.update(true, false);
        }

        savedBlockStates.clear();
    }

    private boolean isEdge(int x, int z, ProtectedRegion region) {
        int minX = region.getMinimumPoint().getBlockX();
        int maxX = region.getMaximumPoint().getBlockX();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxZ = region.getMaximumPoint().getBlockZ();

        return (x == minX || x == maxX || z == minZ || z == maxZ);
    }
}
