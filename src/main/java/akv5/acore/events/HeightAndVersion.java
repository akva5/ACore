package akv5.acore.events;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HeightAndVersion implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        checkPlayerHeightAndVersion(player);
    }

    private void checkPlayerHeightAndVersion(Player player) {
        if (ACore.getInstance().getConfig().getBoolean("version")) {
            UserConnection userConnection = Via.getAPI().getConnection(player.getUniqueId());
            if (userConnection != null) {
                int protocolVersion = userConnection.getProtocolInfo().getProtocolVersion();
                if (protocolVersion >= 754 && protocolVersion <= 758) {
                    Location playerLocation = player.getLocation();
                    double playerY = playerLocation.getY();

                    if (playerY <= 1) {
                        int highestY = -1;
                        for (int y = 1; y <= 319; y++) {
                            Block block = player.getWorld().getBlockAt(playerLocation.getBlockX(), y, playerLocation.getBlockZ());
                            if (block.getType() != Material.AIR) {
                                highestY = y;
                            }
                        }

                        if (highestY != -1) {
                            player.teleport(new Location(player.getWorld(), playerLocation.getX(), highestY + 1, playerLocation.getZ()));
                            Informer.send(player, "&7\n{prefix}Ваша версия &cне поддерживает &fновые &9пещеры &fиз &61.18+&f!\n{prefix}Вы были &aтелепортированы &fнаверх!\n{prefix}Иначе вы бы застряли &7(без /fly или /gm не выбраться)\n&7");
                        } else {
                            Informer.send(player, "{prefix}&cНе удалось найти поверхность над вами.");
                        }
                    }
                }
            }
        }
    }
}
