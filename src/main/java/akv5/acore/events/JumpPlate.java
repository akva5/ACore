package akv5.acore.events;

import akv5.acore.ACore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class JumpPlate implements Listener {

    @EventHandler
    public void onMove(final PlayerMoveEvent e) {
        if (ACore.getInstance().getConfig().getBoolean("settings.jump-plate.enabled")) {
            final Player p = e.getPlayer();
            Material block = Material.valueOf(ACore.getInstance().getConfig().getString("settings.jump-plate.block"));
            if (p.getLocation().getBlock().getType() == block) {
                final Vector v = p.getLocation().getDirection().multiply(ACore.getInstance()
                                .getConfig().getInt("settings.jump-plate.knockback.power"))
                        .setY(ACore.getInstance().getConfig().getInt("settings.jump-plate.knockback.powerY"));
                p.setVelocity(v);
            }
        }
    }
}
