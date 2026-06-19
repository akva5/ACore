package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockCmdsInWorld implements Listener, CommandExecutor {

    private final ACore plugin;
    private final List<String> first_worlds = ACore.getInstance().getConfig().getStringList("blockCmds.first_worlds");
    private final List<String> second_worlds = ACore.getInstance().getConfig().getStringList("blockCmds.second_worlds");
    private final List<String> first_commands = ACore.getInstance().getConfig().getStringList("blockCmds.first_commands");
    private final List<String> second_commands = ACore.getInstance().getConfig().getStringList("blockCmds.second_commands");

    public BlockCmdsInWorld(ACore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return true;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (ACore.getInstance().getConfig().getBoolean("blockCmds.enabled")) {
            Player player = event.getPlayer();
            if (first_worlds.contains(player.getWorld().getName())) {
                String message = event.getMessage().toLowerCase();
                for (String command : first_commands) {
                    if (message.startsWith(command)) {
                        event.setCancelled(true);
                        Informer.send(player,
                                ACore.getInstance().getConfig().getString("messages.blockCmd"));
                        break;
                    }
                }
            }
            if (second_worlds.contains(player.getWorld().getName())) {
                String message = event.getMessage().toLowerCase();
                for (String command : second_commands) {
                    if (message.startsWith(command)) {
                        event.setCancelled(true);
                        Informer.send(player,
                                ACore.getInstance().getConfig().getString("messages.blockCmd"));
                        break;
                    }
                }
            }
        }
    }
}
