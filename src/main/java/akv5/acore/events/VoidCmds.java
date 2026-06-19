package akv5.acore.events;

import akv5.acore.ACore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoidCmds implements Listener {

    private final List<String> commands = ACore.getInstance().getConfig().getStringList("events.VoidCmds.commands");
    private final List<String> worlds = ACore.getInstance().getConfig().getStringList("events.VoidCmds.worlds");
    private final int height = ACore.getInstance().getConfig().getInt("events.VoidCmds.height");
    private final int delay = ACore.getInstance().getConfig().getInt("events.VoidCmds.delay");
    private final Map<UUID, Boolean> processedPlayers = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("events.VoidCmds.enabled")) {
            Player player = event.getPlayer();
            if (worlds.contains(player.getWorld().getName()) && player.getLocation().getY() <= height) {
                event.getPlayer().setFallDistance(0);
                if (!processedPlayers.containsKey(player.getUniqueId())) {
                    processedPlayers.put(player.getUniqueId(), true);
                    executeCommandsWithDelay(player, 0);
                }
            } else {
                processedPlayers.remove(player.getUniqueId());
            }
        }
    }

    private void executeCommandsWithDelay(Player player, int index) {
        if (ACore.getInstance().getConfig().getBoolean("events.VoidCmds.enabled")) {
            if (index < commands.size()) {
                String cmd = commands.get(index).replace("{player}", player.getName());
                int nextIndex = index + 1;
                Bukkit.getScheduler().runTaskLater(ACore.getInstance(), () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    executeCommandsWithDelay(player, nextIndex);
                }, delay * 20L);
            } else {
                processedPlayers.remove(player.getUniqueId());
            }
        }
    }
}
