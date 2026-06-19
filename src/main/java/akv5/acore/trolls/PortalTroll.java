package akv5.acore.trolls;

import akv5.acore.ACore;
import akv5.acore.libs.Informer;
import akv5.acore.libs.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class PortalTroll implements CommandExecutor {

    String perms = ACore.getInstance().getConfig().getString("messages.noPermissions");
    String playerNotFound = ACore.getInstance().getConfig().getString("messages.playerNotFound");
    String usage = ACore.getInstance().getConfig().getString("trolls.portaltroll.usage");
    String protect = ACore.getInstance().getConfig().getString("trolls.protected");
    String success = ACore.getInstance().getConfig().getString("trolls.portaltroll.success");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!Methods.hasPermission(sender, "acore.trolls.portaltroll")) {
            Informer.send(sender, perms);
            return false;
        }

        if (args.length == 1) {
            String playerName = args[0];
            Player target = Bukkit.getPlayer(playerName);

            if (target != null && target.isOnline()) {
                if (Methods.isProtected(target)) {
                    Informer.send(sender, protect);
                    return true;
                }

                executePortalTroll(target);
                Informer.send(sender, success.replace("{player}", target.getName()));
                return true;
            } else {
                Informer.send(sender, playerNotFound);
            }
        } else {
            Informer.send(sender, usage);
        }
        return false;
    }

    private void executePortalTroll(Player target) {
        Location loc = target.getLocation().clone();
        loc.add(0, 5, 0);
        World world = target.getWorld();

        BlockData blockData = Bukkit.createBlockData(Material.END_PORTAL);

        FallingBlock fallingBlock1 = world.spawnFallingBlock(loc, blockData);
        fallingBlock1.setDropItem(false);
        fallingBlock1.setHurtEntities(false);

        loc.add(0, 0.2, 0);
        FallingBlock fallingBlock2 = world.spawnFallingBlock(loc, blockData);
        fallingBlock2.setDropItem(false);
        fallingBlock2.setHurtEntities(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!fallingBlock1.isDead()) {
                    fallingBlock1.remove();
                }
                if (!fallingBlock2.isDead()) {
                    fallingBlock2.remove();
                }
            }
        }.runTaskLater(ACore.getInstance(), 60L);
    }
}