package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import java.util.List;

public class BlockCmds implements Listener {

    private final List<String> blockedCommands = ACore.getInstance().getConfig().getStringList("blockedCmds");
    private final List<String> adminList = ACore.getInstance().getConfig().getStringList("admins");
    private final List<String> adminIpList = ACore.getInstance().getConfig().getStringList("adminIps");

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length > 0) {
            String command = args[0].substring(1);

            if (event.getPlayer().hasPermission("acore.bypass.blockcmds")) {
                return;
            }

            if (blockedCommands.contains(command)) {
                Player player = event.getPlayer();
                if (isAdmin(player)) {
                    event.setCancelled(false);
                } else {
                    Informer.send(player, ACore.getInstance().getConfig().getString("messages.isAdmin"));
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isAdmin(Player player) {
        if (adminList.contains(player.getName())) {
            return true;
        }

        String playerIp = player.getAddress().getAddress().getHostAddress();
        return adminIpList.contains(playerIp);
    }
}
