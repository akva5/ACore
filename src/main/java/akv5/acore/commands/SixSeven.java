package akv5.acore.commands;

import akv5.acore.ACore;
import akv5.acore.libs.Cooldowner;
import akv5.acore.libs.Informer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SixSeven implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (Cooldowner.isInCooldown(player.getName(), Cooldowner.Type.MESSAGE)) {
            String msg = ACore.getInstance().getConfig().getString("messages.cooldown-cmds");
            Informer.send(player, msg);
            return false;
        }

        Cooldowner.start(player.getName(), Cooldowner.Type.MESSAGE, 15);

        new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                if (seconds >= 10) {
                    this.cancel();
                    return;
                }

                if (player.isOnline()) {
                    player.setHealth(0);
                    Informer.sendTitle(player, "&a>> &c&lСИКС СЕВЕН &a<<", "");
                    // Тут звук будет
                } else {
                    this.cancel();
                    return;
                }

                seconds++;
            }
        }.runTaskTimer(ACore.getInstance(), 0L, 20L);

        return true;
    }
}